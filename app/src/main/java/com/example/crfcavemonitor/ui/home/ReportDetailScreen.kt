package com.example.crfcavemonitor.ui.report

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.crfcavemonitor.data.Photo
import com.example.crfcavemonitor.data.Report
import com.example.crfcavemonitor.data.SpeciesCount
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    report: Report,
    speciesCounts: List<SpeciesCount>,
    photos: List<Photo>,
    onBack: () -> Unit,
    onDelete: (Report) -> Unit,
    onEdit: (Report) -> Unit,
    onExport: (Report) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Report") },
            text = { Text("Are you sure you want to delete '${report.caveName} (${report.mssAcc})'?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(report)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
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
                Button(onClick = { onEdit(report) }) { Text("Edit") }
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
                Button(
                    onClick = { onExport(report) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Export") }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Title block
            item {
                Text("${report.caveName} (${report.mssAcc})", style = MaterialTheme.typography.headlineSmall)
                Text("Date: ${dateFormatter.format(report.monitorDate)}", style = MaterialTheme.typography.bodyMedium)
            }

            // Visit Details (formatted key/value grid)
            item {
                SectionHeader("Visit Details")
                KeyValueGrid(
                    pairs = listOf(
                        "Owner" to report.owner,
                        "District or Unit" to report.unit,
                        "Organization" to report.organization,
                        "Monitored By" to report.monitoredBy,
                        "Area Monitored" to report.areaMonitored,
                        "Rationale" to report.rationale,
                        "Entrance Coordinates" to report.location
                    )
                )
            }

            // Use / Human Impact
            item {
                SectionHeader("Use / Human Impact")
                KeyValueGrid(
                    pairs = listOf(
                        "Visitation" to report.visitation,
                        "Litter" to report.litter,
                        "Speleothem Vandalism" to report.speleothemVandalism,
                        "Graffiti" to report.graffiti,
                        "Archaeological Looting" to report.archaeologicalLooting,
                        "Fires" to report.fires,
                        "Camping" to report.camping,
                        "Current Disturbance" to report.currentDisturbance,
                        "Potential Disturbance" to report.potentialDisturbance
                    )
                )
                // Long-form paragraphs
                if (report.manageConsiderations.isNotBlank()) {
                    LabeledParagraph("Management Considerations", report.manageConsiderations)
                }
                if (report.recommendations.isNotBlank()) {
                    LabeledParagraph("Recommendations", report.recommendations)
                }
                if (report.otherComments.isNotBlank()) {
                    LabeledParagraph("Other Comments", report.otherComments)
                }
            }

            // Bio (Species table-like list)
            if (speciesCounts.isNotEmpty()) {
                item { SectionHeader("Bio (Species Counts)") }
                items(speciesCounts, key = { it.id }) { s ->
                    SpeciesRowView(s)
                    Divider(Modifier.padding(top = 8.dp))
                }
            }

            // Photos grid
            if (photos.isNotEmpty()) {
                item { SectionHeader("Photos") }
                items(photos.chunked(2)) { pair ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PhotoCard(
                            photo = pair[0],
                            modifier = Modifier.weight(1f)
                        )
                        if (pair.size == 2) {
                            PhotoCard(
                                photo = pair[1],
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Divider(Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun KeyValueGrid(pairs: List<Pair<String, String>>) {
    // 2-column responsive grid using simple Rows
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        pairs.chunked(2).forEach { rowPairs ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowPairs.forEach { (k, v) ->
                    Column(Modifier.weight(1f)) {
                        Text(k, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text(v.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (rowPairs.size == 1) Spacer(Modifier.weight(1f)) // keep grid aligned
            }
        }
    }
}

@Composable
private fun LabeledParagraph(label: String, text: String) {
    Spacer(Modifier.height(6.dp))
    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SpeciesRowView(s: SpeciesCount) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(s.speciesName.ifBlank { "(unnamed)" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${s.count}", style = MaterialTheme.typography.bodyLarge)
        }
        if (s.notes.isNotBlank()) {
            Text(s.notes, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun PhotosGrid(photos: List<Photo>) {
    // 2 columns; adjusts heights; each cell shows image, caption, timestamp
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 0.dp)
    ) {
        items(photos, key = { it.id }) { p ->
            PhotoCard(p)
        }
    }
}

@Composable
private fun PhotoCard(photo: Photo, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(Uri.parse(photo.uri))
                    .crossfade(true)
                    .size(1024)         // ~max pixel bound (both dims) for the decode
                    .precision(Precision.AUTOMATIC)
                    .scale(Scale.FILL)
                    .allowHardware(false)
                    .build(),
                contentDescription = photo.caption,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = photo.caption.ifBlank { "(no caption)" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(photo.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}