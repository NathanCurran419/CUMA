package com.example.crfcavemonitor.ui

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.crfcavemonitor.data.Photo
import com.example.crfcavemonitor.data.Report
import com.example.crfcavemonitor.data.SpeciesCount
import com.example.crfcavemonitor.ui.home.HomeScreen
import com.example.crfcavemonitor.ui.report.ReportDetailScreen
import com.example.crfcavemonitor.ui.report.ReportFormScreen
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.crfcavemonitor.R

// ---- Export screen route helpers ----
private const val ROUTE_EXPORT = "export/{reportId}"
private fun routeExport(reportId: Long) = "export/$reportId"

// ---- Optional: put your repo URL here (or inject from BuildConfig) ----
private const val GITHUB_URL: String = "https://github.com/NathanCurran419/CUMA"

@Composable
fun AppNav(
    reports: List<Report>,
    onInsertReport: (Report, List<SpeciesCount>, List<Photo>) -> Unit,
    onDeleteReport: (Report) -> Unit = {},
    onLoadSpecies: suspend (Long) -> List<SpeciesCount> = { emptyList() },
    onLoadPhotos: suspend (Long) -> List<Photo> = { emptyList() }
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {

        // --- Home screen with top bar + About sheet ---
        composable("home") {
            var showAbout by remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    AppTopBar(onAboutClick = { showAbout = true })
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    HomeScreen(
                        reports = reports,
                        onAddNew = { navController.navigate("reportForm") },
                        onSelectReport = { report ->
                            navController.navigate("reportDetail/${report.id}")
                        }
                    )
                }
            }

            if (showAbout) {
                AboutSheet(
                    onDismiss = { showAbout = false },
                    appPurpose = "Intended to provide a digital platform for completing cave biological and use monitoring for the Cave Research Foundation Ozark Operations and the Missouri Speleological Survey.",
                    author = "Built by Nathan Curran",
                    githubUrl = GITHUB_URL
                )
            }
        }

        // --- Report form screen (new report) ---
        composable("reportForm") {
            ReportFormScreen(
                existingReport = null,
                existingSpecies = emptyList(),
                existingPhotos = emptyList(),
                onBack = { navController.popBackStack() },
                onSubmit = { report, species, photos ->
                    onInsertReport(report, species, photos)
                    navController.popBackStack("home", inclusive = false)
                },
                onBackToHome = { navController.popBackStack("home", inclusive = false) }
            )
        }

        // --- Report form screen (edit report) ---
        composable(
            route = "reportForm/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getLong("reportId")
            val report = reports.find { it.id == reportId }

            // Load species & photos asynchronously
            val species by produceState(initialValue = emptyList<SpeciesCount>(), reportId) {
                value = if (reportId != null) onLoadSpecies(reportId) else emptyList()
            }
            val photos by produceState(initialValue = emptyList<Photo>(), reportId) {
                value = if (reportId != null) onLoadPhotos(reportId) else emptyList()
            }

            ReportFormScreen(
                existingReport = report,
                existingSpecies = species,
                existingPhotos = photos,
                onBack = { navController.popBackStack() },
                onSubmit = { updated, speciesUpdated, photosUpdated ->
                    onInsertReport(updated, speciesUpdated, photosUpdated)
                    navController.popBackStack("home", inclusive = false)
                },
                onBackToHome = { navController.popBackStack("home", inclusive = false) }
            )
        }

        // --- Report detail screen ---
        composable(
            route = "reportDetail/{reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getLong("reportId")
            val report = reports.find { it.id == reportId }

            // Load species & photos asynchronously (same pattern you used for edit)
            val species by produceState(initialValue = emptyList<SpeciesCount>(), reportId) {
                value = if (reportId != null) onLoadSpecies(reportId) else emptyList()
            }
            val photos by produceState(initialValue = emptyList<Photo>(), reportId) {
                value = if (reportId != null) onLoadPhotos(reportId) else emptyList()
            }

            report?.let {
                ReportDetailScreen(
                    report = it,
                    speciesCounts = species,
                    photos = photos,
                    onBack = { navController.popBackStack() },
                    onDelete = { r ->
                        onDeleteReport(r)
                        navController.popBackStack("home", inclusive = false)
                    },
                    onEdit = { r -> navController.navigate("reportForm/${r.id}") },
                    onExport = { r -> navController.navigate("export/${r.id}") }
                )
            }
        }

        // --- Export dialog destination (modal over current screen) ---
        dialog(
            route = ROUTE_EXPORT,
            arguments = listOf(navArgument("reportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments!!.getLong("reportId")
            val app = LocalContext.current.applicationContext as Application

            com.example.crfcavemonitor.export.ExportDialogRoute(
                reportId = reportId,
                app = app,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(onAboutClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CRF Cave Monitor",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        actions = {
            TextButton(onClick = onAboutClick) {
                Text("About")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // background tint
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSheet(
    onDismiss: () -> Unit,
    appPurpose: String,
    author: String,
    githubUrl: String
) {
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "About CRF Cave Monitor",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = appPurpose,
                style = MaterialTheme.typography.bodyMedium
            )

            Divider(Modifier.padding(vertical = 10.dp))

            Text(
                text = author,
                style = MaterialTheme.typography.bodyMedium
            )

            Divider(Modifier.padding(vertical = 10.dp))

            Text(
                text = "GitHub Repository",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = githubUrl,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .clickable { uriHandler.openUri(githubUrl) }
                    .padding(top = 2.dp)
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close", textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}