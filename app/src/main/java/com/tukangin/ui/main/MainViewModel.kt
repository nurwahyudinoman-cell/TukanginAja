package com.tukangin.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukangin.modules.booking.BookingRepository
import com.tukangin.modules.notification.RealtimeSyncService
import com.tukangin.modules.tukang.TukangRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tukangRepository: TukangRepository,
    private val bookingRepository: BookingRepository,
    private val realtimeSyncService: RealtimeSyncService
) : ViewModel() {

    val systemStatus: MutableStateFlow<String> = MutableStateFlow("Initializing...")

    private var syncJob: Job? = null

    fun startSystem(userId: String) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            systemStatus.value = "Syncing data..."
            realtimeSyncService.listenToBookingUpdates(userId).collect { result ->
                result
                    .onSuccess {
                        systemStatus.value = "Ready"
                    }
                    .onFailure { throwable ->
                        systemStatus.value = "Error: ${throwable.message ?: "Unknown"}"
                    }
            }
        }
    }
}

