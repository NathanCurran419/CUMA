// File: ui.report/ImpactAssessmentStep.kt
package com.example.crfcavemonitor.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImpactAssessmentStep(
    formData: UseMonitoringFormData,
    onFormDataChanged: (UseMonitoringFormData) -> Unit
) {
    val publicUseLevels = listOf("None", "Some", "Many/Much")
    val disturbanceLevels = listOf("None", "Little", "Moderate", "High")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // allow scrolling
    ) {
        // --- Public Use ---
        Text("Public Use:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        formData.publicUseStates.keys.forEach { category ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(category, style = MaterialTheme.typography.bodyLarge)
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    publicUseLevels.forEach { level ->
                        AssistChip(
                            onClick = {
                                val updated = formData.publicUseStates
                                    .toMutableMap()
                                    .also { it[category] = level }
                                onFormDataChanged(formData.copy(publicUseStates = updated))
                            },
                            label = { Text(level) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors =
                                if (formData.publicUseStates[category] == level) {
                                    AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                } else {
                                    AssistChipDefaults.assistChipColors()
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Disturbance ---
        Text("Current Disturbance:", style = MaterialTheme.typography.bodyLarge)
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            disturbanceLevels.forEach { level ->
                AssistChip(
                    onClick = { onFormDataChanged(formData.copy(currentDisturbance = level)) },
                    label = { Text(level) },
                    modifier = Modifier.padding(end = 8.dp),
                    colors =
                        if (formData.currentDisturbance == level) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        }
                )
            }
        }

        Text("Potential Disturbance:", style = MaterialTheme.typography.bodyLarge)
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            disturbanceLevels.forEach { level ->
                AssistChip(
                    onClick = { onFormDataChanged(formData.copy(potentialDisturbance = level)) },
                    label = { Text(level) },
                    modifier = Modifier.padding(end = 8.dp),
                    colors =
                        if (formData.potentialDisturbance == level) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        } else {
                            AssistChipDefaults.assistChipColors()
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Summary Fields ---
        Text("Summary:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formData.managementConsiderations,
            onValueChange = { onFormDataChanged(formData.copy(managementConsiderations = it)) },
            label = { Text("Management Considerations") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = false,
            maxLines = 6
        )

        OutlinedTextField(
            value = formData.recommendations,
            onValueChange = { onFormDataChanged(formData.copy(recommendations = it)) },
            label = { Text("Recommendations") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = false,
            maxLines = 6
        )

        OutlinedTextField(
            value = formData.otherComments,
            onValueChange = { onFormDataChanged(formData.copy(otherComments = it)) },
            label = { Text("Other Comments") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = false,
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}
