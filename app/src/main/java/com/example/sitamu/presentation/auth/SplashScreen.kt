package com.example.sitamu.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val authPreferences = AuthPreferences(context)
    val repository = AuthRepository(database.adminDao(), authPreferences)

    LaunchedEffect(Unit) {
        delay(2000)
        val loggedIn = repository.isLoggedIn.first()
        if (loggedIn) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sitamu",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Buku Tamu Digital",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator()
    }
}
