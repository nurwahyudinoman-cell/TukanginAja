package com.tukanginAja.solusi.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukanginAja.solusi.data.model.ChatMessage
import com.tukanginAja.solusi.data.model.ChatSession
import com.tukanginAja.solusi.data.repository.ChatRepository
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
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    /**
     * Load messages for a chat session (real-time observation)
     */
    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            chatRepository.observeMessages(chatId)
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error loading messages"
                        ) 
                    }
                }
                .collect { messages ->
                    _uiState.update { 
                        it.copy(
                            messages = messages,
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
        }
    }
    
    /**
     * Send a message to a chat session
     */
    fun sendMessage(chatId: String, senderId: String, text: String) {
        viewModelScope.launch {
            if (text.isBlank()) {
                return@launch
            }
            
            val message = ChatMessage(
                senderId = senderId,
                text = text.trim(),
                timestamp = System.currentTimeMillis()
            )
            
            val result = chatRepository.sendMessage(chatId, message)
            result.fold(
                onSuccess = {
                    // Message sent successfully (will be reflected via real-time observation)
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            error = e.message ?: "Gagal mengirim pesan"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * Create or get existing chat session
     */
    fun createChatIfNotExists(userId: String, tukangId: String, onChatCreated: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = chatRepository.createChatIfNotExists(userId, tukangId)
            result.fold(
                onSuccess = { chatId ->
                    _uiState.update { it.copy(isLoading = false) }
                    onChatCreated(chatId)
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Gagal membuat chat"
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

