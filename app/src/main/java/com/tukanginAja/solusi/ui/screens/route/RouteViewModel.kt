package com.tukanginAja.solusi.ui.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukanginAja.solusi.BuildConfig
import com.tukanginAja.solusi.data.model.TukangLocation
import com.tukanginAja.solusi.data.repository.FirestoreRepository
import com.tukanginAja.solusi.data.repository.RouteData
import com.tukanginAja.solusi.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Route Screen
 */
data class RouteUiState(
    val tukangLocation: TukangLocation? = null,
    val routeData: RouteData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RouteUiState(isLoading = true))
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()
    
    // Cache for route data to avoid rebuilding polyline on every small update
    private var cachedRouteData: RouteData? = null
    private var lastTukangLocation: TukangLocation? = null
    private var lastUpdateTime = 0L
    private val ROUTE_UPDATE_INTERVAL = 15000L // 15 seconds minimum between route updates
    private val MIN_LOCATION_CHANGE_METERS = 30.0 // Minimum distance change to trigger route update
    
    /**
     * Start tracking route between user and tukang
     * Observes tukang location in real-time and updates route accordingly
     * Uses caching and debouncing to optimize performance
     */
    fun startTracking(userLat: Double, userLng: Double, tukangId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val apiKey = BuildConfig.MAPS_API_KEY
            if (apiKey.isEmpty()) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Maps API key tidak ditemukan"
                    ) 
                }
                return@launch
            }
            
            // Observe tukang location in real-time
            firestoreRepository.observeTukangById(tukangId)
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error observing tukang location"
                        ) 
                    }
                }
                .collect { tukang ->
                    if (tukang.id.isEmpty()) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Tukang tidak ditemukan"
                            ) 
                        }
                        return@collect
                    }
                    
                    // Always update tukang location (real-time marker position)
                    _uiState.update { it.copy(tukangLocation = tukang) }
                    
                    // Check if route update is needed (debouncing and distance check)
                    val currentTime = System.currentTimeMillis()
                    val shouldUpdateRoute = when {
                        tukang.lat == 0.0 || tukang.lng == 0.0 -> false
                        cachedRouteData == null -> true // First time, always fetch
                        lastTukangLocation == null -> true // No previous location, fetch
                        else -> {
                            val distance = calculateDistance(
                                lastTukangLocation!!.lat,
                                lastTukangLocation!!.lng,
                                tukang.lat,
                                tukang.lng
                            )
                            val timeSinceLastUpdate = currentTime - lastUpdateTime
                            
                            // Update if significant location change or enough time passed
                            distance > MIN_LOCATION_CHANGE_METERS || timeSinceLastUpdate > ROUTE_UPDATE_INTERVAL
                        }
                    }
                    
                    // Get route from user to tukang (with caching)
                    if (shouldUpdateRoute && tukang.lat != 0.0 && tukang.lng != 0.0) {
                        val routeResult = routeRepository.getRoute(
                            apiKey = apiKey,
                            originLat = userLat,
                            originLng = userLng,
                            destLat = tukang.lat,
                            destLng = tukang.lng
                        )
                        
                        routeResult.fold(
                            onSuccess = { routeData ->
                                cachedRouteData = routeData
                                lastTukangLocation = tukang
                                lastUpdateTime = currentTime
                                
                                _uiState.update { 
                                    it.copy(
                                        routeData = routeData,
                                        isLoading = false,
                                        error = null
                                    ) 
                                }
                            },
                            onFailure = { e ->
                                // Use cached route if available, even if update failed
                                if (cachedRouteData != null) {
                                    _uiState.update { 
                                        it.copy(
                                            routeData = cachedRouteData,
                                            isLoading = false,
                                            error = "Gagal memperbarui rute, menggunakan rute terakhir"
                                        ) 
                                    }
                                } else {
                                    _uiState.update { 
                                        it.copy(
                                            isLoading = false,
                                            error = e.message ?: "Gagal mendapatkan rute"
                                        ) 
                                    }
                                }
                            }
                        )
                    } else if (cachedRouteData != null) {
                        // Use cached route data
                        _uiState.update { 
                            it.copy(
                                routeData = cachedRouteData,
                                isLoading = false,
                                error = null
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Koordinat tukang tidak valid"
                            ) 
                        }
                    }
                }
        }
    }
    
    /**
     * Retry getting route
     */
    fun retry(userLat: Double, userLng: Double, tukangId: String) {
        startTracking(userLat, userLng, tukangId)
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 // meters
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
}

