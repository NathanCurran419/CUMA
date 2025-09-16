package com.example.crfcavemonitor.repository

import android.content.Context
import android.net.Uri
import com.example.crfcavemonitor.data.*
import java.io.File

class ReportRepository(
    private val reportDao: ReportDao,
    private val speciesDao: SpeciesCountDao,
    private val photoDao: PhotoDao
) {
    // --- Reports ---
    fun getAllReports() = reportDao.getReportsByDateDesc()

    suspend fun insertReport(report: Report): Long = reportDao.insert(report)
    suspend fun updateReport(report: Report) = reportDao.update(report)
    suspend fun deleteReport(report: Report) = reportDao.delete(report)

    // --- Species ---
    suspend fun getSpeciesCounts(reportId: Long): List<SpeciesCount> =
        speciesDao.getByReport(reportId)

    suspend fun insertSpeciesCounts(species: List<SpeciesCount>) =
        speciesDao.insertAll(species)

    // --- Photos ---
    suspend fun getPhotos(reportId: Long): List<Photo> =
        photoDao.getByReport(reportId)

    suspend fun insertPhoto(photo: Photo) = photoDao.insert(photo)

    suspend fun deletePhoto(photo: Photo) = photoDao.delete(photo)

    suspend fun getValidPhotos(reportId: Long, context: Context): List<Photo> {
        val photos = photoDao.getByReport(reportId)
        val validPhotos = photos.filter { photo ->
            val uri = Uri.parse(photo.uri)
            when (uri.scheme) {
                "file" -> {
                    val file = File(uri.path ?: return@filter false)
                    file.exists()
                }
                "content" -> {
                    try {
                        context.contentResolver.openInputStream(uri)?.close()
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                else -> false
            }
        }

        val removed = photos - validPhotos.toSet()
        removed.forEach { photoDao.delete(it) }

        return validPhotos
    }

    suspend fun deleteReportCascade(report: Report) {
        val species = speciesDao.getByReport(report.id)
        species.forEach { speciesDao.delete(it) }

        val photos = photoDao.getByReport(report.id)
        photos.forEach { photoDao.delete(it) }

        reportDao.delete(report)
    }
}