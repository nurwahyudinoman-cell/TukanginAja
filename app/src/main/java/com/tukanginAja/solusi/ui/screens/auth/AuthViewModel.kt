package com.tukanginAja.solusi.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukanginAja.solusi.domain.usecase.auth.LoginUseCase
import com.tukanginAja.solusi.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        // Validation
        when {
            email.isBlank() -> {
                _uiState.value = AuthUiState.Error("Email cannot be empty")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = AuthUiState.Error("Please enter a valid email address")
                return
            }
            password.isBlank() -> {
                _uiState.value = AuthUiState.Error("Password cannot be empty")
                return
            }
            password.length < 6 -> {
                _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
                return
            }
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email.trim(), password)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                }
                .onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("password", ignoreCase = true) == true -> 
                            "Invalid password"
                        exception.message?.contains("user", ignoreCase = true) == true -> 
                            "User not found"
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection"
                        else -> exception.message ?: "Login failed. Please try again"
                    }
                    _uiState.value = AuthUiState.Error(errorMessage)
                }
        }
    }
    
    fun register(email: String, password: String) {
        // Validation
        when {
            email.isBlank() -> {
                _uiState.value = AuthUiState.Error("Email cannot be empty")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = AuthUiState.Error("Please enter a valid email address")
                return
            }
            password.isBlank() -> {
                _uiState.value = AuthUiState.Error("Password cannot be empty")
                return
            }
            password.length < 6 -> {
                _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
                return
            }
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registerUseCase(email.trim(), password)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                }
                .onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("email", ignoreCase = true) == true -> 
                            "This email is already registered"
                        exception.message?.contains("weak", ignoreCase = true) == true -> 
                            "Password is too weak"
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection"
                        else -> exception.message ?: "Registration failed. Please try again"
                    }
                    _uiState.value = AuthUiState.Error(errorMessage)
                }
        }
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

