package com.example.sitamu.presentation.guest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitamu.data.remote.GuestVisitDto
import com.example.sitamu.data.repository.OnlineGuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import android.util.Log

class OnlineGuestViewModel(
    private val repository: OnlineGuestRepository
) : ViewModel() {

    companion object {
        private const val TAG = "OnlineGuest"
    }

    private val _pendingGuests =
        MutableStateFlow<List<GuestVisitDto>>(emptyList())

    val pendingGuests: StateFlow<List<GuestVisitDto>> =
        _pendingGuests.asStateFlow()

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    private val _processingGuestId = MutableStateFlow<String?>(null)

    val processingGuestId: StateFlow<String?> =
        _processingGuestId.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()

    private val _actionErrorMessage = MutableStateFlow<String?>(null)

    val actionErrorMessage: StateFlow<String?> =
        _actionErrorMessage.asStateFlow()

    init {
        refreshPendingGuests()
    }

    fun refreshPendingGuests(): Job {
        return viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Mulai mengambil data permintaan tamu online.")

            try {
                val guests = withTimeout(15_000) {
                    repository.getPendingGuestVisits()
                }
                _pendingGuests.value = guests
                Log.d(TAG, "Berhasil menerima ${guests.size} data tamu online.")

            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Timeout saat mengambil data tamu online.", e)
                _errorMessage.value =
                    "Koneksi ke server terlalu lama. Silakan coba lagi."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error Supabase saat mengambil data tamu online.", e)
                _errorMessage.value =
                    e.message ?: "Gagal mengambil data tamu online."

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveGuest(id: String) {
        updateGuest(id) {
            repository.approveGuestVisit(id)

            try {
                repository.sendGuestWhatsapp(id)
                "Tamu diterima dan WhatsApp berhasil dikirim."
            } catch (e: Exception) {
                "Tamu diterima, tetapi WhatsApp gagal dikirim."
            }
        }
    }

    fun rejectGuest(id: String) {
        updateGuest(id) {
            repository.rejectGuestVisit(id)
            "Tamu berhasil ditolak."
        }
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun clearActionErrorMessage() {
        _actionErrorMessage.value = null
    }

    private fun updateGuest(
        id: String,
        update: suspend () -> String
    ) {
        if (_processingGuestId.value != null) return

        _processingGuestId.value = id
        _successMessage.value = null
        _actionErrorMessage.value = null

        viewModelScope.launch {
            try {
                val resultMessage = update()
                _successMessage.value = resultMessage
                refreshPendingGuests().join()
            } catch (e: Exception) {
                _actionErrorMessage.value = "Gagal memperbarui status tamu."
            } finally {
                _processingGuestId.value = null
            }
        }
    }
}
