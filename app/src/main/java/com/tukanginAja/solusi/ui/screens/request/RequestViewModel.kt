package com.tukanginAja.solusi.ui.screens.request

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukanginAja.solusi.data.model.ServiceRequest
import com.tukanginAja.solusi.data.model.TukangLocation
import com.tukanginAja.solusi.data.repository.RequestRepository
import com.tukanginAja.solusi.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Request Screen
 */
data class RequestUiState(
    val requestList: List<ServiceRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    // TAHAP 16: Inject NotificationService untuk kirim notifikasi
    private val notificationService: NotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()
    
    /**
     * Create a new service request
     * TAHAP 16: Kirim notifikasi ke tukang setelah order dibuat
     */
    fun createRequest(customerId: String, tukang: TukangLocation, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            val request = ServiceRequest(
                customerId = customerId,
                tukangId = tukang.id,
                tukangName = tukang.name,
                description = description
            )
            
            val result = requestRepository.createRequest(request)
            result.fold(
                onSuccess = { orderId ->
                    // TAHAP 16: Kirim notifikasi ke tukang bahwa ada order baru
                    notificationService.sendNewOrderNotificationToTukang(
                        tukangId = tukang.id,
                        orderId = orderId,
                        customerId = customerId,
                        description = description
                    )
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Request terkirim"
                        ) 
                    }
                    
                    Log.d("RequestViewModel", "Order created: $orderId, notification sent to tukang: ${tukang.id}")
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Gagal membuat request"
                        ) 
                    }
                    Log.e("RequestViewModel", "Failed to create order", e)
                }
            )
        }
    }
    
    /**
     * Observe requests for a specific tukang (real-time updates)
     */
    fun observeRequestsForTukang(tukangId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            requestRepository.observeRequestsForTukang(tukangId)
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            error = e.message ?: "Error loading requests",
                            isLoading = false
                        ) 
                    }
                }
                .collect { list ->
                    _uiState.update { 
                        it.copy(
                            requestList = list,
                            isLoading = false
                        ) 
                    }
                }
        }
    }
    
    /**
     * Observe requests for a specific customer (real-time updates)
     */
    fun observeRequestsForCustomer(customerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            requestRepository.observeRequestsForCustomer(customerId)
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            error = e.message ?: "Error loading requests",
                            isLoading = false
                        ) 
                    }
                }
                .collect { list ->
                    _uiState.update { 
                        it.copy(
                            requestList = list,
                            isLoading = false
                        ) 
                    }
                }
        }
    }
    
    /**
     * Update request status (accept/decline)
     */
    fun updateRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null, successMessage = null) }
            
            val result = requestRepository.updateRequestStatus(id, status)
            result.fold(
                onSuccess = {
                    val message = when (status) {
                        "accepted" -> "Request diterima"
                        "declined" -> "Request ditolak"
                        "completed" -> "Request selesai"
                        else -> "Status diperbarui"
                    }
                    _uiState.update { 
                        it.copy(successMessage = message)
                    }
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            error = e.message ?: "Gagal memperbarui status"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

