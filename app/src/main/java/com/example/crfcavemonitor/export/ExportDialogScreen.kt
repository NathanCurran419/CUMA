package com.example.crfcavemonitor.export

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialogRoute(
    reportId: Long,
    app: Application,
    onDismiss: () -> Unit
) {
    val vm: ExportViewModel = viewModel(
        factory = ExportVMFactory(app, reportId)
    )
    ExportDialogScreen(vm, onDismiss)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialogScreen(
    vm: ExportViewModel,
    onDismiss: () -> Unit
) {
    val reportName by vm.reportName.collectAsState()
    val payload by vm.payload.collectAsState()

    val context = LocalContext.current
    val resolver = remember(context) { context.contentResolver }

    var sections by rememberSaveable { mutableStateOf(setOf(ExportSection.ALL)) }
    var format by rememberSaveable { mutableStateOf(ExportFormat.CSV_USE_MONITORING) }

    // Build dynamic suggested filename
    fun suggestedFileName(baseName: String, f: ExportFormat): String {
        val base = if (baseName.isBlank()) "report" else baseName
        val suffix = when (f) {
            ExportFormat.CSV_USE_MONITORING -> "UseMonitoring"
            ExportFormat.CSV_BIO_MONITORING -> "BioMonitoring"
            ExportFormat.PDF -> "Report"
        }
        val ext = if (f == ExportFormat.PDF) "pdf" else "csv"
        return "$base-$suffix.$ext"
    }

    // CSV launcher
    val createCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val p = payload ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            val csvFmt = when (format) {
                ExportFormat.CSV_BIO_MONITORING -> CsvFormat.BIO_MONITORING
                else -> CsvFormat.MCD_USE_MONITORING
            }

            val selection = ExportSelection(
                sections = setOf(ExportSection.ALL),
                format = format,
                csvFormat = csvFmt
            )

            try {
                val csv = buildCsv(selection, p)
                writeTextToUri(resolver, uri, csv)
                Toast.makeText(context, "CSV export complete", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (t: Throwable) {
                Toast.makeText(context, "Export failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // PDF launcher
    val createPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        val p = payload ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            val selection = ExportSelection(
                sections = sections,
                format = ExportFormat.PDF
            )
            try {
                val doc = buildStyledPdf(context, selection, p, resolver)
                writePdfToUri(resolver, uri, doc)
                Toast.makeText(context, "PDF export complete", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (t: Throwable) {
                Toast.makeText(context, "Export failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Export - $reportName",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val buttonText = when (format) {
                        ExportFormat.PDF -> "Export Report as PDF"
                        ExportFormat.CSV_USE_MONITORING -> "Export Use Monitoring CSV"
                        ExportFormat.CSV_BIO_MONITORING -> "Export Bio Monitoring CSV"
                    }

                    Button(
                        enabled = payload != null,
                        onClick = {
                            val base = if (reportName.isBlank()) "report" else reportName
                            when (format) {
                                ExportFormat.PDF -> createPdf.launch(suggestedFileName(base, format))
                                ExportFormat.CSV_USE_MONITORING -> createCsv.launch(suggestedFileName(base, format))
                                ExportFormat.CSV_BIO_MONITORING -> createCsv.launch(suggestedFileName(base, format))
                            }
                        }
                    ) {
                        Text(buttonText)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text("Format", style = MaterialTheme.typography.titleMedium)

            var fmtExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = fmtExpanded,
                onExpandedChange = { fmtExpanded = !fmtExpanded }
            ) {
                val formatDisplay = when (format) {
                    ExportFormat.PDF -> "Report as PDF"
                    ExportFormat.CSV_USE_MONITORING -> "Use Monitoring CSV"
                    ExportFormat.CSV_BIO_MONITORING -> "Bio Monitoring CSV"
                }
                TextField(
                    value = formatDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Format") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fmtExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = fmtExpanded,
                    onDismissRequest = { fmtExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Use Monitoring CSV") },
                        onClick = {
                            format = ExportFormat.CSV_USE_MONITORING
                            sections = setOf(ExportSection.ALL)
                            fmtExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bio Monitoring CSV") },
                        onClick = {
                            format = ExportFormat.CSV_BIO_MONITORING
                            sections = setOf(ExportSection.ALL)
                            fmtExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Report as PDF") },
                        onClick = {
                            format = ExportFormat.PDF
                            fmtExpanded = false
                        }
                    )
                }
            }

            // ===== FIELDS ONLY WHEN PDF =====
            if (format == ExportFormat.PDF) {
                Divider()
                Text("Fields to Export", style = MaterialTheme.typography.titleMedium)

                // ALL
                SectionToggleRow(
                    label = "All",
                    checked = ExportSection.ALL in sections
                ) { checked ->
                    sections = if (checked) setOf(ExportSection.ALL) else emptySet()
                }

                Divider()

                // Subsections
                listOf(
                    ExportSection.VISIT to "Visit",
                    ExportSection.BIO to "Bio",
                    ExportSection.PHOTOS to "Photos",
                    ExportSection.USE to "Use"
                ).forEach { (sec, label) ->
                    val all = ExportSection.ALL in sections
                    val checked = all || sections.contains(sec)
                    SectionToggleRow(label, checked) { isChecked ->
                        sections = sections.toMutableSet().apply {
                            remove(ExportSection.ALL)
                            if (isChecked) add(sec) else remove(sec)
                        }.ifEmpty { setOf(ExportSection.ALL) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}