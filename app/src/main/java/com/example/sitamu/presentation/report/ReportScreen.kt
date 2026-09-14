package com.example.sitamu.presentation.report

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun ReportScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val repository = GuestRepository(database.guestVisitDao())
    val viewModel = viewModel { ReportViewModel(repository) }

    val visits by viewModel.allVisits.collectAsState()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> viewModel.exportTo(context.contentResolver, uri) }

    val dailyRecap = remember(visits) {
        visits.groupBy { it.visitDate }.mapValues { it.value.size }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Laporan Rekap Kunjungan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Rekapitulasi harian dan unduh data CSV",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Seluruh Kunjungan", fontSize = 14.sp)
                    Text(text = "${visits.size} Tamu", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            viewModel.prepareExport()
                            exportLauncher.launch("sitamu-kunjungan.csv")
                        },
                        enabled = visits.isNotEmpty() && !viewModel.isExporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (viewModel.isExporting) "Menyimpan..." else "Export Semua Data ke CSV")
                    }
                }
            }
        }

        if (viewModel.exportMessage != null) {
            item {
                Text(
                    text = viewModel.exportMessage ?: "",
                    color = if (viewModel.exportFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Text(text = "Rekap Kunjungan Harian", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (dailyRecap.isEmpty()) {
            item {
                Text(text = "Belum ada rekap data harian.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            items(dailyRecap.entries.toList()) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Tanggal: ${entry.key}", fontWeight = FontWeight.Medium)
                        Text(text = "${entry.value} Kunjungan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
