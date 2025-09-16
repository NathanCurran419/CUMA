package com.example.crfcavemonitor.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.crfcavemonitor.data.Photo
import com.example.crfcavemonitor.data.Report
import com.example.crfcavemonitor.data.SpeciesCount
import com.example.crfcavemonitor.ui.home.HomeScreen
import com.example.crfcavemonitor.ui.report.ReportDetailScreen
import com.example.crfcavemonitor.ui.report.ReportFormScreen

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
        // --- Home screen ---
        composable("home") {
            HomeScreen(
                reports = reports,
                onAddNew = { navController.navigate("reportForm") },
                onSelectReport = { report ->
                    navController.navigate("reportDetail/${report.id}")
                }
            )
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

            report?.let {
                ReportDetailScreen(
                    report = it,
                    onBack = { navController.popBackStack() },
                    onDelete = { r ->
                        onDeleteReport(r)
                        navController.popBackStack("home", inclusive = false)
                    },
                    onEdit = { r ->
                        navController.navigate("reportForm/${r.id}")
                    },
                    onExport = { _ ->
                        // TODO: implement PDF export later
                    }
                )
            }
        }
    }
}