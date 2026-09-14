package com.example.sitamu.presentation.guest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import coil.compose.AsyncImage
import com.example.sitamu.data.local.AppDatabase
import com.example.sitamu.data.repository.GuestRepository
import com.example.sitamu.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestDetailScreen(navController: NavController, guestId: Long) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val repository = GuestRepository(database.guestVisitDao())
    val viewModel = viewModel { GuestViewModel(repository) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var institutionCategory by remember { mutableStateOf("") }
    var institutionName by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var personToMeet by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    var visitTime by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(guestId) {
        repository.observeGuestVisitById(guestId).collect { guest ->
            if (guest != null) {
                name = guest.name
                phone = guest.phone
                institutionCategory = guest.institutionCategory
                institutionName = guest.institutionName
                address = guest.address
                purpose = guest.purpose
                personToMeet = guest.personToMeet
                visitDate = guest.visitDate
                visitTime = guest.visitTime
                photoUri = guest.photoUri
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.operationSuccess.collectLatest { success ->
            if (success) {
                navController.popBackStack()
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Data?") },
            text = { Text("Apakah Anda yakin ingin menghapus data kunjungan ini?") },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteGuestVisit(guestId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Kunjungan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Kunjungan pada: $visitDate jam $visitTime", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            DetailItem(label = "Nomor HP", value = phone)
            DetailItem(label = "Kategori Instansi", value = institutionCategory)
            if (!institutionName.isNullOrBlank()) {
                DetailItem(label = "Nama Instansi", value = institutionName ?: "")
            }
            DetailItem(label = "Alamat", value = address)
            DetailItem(label = "Keperluan", value = purpose)
            DetailItem(label = "Bertemu Dengan", value = personToMeet)
            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto tamu",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            viewModel.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    enabled = !viewModel.isLoading,
                    onClick = { navController.navigate(Screen.GuestForm.createRoute(guestId)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                Button(
                    enabled = !viewModel.isLoading,
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus")
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
