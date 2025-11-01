package com.tukanginAja.solusi.data.model

/**
 * Data class representing a route history entry in Firestore
 * Stores the complete route polyline for a service request
 * 
 * Example Firestore document:
 * {
 *   "id": "rh001",
 *   "orderId": "r001",
 *   "tukangId": "t001",
 *   "customerId": "u001",
 *   "routePoints": [[-6.2088, 106.8456], [-6.2089, 106.8457], ...],
 *   "distance": 1500.5,
 *   "duration": 300.0,
 *   "startLocation": [-6.2088, 106.8456],
 *   "endLocation": [-6.2090, 106.8460],
 *   "createdAt": 1730400000000,
 *   "completedAt": 1730400300000
 * }
 */
data class RouteHistory(
    val id: String = "",
    val orderId: String = "",
    val tukangId: String = "",
    val customerId: String = "",
    val routePoints: List<List<Double>> = emptyList(), // List of [lat, lng] pairs
    val distance: Double = 0.0, // in meters
    val duration: Double = 0.0, // in seconds
    val startLocation: List<Double> = emptyList(), // [lat, lng]
    val endLocation: List<Double> = emptyList(), // [lat, lng]
    val createdAt: Long = 0L,
    val completedAt: Long = 0L
) {
    /**
     * Get formatted distance string
     */
    val formattedDistance: String
        get() = when {
            distance >= 1000 -> "${String.format("%.2f", distance / 1000)} km"
            else -> "${distance.toInt()} m"
        }
    
    /**
     * Get formatted duration string
     */
    val formattedDuration: String
        get() = when {
            duration >= 3600 -> {
                val hours = (duration / 3600).toInt()
                val minutes = ((duration % 3600) / 60).toInt()
                if (minutes > 0) "$hours jam $minutes menit" else "$hours jam"
            }
            duration >= 60 -> {
                val minutes = (duration / 60).toInt()
                "$minutes menit"
            }
            else -> "${duration.toInt()} detik"
        }
}
