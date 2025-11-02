package com.tukanginAja.solusi.features.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Order Screen
 */
data class OrderUiState(
    val userOrders: List<OrderModel> = emptyList(),
    val tukangOrders: List<OrderModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for managing order state and operations
 * Provides real-time order updates for both users and tukangs
 */
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderService: OrderService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()
    
    /**
     * Listen to orders for a specific user
     * Automatically updates UI state when orders change
     */
    fun listenUserOrders(userId: String) {
        if (userId.isEmpty()) {
            _uiState.update { it.copy(error = "User ID cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            orderService.listenOrdersForUser(userId)
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { orders ->
                    _uiState.update {
                        it.copy(
                            userOrders = orders,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    /**
     * Listen to orders for a specific tukang
     * Automatically updates UI state when orders change
     */
    fun listenTukangOrders(tukangId: String) {
        if (tukangId.isEmpty()) {
            _uiState.update { it.copy(error = "Tukang ID cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            orderService.listenOrdersForTukang(tukangId)
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { orders ->
                    _uiState.update {
                        it.copy(
                            tukangOrders = orders,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    /**
     * Update order status
     * Updates the order status in Firestore
     */
    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = orderService.updateOrderStatus(orderId, newStatus)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update order status"
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

