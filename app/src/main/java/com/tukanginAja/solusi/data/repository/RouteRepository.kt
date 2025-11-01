package com.tukanginAja.solusi.data.repository

import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor() {
    
    /**
     * Get route between origin and destination using Google Maps Directions API
     * Returns list of LatLng pairs representing the route polyline
     */
    suspend fun getRoute(
        apiKey: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Result<RouteData> {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=$originLat,$originLng" +
                        "&destination=$destLat,$destLng" +
                        "&key=$apiKey" +
                        "&mode=driving"
                
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(
                        Exception("HTTP Error: $responseCode")
                    )
                }
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                val json = JSONObject(response)
                
                // Check for API errors
                val status = json.getString("status")
                if (status != "OK" && status != "ZERO_RESULTS") {
                    val errorMessage = json.optString("error_message", "Unknown error")
                    return@withContext Result.failure(
                        Exception("Directions API Error: $status - $errorMessage")
                    )
                }
                
                if (status == "ZERO_RESULTS") {
                    return@withContext Result.success(
                        RouteData(routePoints = emptyList(), distance = 0.0, duration = 0.0)
                    )
                }
                
                val routes = json.getJSONArray("routes")
                if (routes.length() == 0) {
                    return@withContext Result.success(
                        RouteData(routePoints = emptyList(), distance = 0.0, duration = 0.0)
                    )
                }
                
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline")
                val encodedPolyline = overviewPolyline.getString("points")
                
                // Decode polyline to list of LatLng points
                val decodedPoints = PolyUtil.decode(encodedPolyline)
                val routePoints = decodedPoints.map { latLng -> latLng.latitude to latLng.longitude }
                
                // Extract distance and duration from legs
                var totalDistance = 0.0 // in meters
                var totalDuration = 0.0 // in seconds
                
                val legs = route.getJSONArray("legs")
                for (i in 0 until legs.length()) {
                    val leg = legs.getJSONObject(i)
                    val distance = leg.getJSONObject("distance")
                    val duration = leg.getJSONObject("duration")
                    
                    totalDistance += distance.getDouble("value") // meters
                    totalDuration += duration.getDouble("value") // seconds
                }
                
                Result.success(
                    RouteData(
                        routePoints = routePoints,
                        distance = totalDistance,
                        duration = totalDuration
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

/**
 * Data class representing route information
 */
data class RouteData(
    val routePoints: List<Pair<Double, Double>>,
    val distance: Double, // in meters
    val duration: Double  // in seconds
) {
    /**
     * Get formatted distance string (km or m)
     */
    val formattedDistance: String
        get() = when {
            distance >= 1000 -> "${String.format("%.2f", distance / 1000)} km"
            else -> "${distance.toInt()} m"
        }
    
    /**
     * Get formatted duration string (hours, minutes, or seconds)
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

