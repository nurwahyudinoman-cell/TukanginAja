package com.tukangin.modules.tukang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TukangViewModel @Inject constructor(
    private val repository: TukangRepository
) : ViewModel() {

    val tukangList = MutableStateFlow<List<TukangModel>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    fun loadAll() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            repository.getAllTukang()
                .onSuccess { list -> tukangList.value = list }
                .onFailure { throwable -> errorMessage.value = throwable.message }
            isLoading.value = false
        }
    }

    fun setAvailability(id: String, available: Boolean) {
        viewModelScope.launch {
            repository.setAvailability(id, available)
                .onSuccess { loadAll() }
                .onFailure { throwable -> errorMessage.value = throwable.message }
        }
    }
}

