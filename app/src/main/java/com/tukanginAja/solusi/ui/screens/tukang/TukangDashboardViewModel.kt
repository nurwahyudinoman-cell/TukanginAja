package com.tukanginAja.solusi.ui.screens.tukang

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukanginAja.solusi.data.model.ServiceRequest
import com.tukanginAja.solusi.data.repository.RequestRepository
import com.tukanginAja.solusi.data.repository.RouteHistoryRepository
import com.tukanginAja.solusi.data.repository.RouteData
import com.tukanginAja.solusi.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Tukang Dashboard
 */
data class TukangDashboardUiState(
    val activeOrders: List<ServiceRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentRouteData: RouteData? = null
)

/**
 * ViewModel for Tukang Dashboard Screen
 */
@HiltViewModel
class TukangDashboardViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val routeHistoryRepository: RouteHistoryRepository,
    // TAHAP 16: Inject NotificationService untuk kirim notifikasi
    private val notificationService: NotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TukangDashboardUiState(isLoading = true))
    val uiState: StateFlow<TukangDashboardUiState> = _uiState.asStateFlow()
    
    /**
     * Load active orders for tukang
     */
    fun loadActiveOrders(tukangId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            requestRepository.observeRequestsForTukang(tukangId)
                .collect { orders ->
                    // Filter only active orders (not completed or declined)
                    val activeOrders = orders.filter { order ->
                        order.status != "completed" && order.status != "declined"
                    }.sortedByDescending { it.timestamp }
                    
                    _uiState.update {
                        it.copy(
                            activeOrders = activeOrders,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    /**
     * Complete an order and save route history
     */
    fun completeOrder(
        order: ServiceRequest,
        routeData: RouteData?,
        startLocation: Pair<Double, Double>,
        endLocation: Pair<Double, Double>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Update order status to completed
            val updateResult = requestRepository.updateRequestStatus(order.id, "completed")
            
            updateResult.fold(
                onSuccess = {
                    // TAHAP 16: Kirim notifikasi ke user bahwa order selesai
                    notificationService.sendOrderCompletedNotificationToUser(
                        userId = order.customerId,
                        orderId = order.id,
                        tukangName = order.tukangName
                    )
                    Log.d("TukangDashboardViewModel", "Order completed: ${order.id}, notification sent to user: ${order.customerId}")
                    
                    // Save route history if route data is available
                    if (routeData != null) {
                        val routePoints = routeData.routePoints.map { (lat, lng) ->
                            listOf(lat, lng)
                        }
                        
                        val routeHistory = com.tukanginAja.solusi.data.model.RouteHistory(
                            orderId = order.id,
                            tukangId = order.tukangId,
                            customerId = order.customerId,
                            routePoints = routePoints,
                            distance = routeData.distance,
                            duration = routeData.duration,
                            startLocation = listOf(startLocation.first, startLocation.second),
                            endLocation = listOf(endLocation.first, endLocation.second),
                            createdAt = order.timestamp,
                            completedAt = System.currentTimeMillis()
                        )
                        
                        routeHistoryRepository.saveRouteHistory(routeHistory)
                            .fold(
                                onSuccess = {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            error = null
                                        )
                                    }
                                },
                                onFailure = { e ->
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            error = "Gagal menyimpan histori perjalanan: ${e.message}"
                                        )
                                    }
                                }
                            )
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Gagal menyelesaikan order: ${e.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Accept an order
     */
    fun acceptOrder(order: ServiceRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = requestRepository.updateRequestStatus(order.id, "accepted")
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Gagal menerima order: ${e.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Decline an order
     */
    fun declineOrder(order: ServiceRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = requestRepository.updateRequestStatus(order.id, "declined")
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Gagal menolak order: ${e.message}"
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
