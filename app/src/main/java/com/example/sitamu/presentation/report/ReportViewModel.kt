package com.example.sitamu.presentation.report

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitamu.data.model.GuestVisitEntity
import com.example.sitamu.data.repository.GuestRepository
import com.example.sitamu.utils.CsvExporter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportViewModel(private val guestRepository: GuestRepository) : ViewModel() {
    var isExporting by mutableStateOf(false)
        private set
    var exportMessage by mutableStateOf<String?>(null)
        private set
    var exportFailed by mutableStateOf(false)
        private set
    private var pendingVisits: List<GuestVisitEntity>? = null
    
    val allVisits: StateFlow<List<GuestVisitEntity>> = guestRepository.allGuestVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCsvData(): String {
        return CsvExporter.generateCsvString(allVisits.value)
    }

    fun prepareExport() {
        pendingVisits = allVisits.value.toList()
        isExporting = true
        exportMessage = null
        exportFailed = false
    }

    fun exportTo(resolver: ContentResolver, uri: Uri?) {
        val visits = pendingVisits
        pendingVisits = null
        if (uri == null || visits == null) {
            isExporting = false
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val stream = resolver.openOutputStream(uri, "wt")
                        ?: error("File tidak dapat dibuka.")
                    stream.bufferedWriter(Charsets.UTF_8).use {
                        it.write(CsvExporter.generateCsvString(visits))
                    }
                }
                exportMessage = "Berhasil menyimpan ${visits.size} data ke file CSV."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                exportFailed = true
                exportMessage = "File CSV gagal disimpan. Silakan pilih lokasi lain."
            } finally {
                isExporting = false
            }
        }
    }
}
