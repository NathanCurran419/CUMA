package com.example.crfcavemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.crfcavemonitor.data.*
import com.example.crfcavemonitor.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    private val _reportId = MutableStateFlow<Long?>(null)
    val reportId: StateFlow<Long?> get() = _reportId

    fun saveReport(report: Report) {
        viewModelScope.launch {
            _reportId.value = repository.insertReport(report)
        }
    }

    fun updateReport(report: Report) {
        viewModelScope.launch {
            repository.updateReport(report)
        }
    }

    fun saveSpecies(species: List<SpeciesCount>) {
        viewModelScope.launch {
            repository.insertSpeciesCounts(species)
        }
    }

    fun savePhoto(photo: Photo) {
        viewModelScope.launch {
            repository.insertPhoto(photo)
        }
    }
}

class ReportViewModelFactory(private val repository: ReportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}