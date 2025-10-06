// export/ExportModels.kt

package com.example.crfcavemonitor.export

enum class ExportSection { ALL, VISIT, BIO, PHOTOS, USE }
enum class ExportFormat { CSV, PDF }

data class ExportSelection(
    val sections: Set<ExportSection>,
    val format: ExportFormat
)

data class ExportPayload(
    val reportId: Long,
    val reportName: String,
    val visitRows: List<Map<String, String>> = emptyList(),
    val bioRows: List<Map<String, String>> = emptyList(),
    val photoRows: List<Map<String, String>> = emptyList(),
    val useRows: List<Map<String, String>> = emptyList()
)
