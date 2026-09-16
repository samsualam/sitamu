package com.example.sitamu.presentation.guest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sitamu.data.local.AppDatabase
import com.example.sitamu.data.remote.GuestVisitDto
import com.example.sitamu.data.repository.GuestRepository
import com.example.sitamu.data.repository.OnlineGuestRepository
import com.example.sitamu.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestListScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val repository = GuestRepository(database.guestVisitDao())
    val viewModel = viewModel { GuestViewModel(repository) }

    val onlineRepository = remember {
        OnlineGuestRepository()
    }
    val onlineGuestViewModel = viewModel {
        OnlineGuestViewModel(onlineRepository)
    }

    val guestList by viewModel.guestList.collectAsState()
    val pendingGuests by onlineGuestViewModel.pendingGuests.collectAsState()
    val isLoading by onlineGuestViewModel.isLoading.collectAsState()
    val errorMessage by onlineGuestViewModel.errorMessage.collectAsState()
    val processingGuestId by onlineGuestViewModel.processingGuestId.collectAsState()
    val successMessage by onlineGuestViewModel.successMessage.collectAsState()
    val actionErrorMessage by onlineGuestViewModel.actionErrorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var guestActionToConfirm by remember { mutableStateOf<PendingGuestAction?>(null) }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onlineGuestViewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(actionErrorMessage) {
        actionErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onlineGuestViewModel.clearActionErrorMessage()
        }
    }

    // Refresh when screen opens
    LaunchedEffect(viewModel.searchQuery, viewModel.selectedCategory, viewModel.filterPeriod) {
        viewModel.refreshList()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
        Text(
            text = "Daftar Kunjungan Tamu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar Realtime
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { 
                viewModel.searchQuery = it 
                viewModel.refreshList()
            },
            label = { Text("Cari tamu (Nama, Instansi, HP, Keperluan)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Filter Rentang Tanggal Row
        Text(text = "Filter Tanggal:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        val periods = listOf("Semua", "Hari ini", "Kemarin", "Minggu ini", "Bulan ini")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(periods) { period ->
                FilterChip(
                    selected = viewModel.filterPeriod == period,
                    onClick = {
                        viewModel.filterPeriod = period
                        viewModel.refreshList()
                    },
                    label = { Text(period) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Permintaan Tamu Online",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onlineGuestViewModel.refreshPendingGuests() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat ulang permintaan tamu online"
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Memuat permintaan tamu online...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage ?: "Gagal mengambil data tamu online.",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onlineGuestViewModel.refreshPendingGuests() },
                                enabled = !isLoading
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }

                pendingGuests.isEmpty() -> {
                    item {
                        Text(
                            text = "Tidak ada permintaan tamu baru.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                else -> {
                    items(pendingGuests, key = { it.id }) { guest ->
                        OnlineGuestCard(
                            guest = guest,
                            isProcessing = processingGuestId == guest.id,
                            actionsEnabled = processingGuestId == null,
                            onReject = {
                                guestActionToConfirm = PendingGuestAction(
                                    guest = guest,
                                    action = OnlineGuestAction.REJECT
                                )
                            },
                            onApprove = {
                                guestActionToConfirm = PendingGuestAction(
                                    guest = guest,
                                    action = OnlineGuestAction.APPROVE
                                )
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (guestList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada data kunjungan.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.navigate(Screen.GuestForm.createRoute()) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+ Tambah Tamu")
                        }
                    }
                }
            } else {
                items(guestList) { guest ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.GuestDetail.createRoute(guest.id))
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = guest.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${guest.visitDate} ${guest.visitTime}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instansi: ${guest.institutionName ?: guest.institutionCategory}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Keperluan: ${guest.purpose}",
                                fontSize = 14.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

        guestActionToConfirm?.let { pendingAction ->
            val guest = pendingAction.guest
            val isApproval = pendingAction.action == OnlineGuestAction.APPROVE

            AlertDialog(
                onDismissRequest = { guestActionToConfirm = null },
                title = {
                    Text(if (isApproval) "Terima permintaan tamu?" else "Tolak permintaan tamu?")
                },
                text = {
                    Text(
                        if (isApproval) {
                            "Apakah Anda yakin ingin menerima permintaan dari ${guest.name}?"
                        } else {
                            "Apakah Anda yakin ingin menolak permintaan dari ${guest.name}?"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isApproval) {
                                onlineGuestViewModel.approveGuest(guest.id)
                            } else {
                                onlineGuestViewModel.rejectGuest(guest.id)
                            }
                            guestActionToConfirm = null
                        }
                    ) {
                        Text(if (isApproval) "Terima" else "Tolak")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { guestActionToConfirm = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun OnlineGuestCard(
    guest: GuestVisitDto,
    isProcessing: Boolean,
    actionsEnabled: Boolean,
    onReject: () -> Unit,
    onApprove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = guest.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "PENDING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OnlineGuestDetail(label = "Instansi", value = guest.institution ?: "-")
            OnlineGuestDetail(label = "Nomor HP", value = guest.phone)
            OnlineGuestDetail(label = "Tujuan", value = guest.destinationName)
            OnlineGuestDetail(label = "Keperluan", value = guest.purpose)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = actionsEnabled
                ) {
                    Text("Tolak")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApprove,
                    enabled = actionsEnabled
                ) {
                    Text("Terima")
                }
            }
        }
    }
}

@Composable
private fun OnlineGuestDetail(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(88.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

private enum class OnlineGuestAction {
    APPROVE,
    REJECT
}

private data class PendingGuestAction(
    val guest: GuestVisitDto,
    val action: OnlineGuestAction
)
