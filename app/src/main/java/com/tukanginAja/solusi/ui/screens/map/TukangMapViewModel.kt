package com.tukanginAja.solusi.ui.screens.map

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

/**
 * UI State for Tukang Map Screen
 */
data class MapUiState(
    val tukangList: List<TukangLocation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TukangMapViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState(isLoading = true))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        observeTukangLocations()
    }
    
    /**
     * Observe real-time tukang locations from Firestore
     * All data comes from FirestoreRepository Flow
     */
    private fun observeTukangLocations() {
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
    
    /**
     * Retry observing tukang locations
     */
    fun retry() {
        observeTukangLocations()
    }
}

