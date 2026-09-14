package com.example.sitamu.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
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

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val repository = GuestRepository(database.guestVisitDao())
    val viewModel = viewModel { DashboardViewModel(repository) }

    val todayCount by viewModel.todayCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val latestGuests by viewModel.latest5Guests.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard Beranda",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Ringkasan statistik kunjungan buku tamu digital",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = "Hari Ini", count = todayCount, modifier = Modifier.weight(1f))
                StatCard(title = "Bulan Ini", count = monthCount, modifier = Modifier.weight(1f))
                StatCard(title = "Total Tamu", count = totalCount, modifier = Modifier.weight(1f))
            }
        }

        item {
            Text(text = "Aksi Cepat", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.GuestForm.createRoute()) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 12.sp)
                }
                Button(
                    onClick = { navController.navigate(Screen.GuestList.route) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Daftar", fontSize = 12.sp)
                }
                Button(
                    onClick = { navController.navigate(Screen.Report.route) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Laporan", fontSize = 12.sp)
                }
            }
        }

        item {
            Text(text = "5 Kunjungan Terbaru", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (latestGuests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Belum ada data kunjungan terbaru.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(latestGuests) { guest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.GuestDetail.createRoute(guest.id)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = guest.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "${guest.visitDate} ${guest.visitTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Instansi: ${guest.institutionName ?: guest.institutionCategory}", fontSize = 14.sp)
                        Text(text = "Keperluan: ${guest.purpose}", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
