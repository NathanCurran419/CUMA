// data/SpeciesCount.kt

package com.example.crfcavemonitor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SpeciesCount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    @ColumnInfo(name = "speciesId")
    val speciesId: Long?,
    val speciesName: String,
    val count: Int,
    val notes: String
)
