// File: ui.report/VisitDetailsFormData.kt
package com.example.crfcavemonitor.ui.report

import java.util.Date

data class VisitDetailsFormData(
    val mssAcc: String,
    val caveName: String,
    val owner: String,
    val unit: String,
    val monitorDate: Date,
    val rationale: String,
    val areaMonitored: String,
    val organization: String,
    val monitoredBy: String,
    val location: String = ""
)