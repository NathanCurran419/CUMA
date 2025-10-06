// data/Report.kt

package com.example.crfcavemonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mssAcc: String,
    val caveName: String,
    val ownerUnit: String,
    val monitorDate: Date,
    val rationale: String,
    val areaMonitored: String,
    val organization: String,
    val monitoredBy: String,
    val visitation: String,
    val litter: String,
    val speleothemVandalism: String,
    val graffiti: String,
    val archaeologicalLooting: String,
    val fires: String,
    val camping: String,
    val currentDisturbance: String,
    val potentialDisturbance: String,
    val manageConsiderations: String,
    val recommendations: String,
    val otherComments: String,
    val location: String
)

