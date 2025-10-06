package com.example.crfcavemonitor.export

import android.content.Context
import com.example.crfcavemonitor.data.AppDatabase
import com.example.crfcavemonitor.data.PhotoDao
import com.example.crfcavemonitor.data.ReportDao
import com.example.crfcavemonitor.data.SpeciesCountDao
import java.text.SimpleDateFormat
import java.util.Locale

class ExportRepository(
    private val reportDao: ReportDao,
    private val speciesDao: SpeciesCountDao,
    private val photoDao: PhotoDao
) {
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateTimeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    suspend fun buildPayload(reportId: Long): ExportPayload {
        // Direct DAO call now that getById is implemented
        val report = reportDao.getById(reportId)

        val visitRow = mapOf(
            "mssAcc" to report.mssAcc,
            "caveName" to report.caveName,
            "ownerUnit" to report.ownerUnit,
            "monitorDate" to dateFmt.format(report.monitorDate),
            "rationale" to report.rationale,
            "areaMonitored" to report.areaMonitored,
            "organization" to report.organization,
            "monitoredBy" to report.monitoredBy,
            "location" to report.location
        )

        val useRow = mapOf(
            "visitation" to report.visitation,
            "litter" to report.litter,
            "speleothemVandalism" to report.speleothemVandalism,
            "graffiti" to report.graffiti,
            "archaeologicalLooting" to report.archaeologicalLooting,
            "fires" to report.fires,
            "camping" to report.camping,
            "currentDisturbance" to report.currentDisturbance,
            "potentialDisturbance" to report.potentialDisturbance,
            "manageConsiderations" to report.manageConsiderations,
            "otherComments" to report.otherComments
        )

        val bioRows = speciesDao.getByReport(reportId).map { s ->
            mapOf(
                "speciesName" to s.speciesName,
                "count" to s.count.toString(),
                "notes" to s.notes
            )
        }

        val photoRows = photoDao.getByReport(reportId).map { p ->
            mapOf(
                "uri" to p.uri,
                "caption" to p.caption,
                "timestamp" to dateTimeFmt.format(p.timestamp)
            )
        }

        val reportName = "${report.caveName} ${dateFmt.format(report.monitorDate)}"

        return ExportPayload(
            reportId = reportId,
            reportName = reportName,
            visitRows = listOf(visitRow),
            bioRows = bioRows,
            photoRows = photoRows,
            useRows = listOf(useRow)
        )
    }
}

/**
 * to build ExportRepository with the AppDatabase instance.
 */
fun ExportRepository(context: Context): ExportRepository {
    val db = AppDatabase.getDatabase(context)
    return ExportRepository(db.reportDao(), db.speciesCountDao(), db.photoDao())
}
