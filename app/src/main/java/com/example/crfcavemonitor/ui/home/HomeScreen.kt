package com.example.crfcavemonitor.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.crfcavemonitor.data.Report
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    reports: List<Report>,
    onAddNew: () -> Unit,
    onSelectReport: (Report) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(reports) { report ->
                ReportItem(report, onClick = { onSelectReport(report) })
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = "${report.caveName} (MSS #${report.mssAcc})", // show cave + MSS #
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = dateFormatter.format(report.monitorDate),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}