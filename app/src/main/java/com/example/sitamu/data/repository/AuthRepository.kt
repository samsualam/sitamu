package com.example.sitamu.data.repository

import com.example.sitamu.data.local.AdminDao
import com.example.sitamu.data.local.AuthPreferences
import com.example.sitamu.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    @Suppress("UNUSED_PARAMETER")
    adminDao: AdminDao,
    private val authPreferences: AuthPreferences
) {

    private val supabase = SupabaseProvider.client

    val isLoggedIn: Flow<Boolean> =
        authPreferences.isLoggedIn

    val adminName: Flow<String?> =
        authPreferences.adminName

    val adminUsername: Flow<String?> =
        authPreferences.adminUsername

    suspend fun login(
        emailInput: String,
        passwordRaw: String
    ): Result<Unit> {

        return try {

            supabase.auth.signInWith(Email) {
                email = emailInput
                password = passwordRaw
            }

            authPreferences.saveSession(
                username = emailInput,
                name = emailInput
            )

            Result.success(Unit)

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {

            Result.failure(
                Exception(
                    "Email atau password tidak sesuai."
                )
            )
        }
    }

    suspend fun logout() {

        try {
            supabase.auth.signOut()
        } catch (_: Exception) {
            // Session lokal tetap harus dibersihkan.
        }

        authPreferences.clearSession()
    }
}
