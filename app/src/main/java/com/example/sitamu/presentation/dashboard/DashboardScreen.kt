package com.example.sitamu.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sitamu.data.local.AppDatabase
import com.example.sitamu.data.local.AuthPreferences
import com.example.sitamu.data.model.GuestVisitEntity
import com.example.sitamu.data.repository.GuestRepository
import com.example.sitamu.navigation.Screen

private val SitamuRed = Color(0xFFB32638)
private val SitamuRedDark = Color(0xFF8E1B2A)
private val DashboardBackground = Color(0xFFF8F7F8)
private val DashboardText = Color(0xFF1D1B20)
private val DashboardSecondaryText = Color(0xFF625B60)
private val DashboardBorder = Color(0xFFE9E3E5)
private val PendingAccent = Color(0xFFB45309)
private val ApprovedAccent = Color(0xFF2E7D32)

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext
    val database = AppDatabase.getInstance(context)
    val repository = GuestRepository(database.guestVisitDao())
    val viewModel = viewModel { DashboardViewModel(repository) }
    val authPreferences = remember(context) { AuthPreferences(context) }

    val todayCount by viewModel.todayCount.collectAsState()
    val monthCount by viewModel.monthCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val latestGuests by viewModel.latest5Guests.collectAsState()
    val adminName by authPreferences.adminName.collectAsState(initial = null)
    val adminUsername by authPreferences.adminUsername.collectAsState(initial = null)
    val displayName = listOf(adminName, adminUsername)
        .firstOrNull { !it.isNullOrBlank() }
        .orEmpty()
        .ifBlank { "Admin" }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DashboardBackground
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { DashboardHeader(displayName) }
            item { TodayHeroCard(todayCount) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard("Bulan ini", monthCount, Icons.Default.List, PendingAccent, Modifier.weight(1f))
                    StatisticCard("Total tamu", totalCount, Icons.Default.AccountBox, ApprovedAccent, Modifier.weight(1f))
                }
            }
            item {
                SectionTitle("Akses Cepat")
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionCard("Daftar Tamu", Icons.Default.AccountBox, { navController.navigate(Screen.GuestList.route) }, Modifier.weight(1f))
                        QuickActionCard("Tambah Tamu", Icons.Default.Add, { navController.navigate(Screen.GuestForm.createRoute()) }, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionCard("Tujuan Kunjungan", Icons.Default.List, { navController.navigate(Screen.Destinations.route) }, Modifier.weight(1f))
                        QuickActionCard("Laporan", Icons.Default.List, { navController.navigate(Screen.Report.route) }, Modifier.weight(1f))
                    }
                }
            }
            item { SectionTitle("Permintaan Terbaru") }
            if (latestGuests.isEmpty()) {
                item { EmptyRecentGuestsCard() }
            } else {
                items(items = latestGuests, key = { it.id }) { guest ->
                    RecentGuestCard(guest) { navController.navigate(Screen.GuestDetail.createRoute(guest.id)) }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(displayName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text("Selamat datang,", style = MaterialTheme.typography.bodyLarge, color = DashboardSecondaryText)
            Text(
                displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DashboardText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text("Kelola kunjungan tamu hari ini", style = MaterialTheme.typography.bodyMedium, color = DashboardSecondaryText)
        }
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(SitamuRed.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                displayName.firstOrNull()?.uppercase() ?: "A",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SitamuRedDark
            )
        }
    }
}

@Composable
private fun TodayHeroCard(todayCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SitamuRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Tamu Hari Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(8.dp))
                Text(todayCount.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Total kunjungan hari ini", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.82f))
            }
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBox, null, Modifier.size(30.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun StatisticCard(title: String, count: Int, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(20.dp), tint = accent)
            }
            Spacer(Modifier.height(14.dp))
            Text(count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DashboardText)
            Text(title, style = MaterialTheme.typography.bodySmall, color = DashboardSecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun QuickActionCard(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(96.dp).clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, Modifier.size(24.dp), tint = SitamuRed)
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = DashboardText, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RecentGuestCard(guest: GuestVisitEntity, onClick: () -> Unit) {
    val institution = guest.institutionName?.takeIf { it.isNotBlank() } ?: guest.institutionCategory
    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(SitamuRed.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(guest.name.firstOrNull()?.uppercase() ?: "T", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SitamuRedDark)
            }
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(guest.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = DashboardText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    VisitDateBadge(guest.visitDate, guest.visitTime)
                }
                Spacer(Modifier.height(4.dp))
                Text(institution, style = MaterialTheme.typography.bodyMedium, color = DashboardSecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(guest.purpose, style = MaterialTheme.typography.bodySmall, color = DashboardSecondaryText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun VisitDateBadge(visitDate: String, visitTime: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = SitamuRed.copy(alpha = 0.08f)) {
        Text(
            "$visitDate • $visitTime",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = SitamuRedDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyRecentGuestsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.List, null, Modifier.size(28.dp), tint = DashboardSecondaryText)
            Spacer(Modifier.height(10.dp))
            Text("Belum ada kunjungan terbaru", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = DashboardText)
            Spacer(Modifier.height(4.dp))
            Text("Data tamu baru akan muncul di sini.", style = MaterialTheme.typography.bodyMedium, color = DashboardSecondaryText)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DashboardText)
}
