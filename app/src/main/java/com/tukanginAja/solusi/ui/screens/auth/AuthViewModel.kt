package com.tukanginAja.solusi.ui.screens.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukanginAja.solusi.data.repository.AuthRepository
import com.tukanginAja.solusi.domain.usecase.auth.LoginUseCase
import com.tukanginAja.solusi.domain.usecase.auth.RegisterUseCase
import com.tukanginAja.solusi.utils.RolePreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val authRepository: AuthRepository,
    private val application: Application
) : ViewModel() {
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()
    
    /**
     * Fetch user role from Firestore and save to SharedPreferences
     * Uses UID-based lookup for consistency with users/{uid} path
     * Falls back to email-based lookup if UID is not available
     */
    private suspend fun fetchAndSaveUserRole(email: String) {
        try {
            // Try to get UID from current user first (preferred method)
            val uid = authRepository.currentUser?.uid
            
            val roleResult = if (!uid.isNullOrBlank()) {
                Log.d(TAG, "Fetching user role for UID: $uid")
                authRepository.getUserRoleByUid(uid)
            } else {
                Log.d(TAG, "UID not available, fetching user role for email: $email")
                authRepository.getUserRole(email.trim())
            }
            
            when {
                roleResult.isSuccess -> {
                    val role = roleResult.getOrNull()?.lowercase()?.trim()
                    if (!role.isNullOrBlank()) {
                        Log.d(TAG, "✅ User role fetched successfully: $role")
                        RolePreferencesHelper.saveUserRole(application, role)
                        _userRole.value = role
                    } else {
                        Log.w(TAG, "⚠️ Role is null or blank, defaulting to 'user'")
                        RolePreferencesHelper.saveUserRole(application, "user")
                        _userRole.value = "user"
                    }
                }
                else -> {
                    val exception = roleResult.exceptionOrNull()
                    Log.e(TAG, "❌ Failed to fetch user role: ${exception?.message}", exception)
                    // Don't default to user on error - let the caller handle it
                    _userRole.value = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception while fetching user role: ${e.message}", e)
            _userRole.value = null
        }
    }
    
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
                .onSuccess { user ->
                    Log.d(TAG, "Firebase Auth login successful for: ${user.email}")
                    
                    // Fetch and save user role after successful login
                    fetchAndSaveUserRole(email.trim())
                    
                    // Check if role was successfully fetched
                    if (_userRole.value != null) {
                        val role = _userRole.value!!
                        Log.d(TAG, "✅ Login success for ${email.trim()} with role: $role")
                        onLoginSuccess(email.trim(), role)
                    } else {
                        Log.e(TAG, "Failed to fetch user role after successful login")
                        _uiState.value = AuthUiState.Error("Failed to retrieve user role. Please contact support.")
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Login failed: ${exception.message}", exception)
                    val errorMessage = when {
                        exception.message?.contains("password", ignoreCase = true) == true -> 
                            "Invalid password"
                        exception.message?.contains("user", ignoreCase = true) == true -> 
                            "User not found"
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection"
                        exception.message?.contains("recaptcha", ignoreCase = true) == true -> 
                            "Authentication required. Please try again"
                        exception.message?.contains("invalid", ignoreCase = true) == true -> 
                            "Invalid email or password"
                        exception.message?.contains("too-many-requests", ignoreCase = true) == true -> 
                            "Too many failed attempts. Please try again later"
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
                .onSuccess { user ->
                    Log.d(TAG, "Registration successful for: ${user.email}")
                    // Default to user role for new registrations
                    val role = "user"
                    RolePreferencesHelper.saveUserRole(application, role)
                    _userRole.value = role
                    Log.d(TAG, "New user registered with role: $role")
                    onLoginSuccess(user.email ?: email.trim(), role)
                }
                .onFailure { exception ->
                    Log.e(TAG, "Registration failed: ${exception.message}", exception)
                    val errorMessage = when {
                        exception.message?.contains("email", ignoreCase = true) == true -> 
                            "This email is already registered"
                        exception.message?.contains("weak", ignoreCase = true) == true -> 
                            "Password is too weak"
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection"
                        exception.message?.contains("recaptcha", ignoreCase = true) == true -> 
                            "Verification required. Please try again"
                        exception.message?.contains("invalid", ignoreCase = true) == true -> 
                            "Invalid email format or password"
                        else -> exception.message ?: "Registration failed. Please try again"
                    }
                    _uiState.value = AuthUiState.Error(errorMessage)
                }
        }
    }
    
    /**
     * Handle successful login - save role to preferences
     */
    private fun onLoginSuccess(email: String, role: String) {
        RolePreferencesHelper.saveUserRole(application, role)
        _userRole.value = role
        _uiState.value = AuthUiState.Success(role = role)
        Log.d(TAG, "✅ Login success for $email with role: $role")
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: String? = null) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

