package com.tukangin.modules.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<BookingModel>>(emptyList())
    val bookings: StateFlow<List<BookingModel>> = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadBookingsForUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getBookingsByUser(userId)
                .onSuccess { _bookings.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadBookingsForTukang(tukangId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getBookingsByTukang(tukangId)
                .onSuccess { _bookings.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }
}
