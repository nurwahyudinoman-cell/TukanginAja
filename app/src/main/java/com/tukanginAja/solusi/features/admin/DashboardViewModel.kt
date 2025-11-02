package com.tukanginAja.solusi.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Admin Dashboard
 */
data class DashboardUiState(
    val dashboard: AdminDashboardModel = AdminDashboardModel(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Admin Dashboard Screen
 * Manages dashboard state and analytics data
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val analytics: AdminAnalyticsService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    /**
     * Load dashboard summary from Firestore
     * Automatically updates UI state when data is loaded
     */
    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = analytics.getDashboardSummary()
            
            result.fold(
                onSuccess = { dashboard ->
                    _uiState.update {
                        it.copy(
                            dashboard = dashboard,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load dashboard"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Refresh dashboard summary (force reload from Firestore)
     * Clears cache and fetches fresh data
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = analytics.refreshDashboardSummary()
            
            result.fold(
                onSuccess = { dashboard ->
                    _uiState.update {
                        it.copy(
                            dashboard = dashboard,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to refresh dashboard"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

