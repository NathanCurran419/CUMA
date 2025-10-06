package com.example.crfcavemonitor.export

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

    // Capture once
    val context = LocalContext.current
    val resolver = remember(context) { context.contentResolver }

    var sections by rememberSaveable { mutableStateOf(setOf(ExportSection.ALL)) }
    var format by rememberSaveable { mutableStateOf(ExportFormat.CSV) }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        val p = payload ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            resolver.takePersistablePermissionIfPossible(uri)
            val selection = ExportSelection(sections, format)
            when (format) {
                ExportFormat.CSV -> {
                    val csv = buildCsv(selection, p)
                    writeTextToUri(resolver, uri, csv)
                }
                ExportFormat.PDF -> {
                    val doc = buildStyledPdf(context, selection, p, resolver)
                    writePdfToUri(resolver, uri, doc)
                }
            }
            onDismiss()
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
                    Button(
                        enabled = payload != null,
                        onClick = {
                            val base = if (reportName.isBlank()) "report" else reportName
                            val ext = if (format == ExportFormat.CSV) "csv" else "pdf"
                            createDocument.launch("$base-export.$ext")
                        }
                    ) {
                        Text("Export Report")
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
            Text("Options", style = MaterialTheme.typography.titleMedium)

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

            // Format
            Text("Format", style = MaterialTheme.typography.titleMedium)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = if (format == ExportFormat.CSV) "CSV" else "PDF",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Format") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("CSV") },
                        onClick = { format = ExportFormat.CSV; expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("PDF") },
                        onClick = { format = ExportFormat.PDF; expanded = false }
                    )
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
