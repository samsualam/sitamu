package com.example.sitamu.data.repository

import com.example.sitamu.data.local.AdminDao
import com.example.sitamu.data.local.AuthPreferences
import com.example.sitamu.data.model.AdminEntity
import com.example.sitamu.utils.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CancellationException

class AuthRepository(
    private val adminDao: AdminDao,
    private val authPreferences: AuthPreferences
) {
    val isLoggedIn: Flow<Boolean> = authPreferences.isLoggedIn
    val adminName: Flow<String?> = authPreferences.adminName
    val adminUsername: Flow<String?> = authPreferences.adminUsername

    suspend fun login(username: String, passwordRaw: String): Result<AdminEntity> {
        return try {
            val admin = adminDao.getAdminByUsername(username)
            if (admin == null) {
                return Result.failure(Exception("Username atau password tidak sesuai."))
            }
            val hashedInput = SecurityUtils.hashPassword(passwordRaw)
            if (admin.passwordHash == hashedInput) {
                authPreferences.saveSession(admin.username, admin.name)
                Result.success(admin)
            } else {
                Result.failure(Exception("Username atau password tidak sesuai."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Terjadi kesalahan saat masuk. Silakan coba kembali."))
        }
    }

    suspend fun logout() {
        authPreferences.clearSession()
    }
}
