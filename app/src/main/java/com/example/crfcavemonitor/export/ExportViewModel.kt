// export/ExportViewModel.kt

package com.example.crfcavemonitor.export

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExportViewModel(app: Application, private val reportId: Long) : AndroidViewModel(app) {
    private val repo = ExportRepository(app.applicationContext)

    private val _reportName = MutableStateFlow("Report")
    val reportName: StateFlow<String> = _reportName

    private val _payload = MutableStateFlow<ExportPayload?>(null)
    val payload: StateFlow<ExportPayload?> = _payload

    init {
        viewModelScope.launch {
            val data = repo.buildPayload(reportId)
            _payload.value = data
            _reportName.value = data.reportName
        }
    }
}