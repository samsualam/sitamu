package com.example.sitamu.presentation.destination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitamu.data.remote.DestinationContact
import com.example.sitamu.data.repository.DestinationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class DestinationDraft(
    val id: Long? = null,
    val name: String,
    val division: String,
    val whatsapp: String,
    val active: Boolean
)

data class DestinationFormErrors(
    val name: String? = null,
    val division: String? = null,
    val whatsapp: String? = null
) {
    val hasAny: Boolean get() = name != null || division != null || whatsapp != null
}

data class DestinationUiState(
    val destinations: List<DestinationContact> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val isSaving: Boolean = false,
    val changingActiveId: Long? = null,
    val formErrors: DestinationFormErrors = DestinationFormErrors()
)

data class DestinationMessage(
    val text: String,
    val closeEditor: Boolean = false
)

class DestinationViewModel(
    private val repository: DestinationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DestinationUiState())
    val uiState: StateFlow<DestinationUiState> = _uiState.asStateFlow()

    private val _message = MutableStateFlow<DestinationMessage?>(null)
    val message: StateFlow<DestinationMessage?> = _message.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadError = null)
            try {
                _uiState.value = _uiState.value.copy(
                    destinations = withTimeout(15_000L) { repository.getDestinations() }
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    loadError = "Gagal memuat tujuan kunjungan. Silakan coba lagi."
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun save(draft: DestinationDraft) {
        if (_uiState.value.isSaving) return
        val normalized = draft.copy(
            name = draft.name.trim(),
            division = draft.division.trim(),
            whatsapp = draft.whatsapp.trim()
        )
        val errors = validate(normalized)
        _uiState.value = _uiState.value.copy(formErrors = errors)
        if (errors.hasAny) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                if (normalized.id == null) {
                    repository.createDestination(normalized.name, normalized.division, normalized.whatsapp, normalized.active)
                } else {
                    repository.updateDestination(normalized.id, normalized.name, normalized.division, normalized.whatsapp, normalized.active)
                }
                _uiState.value = _uiState.value.copy(formErrors = DestinationFormErrors())
                _message.value = DestinationMessage(
                    text = if (normalized.id == null) "Tujuan kunjungan berhasil ditambahkan." else "Tujuan kunjungan berhasil diperbarui.",
                    closeEditor = true
                )
                refresh()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _message.value = DestinationMessage("Gagal menyimpan tujuan kunjungan.")
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun setActive(destination: DestinationContact, active: Boolean) {
        if (_uiState.value.changingActiveId != null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(changingActiveId = destination.destination.id)
            try {
                repository.setDestinationActive(destination.destination.id, active)
                _message.value = DestinationMessage(
                    if (active) "Tujuan kunjungan berhasil diaktifkan kembali." else "Tujuan kunjungan berhasil dinonaktifkan."
                )
                refresh()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _message.value = DestinationMessage("Gagal mengubah status tujuan kunjungan.")
            } finally {
                _uiState.value = _uiState.value.copy(changingActiveId = null)
            }
        }
    }

    fun consumeMessage() { _message.value = null }

    private fun validate(draft: DestinationDraft) = DestinationFormErrors(
        name = if (draft.name.isBlank()) "Nama tujuan wajib diisi." else null,
        division = if (draft.division.isBlank()) "Bagian/divisi wajib diisi." else null,
        whatsapp = when {
            draft.whatsapp.isBlank() -> "Nomor WhatsApp wajib diisi."
            !WHATSAPP_PATTERN.matches(draft.whatsapp) -> "Nomor WhatsApp hanya boleh berisi angka, dengan + di awal bila diperlukan."
            else -> null
        }
    )

    private companion object {
        val WHATSAPP_PATTERN = Regex("^\\+?\\d+$")
    }
}
