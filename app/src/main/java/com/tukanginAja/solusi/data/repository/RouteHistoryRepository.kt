package com.tukanginAja.solusi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tukanginAja.solusi.data.model.RouteHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing route history in Firestore
 */
@Singleton
class RouteHistoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val historyCollection = firestore.collection("route_history")
    
    /**
     * Save route history to Firestore
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun saveRouteHistory(history: RouteHistory): Result<Unit> = try {
        val docRef = if (history.id.isEmpty()) {
            historyCollection.document()
        } else {
            historyCollection.document(history.id)
        }
        
        val newHistory = history.copy(
            id = docRef.id,
            completedAt = if (history.completedAt == 0L) System.currentTimeMillis() else history.completedAt
        )
        
        val dataMap = mapOf(
            "id" to newHistory.id,
            "orderId" to newHistory.orderId,
            "tukangId" to newHistory.tukangId,
            "customerId" to newHistory.customerId,
            "routePoints" to newHistory.routePoints,
            "distance" to newHistory.distance,
            "duration" to newHistory.duration,
            "startLocation" to newHistory.startLocation,
            "endLocation" to newHistory.endLocation,
            "createdAt" to newHistory.createdAt,
            "completedAt" to newHistory.completedAt
        )
        
        docRef.set(dataMap).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Get route history for a specific order
     * Returns Flow that emits RouteHistory?
     */
    fun getRouteHistoryByOrderId(orderId: String): Flow<RouteHistory?> = flow {
        try {
            val snapshot = historyCollection
                .whereEqualTo("orderId", orderId)
                .limit(1)
                .get()
                .await()
            
            val history = if (!snapshot.isEmpty && snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents[0]
                parseRouteHistory(doc)
            } else {
                null
            }
            
            emit(history)
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    /**
     * Get route history for a specific tukang
     * Returns Flow that emits List<RouteHistory>
     */
    fun getRouteHistoryByTukangId(tukangId: String): Flow<List<RouteHistory>> = flow {
        try {
            val snapshot = historyCollection
                .whereEqualTo("tukangId", tukangId)
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val historyList = snapshot.documents.mapNotNull { doc ->
                parseRouteHistory(doc)
            }
            
            emit(historyList)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get route history for a specific customer
     * Returns Flow that emits List<RouteHistory>
     */
    fun getRouteHistoryByCustomerId(customerId: String): Flow<List<RouteHistory>> = flow {
        try {
            val snapshot = historyCollection
                .whereEqualTo("customerId", customerId)
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val historyList = snapshot.documents.mapNotNull { doc ->
                parseRouteHistory(doc)
            }
            
            emit(historyList)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Parse Firestore document to RouteHistory
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseRouteHistory(doc: com.google.firebase.firestore.DocumentSnapshot): RouteHistory? {
        return try {
            @Suppress("UNCHECKED_CAST")
            val routePoints: List<*> = (doc.get("routePoints") as? List<*>) ?: emptyList<Any>()
            @Suppress("UNCHECKED_CAST")
            val parsedRoutePoints: List<List<Double>> = routePoints.mapNotNull { point: Any? ->
                when (point) {
                    is List<*> -> {
                        val lat = (point[0] as? Number)?.toDouble() ?: 0.0
                        val lng = (point[1] as? Number)?.toDouble() ?: 0.0
                        if (lat != 0.0 && lng != 0.0) listOf(lat, lng) else null
                    }
                    else -> null
                }
            }
            
            // Parse start location
            @Suppress("UNCHECKED_CAST")
            val startLocationRaw: List<*> = (doc.get("startLocation") as? List<*>) ?: emptyList<Any>()
            val parsedStartLocation: List<Double> = startLocationRaw
                .mapNotNull { item: Any? ->
                    when (item) {
                        is Number -> item.toDouble()
                        else -> null
                    }
                }
            
            // Parse end location
            @Suppress("UNCHECKED_CAST")
            val endLocationRaw: List<*> = (doc.get("endLocation") as? List<*>) ?: emptyList<Any>()
            val parsedEndLocation: List<Double> = endLocationRaw
                .mapNotNull { item: Any? ->
                    when (item) {
                        is Number -> item.toDouble()
                        else -> null
                    }
                }
            
            RouteHistory(
                id = doc.getString("id") ?: doc.id,
                orderId = doc.getString("orderId") ?: "",
                tukangId = doc.getString("tukangId") ?: "",
                customerId = doc.getString("customerId") ?: "",
                routePoints = parsedRoutePoints,
                distance = (doc.get("distance") as? Number)?.toDouble() ?: 0.0,
                duration = (doc.get("duration") as? Number)?.toDouble() ?: 0.0,
                startLocation = parsedStartLocation,
                endLocation = parsedEndLocation,
                createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: 0L,
                completedAt = (doc.get("completedAt") as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) {
            null
        }
    }
}
