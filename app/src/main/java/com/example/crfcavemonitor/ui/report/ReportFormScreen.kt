// File: ui.report/HomeScreen.kt  (contains ReportFormScreen)
package com.example.crfcavemonitor.ui.report

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.crfcavemonitor.R
import com.example.crfcavemonitor.data.Photo
import com.example.crfcavemonitor.data.Report
import com.example.crfcavemonitor.data.SpeciesCount
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    existingReport: Report? = null,
    existingSpecies: List<SpeciesCount> = emptyList(),
    existingPhotos: List<Photo> = emptyList(),
    onBack: () -> Unit,
    onSubmit: (Report, List<SpeciesCount>, List<Photo>) -> Unit,
    onBackToHome: () -> Unit
) {
    var step by remember { mutableStateOf(0) }

    // Visit details
    var visitDetails by remember {
        mutableStateOf(
            if (existingReport != null) {
                VisitDetailsFormData(
                    mssAcc = existingReport.mssAcc,
                    caveName = existingReport.caveName,
                    ownerUnit = existingReport.ownerUnit,
                    monitorDate = existingReport.monitorDate,
                    rationale = existingReport.rationale,
                    areaMonitored = existingReport.areaMonitored,
                    organization = existingReport.organization,
                    monitoredBy = existingReport.monitoredBy,
                    location = existingReport.location
                )
            } else {
                VisitDetailsFormData("", "", "", Date(), "", "", "", "", "")
            }
        )
    }

    // Species list
    var speciesList by remember { mutableStateOf(existingSpecies) }
    LaunchedEffect(existingSpecies) { speciesList = existingSpecies }

    // Photo list (kept in Compose state)
    var photoList by remember { mutableStateOf(existingPhotos.map { PhotoItem(it.uri.toUri(), it.caption) }) }
    LaunchedEffect(existingPhotos) { photoList = existingPhotos.map { PhotoItem(it.uri.toUri(), it.caption) } }

    // Use / Impact data
    var useMonitoringData by remember {
        mutableStateOf(
            if (existingReport != null) {
                UseMonitoringFormData(
                    publicUseStates = mapOf(
                        "Visitation" to existingReport.visitation,
                        "Litter/Trash" to existingReport.litter,
                        "Speleothem Vandalism" to existingReport.speleothemVandalism,
                        "Graffiti" to existingReport.graffiti,
                        "Archeological Looting" to existingReport.archaeologicalLooting,
                        "Fires" to existingReport.fires,
                        "Camping" to existingReport.camping
                    ),
                    currentDisturbance = existingReport.currentDisturbance,
                    potentialDisturbance = existingReport.potentialDisturbance,
                    managementConsiderations = existingReport.manageConsiderations,
                    recommendations = "",
                    otherComments = existingReport.otherComments
                )
            } else UseMonitoringFormData()
        )
    }

    // 🔔 Dialog state
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }
    var showMissingCaveName by remember { mutableStateOf(false) } // << NEW: missing cave name dialog

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Home (confirm exit without saving)
                NavigationBarItem(
                    selected = false,
                    onClick = { showExitConfirm = true },
                    icon = { Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Home") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = step == 0,
                    onClick = { step = 0 },
                    icon = { Icon(painterResource(id = R.drawable.ic_cave_info), contentDescription = "Visit Info") },
                    label = { Text("Visit") }
                )
                NavigationBarItem(
                    selected = step == 1,
                    onClick = { step = 1 },
                    icon = { Icon(painterResource(id = R.drawable.ic_bio_inventory), contentDescription = "Bio") },
                    label = { Text("Bio") }
                )
                NavigationBarItem(
                    selected = step == 2,
                    onClick = { step = 2 },
                    icon = { Icon(painterResource(id = R.drawable.ic_photo), contentDescription = "Photo") },
                    label = { Text("Photos") }
                )
                NavigationBarItem(
                    selected = step == 3,
                    onClick = { step = 3 },
                    icon = { Icon(painterResource(id = R.drawable.ic_use_monitoring), contentDescription = "Use") },
                    label = { Text("Use") }
                )

                // Save: validate Cave Name before showing confirm dialog
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        if (visitDetails.caveName.isBlank()) {
                            showMissingCaveName = true
                        } else {
                            showSaveConfirm = true
                        }
                    },
                    icon = { Icon(painterResource(id = R.drawable.ic_save_form), contentDescription = "Save") },
                    label = { Text("Save") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (step) {
                0 -> VisitDetailsStep(
                    onDetailsChanged = { visitDetails = it },
                    initialData = visitDetails
                )
                1 -> BioInventoryStep(
                    speciesList = speciesList,
                    onSpeciesListChanged = { speciesList = it }
                )
                2 -> PhotoStepComponent(
                    photoList = photoList,
                    onPhotoListChanged = { updated -> photoList = updated }
                )
                3 -> ImpactAssessmentStep(
                    formData = useMonitoringData,
                    onFormDataChanged = { useMonitoringData = it }
                )
            }
        }
    }

    // ============================
    //          Dialogs
    // ============================

    // Exit to Home (without saving)
    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Exit to Home") },
            text = { Text("Are you sure you want to exit to home without saving?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        onBackToHome()
                    }
                ) { Text("Exit") }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // 🚫 Missing Cave Name
    if (showMissingCaveName) {
        AlertDialog(
            onDismissRequest = { showMissingCaveName = false },
            title = { Text("Missing Cave Name") },
            text = { Text("Must enter a cave name to save") },
            confirmButton = {
                TextButton(onClick = { showMissingCaveName = false }) {
                    Text("Okay")
                }
            }
        )
    }

    // Save Report (only shown if cave name is not blank)
    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("Save Report") },
            text = { Text("Are you sure you want to save report?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Build Report from current form state
                        val report = Report(
                            id = existingReport?.id ?: 0,
                            mssAcc = visitDetails.mssAcc,
                            caveName = visitDetails.caveName,
                            ownerUnit = visitDetails.ownerUnit,
                            monitorDate = visitDetails.monitorDate,
                            rationale = visitDetails.rationale,
                            areaMonitored = visitDetails.areaMonitored,
                            organization = visitDetails.organization,
                            monitoredBy = visitDetails.monitoredBy,
                            visitation = useMonitoringData.publicUseStates["Visitation"] ?: "None",
                            litter = useMonitoringData.publicUseStates["Litter/Trash"] ?: "None",
                            speleothemVandalism = useMonitoringData.publicUseStates["Speleothem Vandalism"] ?: "None",
                            graffiti = useMonitoringData.publicUseStates["Graffiti"] ?: "None",
                            archaeologicalLooting = useMonitoringData.publicUseStates["Archeological Looting"] ?: "None",
                            fires = useMonitoringData.publicUseStates["Fires"] ?: "None",
                            camping = useMonitoringData.publicUseStates["Camping"] ?: "None",
                            currentDisturbance = useMonitoringData.currentDisturbance,
                            potentialDisturbance = useMonitoringData.potentialDisturbance,
                            manageConsiderations = useMonitoringData.managementConsiderations,
                            otherComments = useMonitoringData.otherComments,
                            location = visitDetails.location
                        )

                        // Preserve Species IDs
                        val speciesWithIds = speciesList.map { sp ->
                            sp.copy(id = sp.id, reportId = report.id, notes = sp.notes)
                        }

                        // Photos from current state, preserve IDs/timestamps if existing
                        val photosWithIds = photoList.map { item ->
                            val existing = existingPhotos.find {
                                it.uri == item.uri.toString() && it.caption == item.caption
                            }
                            Photo(
                                id = existing?.id ?: 0,
                                reportId = report.id,
                                uri = item.uri.toString(),
                                caption = item.caption,
                                timestamp = existing?.timestamp ?: Date()
                            )
                        }

                        onSubmit(report, speciesWithIds, photosWithIds)
                        showSaveConfirm = false
                        onBackToHome()
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
