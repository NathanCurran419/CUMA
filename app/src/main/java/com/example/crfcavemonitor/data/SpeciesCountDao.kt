// data/SpeciesCountDao.kt

package com.example.crfcavemonitor.data

import androidx.room.*

@Dao
interface SpeciesCountDao {
    @Query("SELECT * FROM SpeciesCount WHERE reportId = :reportId")
    suspend fun getByReport(reportId: Long): List<SpeciesCount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(species: List<SpeciesCount>)

    @Delete
    suspend fun delete(species: SpeciesCount)
}