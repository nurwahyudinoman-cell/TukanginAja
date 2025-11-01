package com.tukanginAja.solusi.ui.screens.tukang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukanginAja.solusi.data.model.TukangLocation
import com.tukanginAja.solusi.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TukangUiState(
    val tukangList: List<TukangLocation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TukangViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TukangUiState(isLoading = true))
    val uiState: StateFlow<TukangUiState> = _uiState.asStateFlow()
    
    init {
        getAllTukang()
    }
    
    /**
     * Fetch all tukang locations from Firestore
     * Observes real-time updates automatically
     */
    fun getAllTukang() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            firestoreRepository.observeTukangLocations()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { tukangList ->
                    _uiState.value = _uiState.value.copy(
                        tukangList = tukangList,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
}

