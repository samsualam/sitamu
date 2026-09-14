package com.example.sitamu.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.sitamu.navigation.Screen

@Composable
fun MainLayout(
    navController: NavController,
    currentScreen: Screen,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.Dashboard,
                    onClick = {
                        if (currentScreen != Screen.Dashboard) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.GuestList,
                    onClick = {
                        if (currentScreen != Screen.GuestList) {
                            navController.navigate(Screen.GuestList.route) {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = "Tamu") },
                    label = { Text("Tamu") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.GuestForm,
                    onClick = {
                        if (currentScreen != Screen.GuestForm) {
                            navController.navigate(Screen.GuestForm.createRoute())
                        }
                    },
                    icon = { 
                        FloatingActionButton(
                            onClick = {
                                if (currentScreen != Screen.GuestForm) {
                                    navController.navigate(Screen.GuestForm.createRoute())
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah")
                        }
                    },
                    label = { Text("Tambah") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Report,
                    onClick = {
                        if (currentScreen != Screen.Report) {
                            navController.navigate(Screen.Report.route) {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = "Laporan") },
                    label = { Text("Laporan") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Settings,
                    onClick = {
                        if (currentScreen != Screen.Settings) {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                    label = { Text("Pengaturan") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
