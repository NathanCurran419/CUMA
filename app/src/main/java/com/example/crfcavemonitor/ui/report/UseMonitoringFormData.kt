// File: ui.report/UseMonitoringFormData.kt
package com.example.crfcavemonitor.ui.report

data class UseMonitoringFormData(
    val publicUseStates: Map<String, String> = listOf(
        "Visitation",
        "Litter/Trash",
        "Speleothem Vandalism",
        "Graffiti",
        "Archeological Looting",
        "Fires",
        "Camping"
    ).associateWith { "None" },
    val currentDisturbance: String = "None",
    val potentialDisturbance: String = "None",
    val managementConsiderations: String = "",
    val recommendations: String = "",
    val otherComments: String = ""
)