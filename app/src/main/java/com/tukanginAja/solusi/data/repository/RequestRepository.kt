package com.tukanginAja.solusi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tukanginAja.solusi.data.model.ServiceRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val requestCollection = firestore.collection("service_requests")
    
    /**
     * Create a new service request
     * TAHAP 16: Return Result.success(orderId) untuk notifikasi
     * Returns Result.success(orderId) on success or Result.failure(exception) on error
     */
    suspend fun createRequest(request: ServiceRequest): Result<String> = try {
        val docRef = requestCollection.document()
        
        val newRequest = request.copy(
            id = docRef.id,
            timestamp = System.currentTimeMillis()
        )
        
        val dataMap = mapOf(
            "id" to newRequest.id,
            "customerId" to newRequest.customerId,
            "tukangId" to newRequest.tukangId,
            "tukangName" to newRequest.tukangName,
            "status" to newRequest.status,
            "timestamp" to newRequest.timestamp,
            "description" to newRequest.description
        )
        
        docRef.set(dataMap).await()
        // TAHAP 16: Return order ID untuk notifikasi
        Result.success(newRequest.id)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Update request status
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun updateRequestStatus(id: String, status: String): Result<Unit> {
        return try {
            if (id.isEmpty()) {
                return Result.failure(IllegalArgumentException("Request ID cannot be empty"))
            }
            
            requestCollection.document(id).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe requests for a specific tukang (real-time updates)
     * Returns a Flow that emits List<ServiceRequest> whenever requests change
     */
    fun observeRequestsForTukang(tukangId: String): Flow<List<ServiceRequest>> = callbackFlow {
        if (tukangId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val listenerRegistration: ListenerRegistration = requestCollection
            .whereEqualTo("tukangId", tukangId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        try {
                            ServiceRequest(
                                id = doc.getString("id") ?: doc.id,
                                customerId = doc.getString("customerId") ?: "",
                                tukangId = doc.getString("tukangId") ?: "",
                                tukangName = doc.getString("tukangName") ?: "",
                                status = doc.getString("status") ?: "pending",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                description = doc.getString("description") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(requests)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Observe requests for a specific customer (real-time updates)
     * Returns a Flow that emits List<ServiceRequest> whenever requests change
     */
    fun observeRequestsForCustomer(customerId: String): Flow<List<ServiceRequest>> = callbackFlow {
        if (customerId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val listenerRegistration: ListenerRegistration = requestCollection
            .whereEqualTo("customerId", customerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        try {
                            ServiceRequest(
                                id = doc.getString("id") ?: doc.id,
                                customerId = doc.getString("customerId") ?: "",
                                tukangId = doc.getString("tukangId") ?: "",
                                tukangName = doc.getString("tukangName") ?: "",
                                status = doc.getString("status") ?: "pending",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                description = doc.getString("description") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(requests)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Observe all requests (for admin - real-time updates)
     * Returns a Flow that emits List<ServiceRequest> whenever requests change
     */
    fun observeAllRequests(): Flow<List<ServiceRequest>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = requestCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        try {
                            ServiceRequest(
                                id = doc.getString("id") ?: doc.id,
                                customerId = doc.getString("customerId") ?: "",
                                tukangId = doc.getString("tukangId") ?: "",
                                tukangName = doc.getString("tukangName") ?: "",
                                status = doc.getString("status") ?: "pending",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                description = doc.getString("description") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(requests)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
}

