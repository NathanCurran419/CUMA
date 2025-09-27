// data/Report.kt

package com.example.crfcavemonitor.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM Report ORDER BY monitorDate DESC")
    fun getReportsByDateDesc(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: Report): Long

    @Update
    suspend fun update(report: Report)

    @Delete
    suspend fun delete(report: Report)

    @Query("SELECT * FROM Report WHERE id = :id")
    suspend fun getById(id: Long): Report
}