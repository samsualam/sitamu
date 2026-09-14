package com.example.sitamu.utils

import java.security.MessageDigest

object SecurityUtils {
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { String.format("%02x", it) }
    }
}
