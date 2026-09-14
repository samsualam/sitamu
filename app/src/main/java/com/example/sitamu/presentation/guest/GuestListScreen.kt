package com.example.sitamu.presentation.guest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.sitamu.data.repository.GuestRepository
import com.example.sitamu.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestListScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val repository = GuestRepository(database.guestVisitDao())
    val viewModel = viewModel { GuestViewModel(repository) }

    val guestList by viewModel.guestList.collectAsState()

    // Refresh when screen opens
    LaunchedEffect(viewModel.searchQuery, viewModel.selectedCategory, viewModel.filterPeriod) {
        viewModel.refreshList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        // List Tamu
        if (guestList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
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
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
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
}
