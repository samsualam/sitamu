package com.example.sitamu.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sitamu.presentation.auth.LoginScreen
import com.example.sitamu.presentation.auth.SplashScreen
import com.example.sitamu.presentation.dashboard.DashboardScreen
import com.example.sitamu.presentation.destination.DestinationManagementScreen
import com.example.sitamu.presentation.guest.GuestDetailScreen
import com.example.sitamu.presentation.guest.GuestFormScreen
import com.example.sitamu.presentation.guest.GuestListScreen
import com.example.sitamu.presentation.main.MainLayout
import com.example.sitamu.presentation.report.ReportScreen
import com.example.sitamu.presentation.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            MainLayout(navController = navController, currentScreen = Screen.Dashboard) {
                DashboardScreen(navController = navController)
            }
        }
        composable(Screen.GuestList.route) {
            MainLayout(navController = navController, currentScreen = Screen.GuestList) {
                GuestListScreen(navController = navController)
            }
        }
        composable(
            route = Screen.GuestForm.route,
            arguments = listOf(
                navArgument("guestId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val guestIdStr = backStackEntry.arguments?.getString("guestId")
            val guestId = guestIdStr?.toLongOrNull()
            MainLayout(navController = navController, currentScreen = Screen.GuestForm) {
                GuestFormScreen(navController = navController, guestId = guestId)
            }
        }
        composable(
            route = Screen.GuestDetail.route,
            arguments = listOf(
                navArgument("guestId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val guestId = backStackEntry.arguments?.getLong("guestId") ?: 0L
            GuestDetailScreen(navController = navController, guestId = guestId)
        }
        composable(Screen.Report.route) {
            MainLayout(navController = navController, currentScreen = Screen.Report) {
                ReportScreen(navController = navController)
            }
        }
        composable(Screen.Settings.route) {
            MainLayout(navController = navController, currentScreen = Screen.Settings) {
                SettingsScreen(navController = navController)
            }
        }
        composable(Screen.Destinations.route) {
            MainLayout(navController = navController, currentScreen = Screen.Destinations) {
                DestinationManagementScreen(navController = navController)
            }
        }
    }
}
