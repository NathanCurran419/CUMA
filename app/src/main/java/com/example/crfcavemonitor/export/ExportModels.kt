// export/ExportModels.kt

package com.example.crfcavemonitor.export

enum class ExportSection { ALL, VISIT, BIO, PHOTOS, USE }
enum class ExportFormat {
    CSV_USE_MONITORING,
    CSV_BIO_MONITORING,
    PDF
}

data class ExportSelection(
    val sections: Set<ExportSection>,
    val format: ExportFormat,
    val csvFormat: CsvFormat = CsvFormat.MCD_USE_MONITORING
)

enum class CsvFormat {
    MCD_USE_MONITORING,
    BIO_MONITORING
}

data class ExportPayload(
    val reportId: Long,
    val reportName: String,
    val visitRows: List<Map<String, String>> = emptyList(),
    val bioRows: List<Map<String, String>> = emptyList(),
    val photoRows: List<Map<String, String>> = emptyList(),
    val useRows: List<Map<String, String>> = emptyList()
)
