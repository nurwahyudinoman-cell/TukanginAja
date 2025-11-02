package com.tukanginAja.solusi.features.chat

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
 * UI State for Chat Screen
 */
data class ChatUiState(
    val messages: List<ChatModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for managing chat state and operations
 * Provides real-time message updates for order-based chat
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatService: ChatService
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatModel>>(emptyList())
    val messages: StateFlow<List<ChatModel>> = _messages.asStateFlow()
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    /**
     * Observe chat messages for a specific order (real-time updates)
     * Automatically updates UI state when messages change
     */
    fun observeChat(orderId: String) {
        if (orderId.isEmpty()) {
            _uiState.update { it.copy(error = "Order ID cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            chatService.listenMessages(orderId)
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error loading messages"
                        )
                    }
                }
                .collect { msgs ->
                    _messages.value = msgs
                    _uiState.update {
                        it.copy(
                            messages = msgs,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    /**
     * Send a chat message to an order's chat
     * Updates Firestore and triggers real-time listener
     */
    fun sendChat(orderId: String, msg: ChatModel) {
        if (orderId.isEmpty() || msg.message.isBlank()) {
            _uiState.update { it.copy(error = "Order ID and message cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = chatService.sendMessage(orderId, msg)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    // Message will appear via real-time listener
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to send message"
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

