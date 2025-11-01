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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Tukang CRUD Screen
 */
data class TukangCrudUiState(
    val tukangList: List<TukangLocation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TukangCrudViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TukangCrudUiState())
    val uiState: StateFlow<TukangCrudUiState> = _uiState.asStateFlow()
    
    init {
        refreshTukangList()
    }
    
    /**
     * Add a new tukang to Firestore
     */
    fun addTukang(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            val tukang = TukangLocation(
                name = name,
                lat = lat,
                lng = lng,
                status = "offline"
            )
            
            val result = repository.addTukang(tukang)
            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            successMessage = "Tukang berhasil ditambahkan"
                        ) 
                    }
                    refreshTukangList()
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message ?: "Gagal menambahkan tukang"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * Update an existing tukang in Firestore
     */
    fun updateTukang(tukang: TukangLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            val result = repository.updateTukang(tukang)
            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            successMessage = "Tukang berhasil diperbarui"
                        ) 
                    }
                    refreshTukangList()
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message ?: "Gagal memperbarui tukang"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * Delete a tukang from Firestore
     */
    fun deleteTukang(tukangId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            val result = repository.deleteTukang(tukangId)
            result.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            successMessage = "Tukang berhasil dihapus"
                        ) 
                    }
                    refreshTukangList()
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message ?: "Gagal menghapus tukang"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * Refresh tukang list by observing Firestore changes
     */
    private fun refreshTukangList() {
        viewModelScope.launch {
            repository.observeTukangLocations()
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            error = e.message ?: "Error loading tukang list",
                            isLoading = false
                        ) 
                    }
                }
                .collect { list ->
                    _uiState.update { 
                        it.copy(
                            tukangList = list, 
                            isLoading = false
                        ) 
                    }
                }
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

