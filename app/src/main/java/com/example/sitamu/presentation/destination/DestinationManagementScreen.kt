package com.example.sitamu.presentation.destination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sitamu.data.remote.DestinationContact
import com.example.sitamu.data.repository.DestinationRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationManagementScreen(navController: NavController) {
    val viewModel: DestinationViewModel = viewModel { DestinationViewModel(DestinationRepository()) }
    val uiState by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var editingDestination by remember { mutableStateOf<DestinationContact?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var destinationToDeactivate by remember { mutableStateOf<DestinationContact?>(null) }

    LaunchedEffect(message) {
        message?.let { event ->
            snackbarHostState.showSnackbar(event.text)
            if (event.closeEditor) {
                editingDestination = null
                isCreating = false
            }
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tujuan Kunjungan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Pengaturan")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !uiState.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang tujuan kunjungan")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isCreating = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Tujuan") },
                elevation = FloatingActionButtonDefaults.elevation()
            )
        }
    ) { innerPadding ->
        DestinationContent(
            destinations = uiState.destinations,
            isLoading = uiState.isLoading,
            loadError = uiState.loadError,
            changingActiveId = uiState.changingActiveId,
            onRetry = viewModel::refresh,
            onEdit = { editingDestination = it },
            onActiveChange = { destination, active ->
                if (active) viewModel.setActive(destination, true) else destinationToDeactivate = destination
            },
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (isCreating || editingDestination != null) {
        DestinationEditorDialog(
            destination = editingDestination,
            errors = uiState.formErrors,
            saving = uiState.isSaving,
            onDismiss = {
                if (!uiState.isSaving) {
                    isCreating = false
                    editingDestination = null
                }
            },
            onSave = viewModel::save
        )
    }

    destinationToDeactivate?.let { destination ->
        AlertDialog(
            onDismissRequest = { destinationToDeactivate = null },
            title = { Text("Nonaktifkan tujuan?") },
            text = {
                Text(
                    "${destination.destination.name} tidak lagi tersedia pada form kunjungan web. " +
                        "Data kunjungan dan nomor penerima yang sudah ada tetap disimpan."
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setActive(destination, false)
                    destinationToDeactivate = null
                }) { Text("Nonaktifkan") }
            },
            dismissButton = { TextButton(onClick = { destinationToDeactivate = null }) { Text("Batal") } }
        )
    }
}

@Composable
private fun DestinationContent(
    destinations: List<DestinationContact>,
    isLoading: Boolean,
    loadError: String?,
    changingActiveId: Long?,
    onRetry: () -> Unit,
    onEdit: (DestinationContact) -> Unit,
    onActiveChange: (DestinationContact, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading && destinations.isEmpty() -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        loadError != null && destinations.isEmpty() -> Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(loadError, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Coba Lagi") }
        }
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loadError != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(loadError, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            TextButton(onClick = onRetry) { Text("Coba lagi") }
                        }
                    }
                }
            }
            if (destinations.isEmpty()) {
                item {
                    Text(
                        "Belum ada tujuan kunjungan. Tambahkan tujuan baru untuk mulai menerima kunjungan.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(destinations, key = { it.destination.id }) { destination ->
                    DestinationCard(
                        destination = destination,
                        changingStatus = changingActiveId == destination.destination.id,
                        onEdit = { onEdit(destination) },
                        onActiveChange = { onActiveChange(destination, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationCard(
    destination: DestinationContact,
    changingStatus: Boolean,
    onEdit: () -> Unit,
    onActiveChange: (Boolean) -> Unit
) {
    val active = destination.destination.active
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(destination.destination.name, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(destination.destination.division, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (active) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(if (active) "Aktif" else "Nonaktif", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("WhatsApp: ${destination.whatsapp ?: "Belum tersedia"}")
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onEdit, enabled = !changingStatus) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit")
                }
                Spacer(Modifier.width(8.dp))
                if (changingStatus) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Button(onClick = { onActiveChange(!active) }) { Text(if (active) "Nonaktifkan" else "Aktifkan") }
                }
            }
        }
    }
}

@Composable
private fun DestinationEditorDialog(
    destination: DestinationContact?,
    errors: DestinationFormErrors,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (DestinationDraft) -> Unit
) {
    val destinationId = destination?.destination?.id
    var name by rememberSaveable(destinationId) { mutableStateOf(destination?.destination?.name.orEmpty()) }
    var division by rememberSaveable(destinationId) { mutableStateOf(destination?.destination?.division.orEmpty()) }
    var whatsapp by rememberSaveable(destinationId) { mutableStateOf(destination?.whatsapp.orEmpty()) }
    var active by rememberSaveable(destinationId) { mutableStateOf(destination?.destination?.active ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (destination == null) "Tambah Tujuan" else "Edit Tujuan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, label = { Text("Nama Tujuan *") },
                    isError = errors.name != null,
                    supportingText = { errors.name?.let { Text(it) } },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = division, onValueChange = { division = it }, label = { Text("Bagian/Divisi *") },
                    isError = errors.division != null,
                    supportingText = { errors.division?.let { Text(it) } },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("Nomor WhatsApp *") },
                    isError = errors.whatsapp != null,
                    supportingText = { errors.whatsapp?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { active = !active },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Status aktif", fontWeight = FontWeight.Medium)
                        Text(
                            if (active) "Tersedia pada form kunjungan web" else "Tidak tersedia pada form kunjungan web",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(DestinationDraft(destinationId, name, division, whatsapp, active)) },
                enabled = !saving
            ) {
                if (saving) CircularProgressIndicator(
                    modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                ) else Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Batal") } }
    )
}
