package com.example.sitamu.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object GuestList : Screen("guest_list")
    object GuestForm : Screen("guest_form?guestId={guestId}") {
        fun createRoute(guestId: Long? = null) = if (guestId != null) "guest_form?guestId=$guestId" else "guest_form"
    }
    object GuestDetail : Screen("guest_detail/{guestId}") {
        fun createRoute(guestId: Long) = "guest_detail/$guestId"
    }
    object Report : Screen("report")
    object Settings : Screen("settings")
}
