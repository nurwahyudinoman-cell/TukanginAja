package com.tukanginAja.solusi.data.model

/**
 * Data class representing a tukang (worker) location in Firestore
 * 
 * Example Firestore document:
 * {
 *   "id": "t001",
 *   "name": "Budi Tukang AC",
 *   "lat": -6.2088,
 *   "lng": 106.8456,
 *   "status": "online",
 *   "updatedAt": 1698743000000
 * }
 */
data class TukangLocation(
    val id: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val status: String = "offline",
    val updatedAt: Long = 0L
) {
    /**
     * Helper property to check if tukang is online
     */
    val isOnline: Boolean
        get() = status == "online"
    
    // Backward compatibility: support for old fields (used by MapScreen)
    val type: String
        get() = ""
    
    val isAvailable: Boolean
        get() = isOnline
}

