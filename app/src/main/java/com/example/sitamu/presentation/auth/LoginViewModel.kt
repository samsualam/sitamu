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

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    private val _loginSuccess = Channel<Boolean>(Channel.BUFFERED)
    val loginSuccess = _loginSuccess.receiveAsFlow()
    
    fun onLoginClick() {
        if (isLoading) return
        if (username.isBlank()) {
            errorMessage = "Username tidak boleh kosong."
            return
        }
        if (password.isBlank()) {
            errorMessage = "Password tidak boleh kosong."
            return
        }
        
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            val result = authRepository.login(username.trim(), password)
            isLoading = false
            result.onSuccess {
                _loginSuccess.send(true)
            }.onFailure { exception ->
                errorMessage = exception.message
            }
        }
    }
}
