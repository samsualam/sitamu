package com.example.sitamu.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sitamu.data.local.AppDatabase
import com.example.sitamu.data.local.AuthPreferences
import com.example.sitamu.data.repository.AuthRepository
import com.example.sitamu.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val authPreferences = AuthPreferences(context)
    val repository = AuthRepository(database.adminDao(), authPreferences)
    val scope = rememberCoroutineScope()

    val adminName by repository.adminName.collectAsState(initial = "Administrator")
    val adminUsername by repository.adminUsername.collectAsState(initial = "admin")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Pengaturan Aplikasi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Profil Petugas Aktif", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = adminName ?: "Administrator", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Username: ${adminUsername ?: "admin"}", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Manajemen Kunjungan", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(Screen.Destinations.route) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBox, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Tujuan Kunjungan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Kelola tujuan, divisi, nomor WhatsApp, dan status aktif.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Tentang Aplikasi", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Sitamu - Buku Tamu Digital", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Versi: 1.0 (MVP)", fontSize = 14.sp)
                Text(text = "Pengembang: Internal IT Team", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                scope.launch {
                    repository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar dari Akun (Logout)", fontSize = 16.sp)
        }
    }
}
