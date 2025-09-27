// data/Photo.kt

package com.example.crfcavemonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val uri: String,
    val caption: String,
    val timestamp: Date
)