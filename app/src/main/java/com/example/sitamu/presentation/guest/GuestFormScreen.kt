package com.example.sitamu.presentation.guest

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.sitamu.data.repository.GuestRepository
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestFormScreen(navController: NavController, guestId: Long? = null) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val repository = GuestRepository(database.guestVisitDao())
    val viewModel = viewModel { GuestViewModel(repository) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.photoUri = uri.toString()
            } catch (e: SecurityException) {
                viewModel.errorMessage = "Foto tidak dapat diakses. Silakan pilih foto lain."
            }
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Pemerintah", "Swasta", "Pendidikan", "Organisasi", "Masyarakat Umum", "Lainnya")

    LaunchedEffect(guestId) {
        viewModel.initializeForm(guestId)
    }

    LaunchedEffect(viewModel) {
        viewModel.operationSuccess.collectLatest { success ->
            if (success) {
                navController.popBackStack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = if (guestId == null || guestId == 0L) "Tambah Tamu Baru" else "Edit Data Tamu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Nama Lengkap *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !viewModel.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = { viewModel.phone = it },
            label = { Text("Nomor HP *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !viewModel.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Kategori Instansi Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { if (!viewModel.isLoading) categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = viewModel.institutionCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Asal Instansi *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !viewModel.isLoading
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            viewModel.institutionCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.institutionCategory == "Lainnya") {
            OutlinedTextField(
                value = viewModel.institutionName,
                onValueChange = { viewModel.institutionName = it },
                label = { Text("Nama Instansi Kustom *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !viewModel.isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = viewModel.address,
            onValueChange = { viewModel.address = it },
            label = { Text("Alamat *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = !viewModel.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.purpose,
            onValueChange = { viewModel.purpose = it },
            label = { Text("Keperluan *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            enabled = !viewModel.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.personToMeet,
            onValueChange = { viewModel.personToMeet = it },
            label = { Text("Bertemu Dengan *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !viewModel.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Foto Tamu (Opsional)", style = MaterialTheme.typography.labelLarge)
        viewModel.photoUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Foto tamu yang dipilih",
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            TextButton(onClick = { viewModel.photoUri = null }, enabled = !viewModel.isLoading) {
                Text("Hapus Foto")
            }
        }
        OutlinedButton(
            onClick = { photoPicker.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) { Text("Pilih Foto") }
        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (viewModel.successMessage != null) {
            Text(
                text = viewModel.successMessage ?: "",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { viewModel.saveGuestVisit(guestId) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Simpan Data Tamu", fontSize = 16.sp)
            }
        }
    }
}
