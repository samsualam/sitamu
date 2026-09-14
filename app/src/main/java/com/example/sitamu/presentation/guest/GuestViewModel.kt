package com.example.sitamu.presentation.guest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitamu.data.model.GuestVisitEntity
import com.example.sitamu.data.repository.GuestRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class GuestViewModel(private val guestRepository: GuestRepository) : ViewModel() {
    
    // List & Search/Filter States
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("") // Empty means all
    var filterPeriod by mutableStateOf("Semua") // Semua, Hari ini, Kemarin, Minggu ini, Bulan ini, Custom
    var startTs by mutableStateOf(0L)
    var endTs by mutableStateOf(Long.MAX_VALUE)

    private val _triggerFetch = MutableStateFlow(0L)

    val guestList: StateFlow<List<GuestVisitEntity>> = _triggerFetch
        .flatMapLatest {
            calculateTimestamps()
            guestRepository.searchAndFilterGuests(searchQuery, selectedCategory, startTs, endTs)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form States
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var institutionCategory by mutableStateOf("")
    var institutionName by mutableStateOf("")
    var address by mutableStateOf("")
    var purpose by mutableStateOf("")
    var personToMeet by mutableStateOf("")
    var photoUri by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    private val _operationSuccess = Channel<Boolean>(Channel.BUFFERED)
    val operationSuccess = _operationSuccess.receiveAsFlow()
    private var formInitialized = false

    fun initializeForm(id: Long?) {
        if (formInitialized) return
        formInitialized = true
        if (id != null && id != 0L) loadGuestForEdit(id)
    }

    fun refreshList() {
        _triggerFetch.value += 1
    }

    private fun calculateTimestamps() {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        when (filterPeriod) {
            "Hari ini" -> {
                startTs = todayStart
                endTs = todayEnd
            }
            "Kemarin" -> {
                startTs = todayStart - 24 * 60 * 60 * 1000
                endTs = todayEnd - 24 * 60 * 60 * 1000
            }
            "Minggu ini" -> {
                calendar.timeInMillis = todayStart
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                startTs = calendar.timeInMillis
                endTs = todayEnd
            }
            "Bulan ini" -> {
                calendar.timeInMillis = todayStart
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startTs = calendar.timeInMillis
                endTs = todayEnd
            }
            "Custom" -> {
                // Kept as set manually or unchanged
            }
            else -> {
                startTs = 0L
                endTs = Long.MAX_VALUE
            }
        }
    }

    fun clearForm() {
        name = ""
        phone = ""
        institutionCategory = ""
        institutionName = ""
        address = ""
        purpose = ""
        personToMeet = ""
        photoUri = null
        errorMessage = null
        successMessage = null
    }

    fun loadGuestForEdit(id: Long) {
        isLoading = true
        viewModelScope.launch {
            try {
                val guest = guestRepository.getGuestVisitById(id)
                    ?: error("Data tamu tidak ditemukan.")
                name = guest.name
                phone = guest.phone
                institutionCategory = guest.institutionCategory
                institutionName = guest.institutionName ?: ""
                address = guest.address
                purpose = guest.purpose
                personToMeet = guest.personToMeet
                photoUri = guest.photoUri
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = "Data tamu tidak dapat dimuat. Kembali dan coba lagi."
            } finally {
                isLoading = false
            }
        }
    }

    fun saveGuestVisit(existingId: Long? = null) {
        if (isLoading) return
        if (name.isBlank()) { errorMessage = "Nama lengkap wajib diisi."; return }
        if (phone.isBlank()) { errorMessage = "Nomor HP wajib diisi."; return }
        if (institutionCategory.isBlank()) { errorMessage = "Instansi belum dipilih."; return }
        if (institutionCategory == "Lainnya" && institutionName.isBlank()) {
            errorMessage = "Nama instansi kustom wajib diisi."
            return
        }
        if (address.isBlank()) { errorMessage = "Alamat wajib diisi."; return }
        if (purpose.isBlank()) { errorMessage = "Keperluan wajib diisi."; return }
        if (personToMeet.isBlank()) { errorMessage = "Tujuan/Bertemu Dengan kosong."; return }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val now = Date()

                if (existingId == null || existingId == 0L) {
                    // Insert new
                    val newGuest = GuestVisitEntity(
                        name = name,
                        phone = phone,
                        institutionCategory = institutionCategory,
                        institutionName = if (institutionCategory == "Lainnya") institutionName else null,
                        address = address,
                        purpose = purpose,
                        personToMeet = personToMeet,
                        photoUri = photoUri,
                        visitDate = dateFormat.format(now),
                        visitTime = timeFormat.format(now),
                        createdAt = now.time,
                        updatedAt = null
                    )
                    guestRepository.insertGuestVisit(newGuest)
                    successMessage = "Data tamu berhasil disimpan."
                } else {
                    // Update existing
                    val original = guestRepository.getGuestVisitById(existingId)
                    if (original != null) {
                        val updatedGuest = original.copy(
                            name = name,
                            phone = phone,
                            institutionCategory = institutionCategory,
                            institutionName = if (institutionCategory == "Lainnya") institutionName else null,
                            address = address,
                            purpose = purpose,
                            personToMeet = personToMeet,
                            photoUri = photoUri,
                            updatedAt = now.time
                        )
                        check(guestRepository.updateGuestVisit(updatedGuest) > 0)
                        successMessage = "Data berhasil diperbarui."
                    } else {
                        errorMessage = "Data tamu sudah tidak tersedia. Kembali ke daftar tamu."
                        return@launch
                    }
                }
                isLoading = false
                _operationSuccess.send(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = "Terjadi kesalahan saat menyimpan data. Silakan coba kembali."
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteGuestVisit(id: Long) {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val guest = guestRepository.getGuestVisitById(id)
                if (guest != null) {
                    check(guestRepository.deleteGuestVisit(guest) > 0)
                }
                _operationSuccess.send(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = "Data tamu gagal dihapus. Silakan coba kembali."
            } finally {
                isLoading = false
            }
        }
    }
}
