package com.example.crfcavemonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SpeciesCount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val speciesName: String,
    val count: Int,
    val tentative: Boolean,
    val notes: String
)
