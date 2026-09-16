package com.example.sitamu.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sitamu.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _loginSuccess =
        Channel<Boolean>(Channel.BUFFERED)

    val loginSuccess =
        _loginSuccess.receiveAsFlow()

    fun onLoginClick() {

        if (isLoading) return

        if (email.isBlank()) {
            errorMessage =
                "Email tidak boleh kosong."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email.trim())
                .matches()
        ) {
            errorMessage =
                "Format email tidak valid."
            return
        }

        if (password.isBlank()) {
            errorMessage =
                "Password tidak boleh kosong."
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {

            val result =
                authRepository.login(
                    email.trim(),
                    password
                )

            isLoading = false

            result
                .onSuccess {
                    _loginSuccess.send(true)
                }
                .onFailure { exception ->
                    errorMessage =
                        exception.message
                            ?: "Login gagal."
                }
        }
    }
}
