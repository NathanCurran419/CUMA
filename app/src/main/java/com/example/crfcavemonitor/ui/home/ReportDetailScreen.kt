package com.example.crfcavemonitor.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.crfcavemonitor.data.Report
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    report: Report,
    onBack: () -> Unit,
    onDelete: (Report) -> Unit,
    onEdit: (Report) -> Unit,
    onExport: (Report) -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Report") },
            text = {
                Text("Are you sure you want to delete '${report.caveName} (${report.mssAcc})'?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(report)
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onEdit(report) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
                Button(
                    onClick = { onExport(report) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Export")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "${report.caveName} (${report.mssAcc})",
                style = MaterialTheme.typography.headlineSmall
            )
            Text("Date: ${dateFormatter.format(report.monitorDate)}")
            Spacer(Modifier.height(16.dp))

            Text("Owner/Unit: ${report.ownerUnit}")
            Text("Rationale: ${report.rationale}")
            Text("Area Monitored: ${report.areaMonitored}")
            Text("Organization: ${report.organization}")
            Text("Monitored By: ${report.monitoredBy}")
            Spacer(Modifier.height(16.dp))

            Text("Visitation: ${report.visitation}")
            Text("Litter: ${report.litter}")
            Text("Speleothem Vandalism: ${report.speleothemVandalism}")
            Text("Graffiti: ${report.graffiti}")
            Text("Archaeological Looting: ${report.archaeologicalLooting}")
            Text("Fires: ${report.fires}")
            Text("Camping: ${report.camping}")
            Spacer(Modifier.height(16.dp))

            Text("Current Disturbance: ${report.currentDisturbance}")
            Text("Potential Disturbance: ${report.potentialDisturbance}")
            Spacer(Modifier.height(16.dp))

            Text("Management Considerations:")
            Text(report.manageConsiderations, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(8.dp))

            Text("Other Comments:")
            Text(report.otherComments, style = MaterialTheme.typography.bodyMedium)
        }
    }
}