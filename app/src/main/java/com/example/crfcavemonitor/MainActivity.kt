package com.example.crfcavemonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crfcavemonitor.data.AppDatabase
import com.example.crfcavemonitor.data.Photo
import com.example.crfcavemonitor.data.Report
import com.example.crfcavemonitor.data.SpeciesCount
import com.example.crfcavemonitor.repository.ReportRepository
import com.example.crfcavemonitor.ui.AppNav
import com.example.crfcavemonitor.ui.theme.CRFCaveMonitorTheme
import com.example.crfcavemonitor.viewmodel.HomeViewModel
import com.example.crfcavemonitor.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val reportRepository = ReportRepository(
            db.reportDao(),
            db.speciesCountDao(),
            db.photoDao()
        )
        val homeViewModelFactory = HomeViewModelFactory(reportRepository)

        setContent {
            CRFCaveMonitorTheme {
                val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)

                AppNav(
                    reports = homeViewModel.reports.collectAsState().value,

                    // Insert or update report with species + photos
                    onInsertReport = { report: Report, species: List<SpeciesCount>, photos: List<Photo> ->
                        lifecycleScope.launch {
                            val reportId = reportRepository.insertReport(report)

                            // Update species
                            val updatedSpecies = species.map { it.copy(reportId = reportId) }
                            reportRepository.insertSpeciesCounts(updatedSpecies)

                            // Sync photos
                            val existingPhotos = reportRepository.getPhotos(reportId)

                            // Delete photos that were removed in the UI
                            val toDelete = existingPhotos.filter { old ->
                                photos.none { new -> new.uri == old.uri && new.caption == old.caption }
                            }
                            toDelete.forEach { reportRepository.deletePhoto(it) }

                            // Insert/update remaining photos
                            val updatedPhotos = photos.map { it.copy(reportId = reportId) }
                            updatedPhotos.forEach { reportRepository.insertPhoto(it) }
                        }
                    },

                    // Cascade delete
                    onDeleteReport = { report ->
                        lifecycleScope.launch {
                            reportRepository.deleteReportCascade(report)
                        }
                    },

                    // Load species for editing
                    onLoadSpecies = { reportId ->
                        reportRepository.getSpeciesCounts(reportId)
                    },

                    // Load photos safely (with file/URI validation)
                    onLoadPhotos = { reportId ->
                        reportRepository.getValidPhotos(reportId, this@MainActivity)
                    }
                )
            }
        }
    }
}