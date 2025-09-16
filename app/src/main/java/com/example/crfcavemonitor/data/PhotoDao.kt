package com.example.crfcavemonitor.data

import androidx.room.*

@Dao
interface PhotoDao {
    @Query("SELECT * FROM Photo WHERE reportId = :reportId")
    suspend fun getByReport(reportId: Long): List<Photo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)
}