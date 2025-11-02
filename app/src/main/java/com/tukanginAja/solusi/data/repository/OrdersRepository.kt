package com.tukanginAja.solusi.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.tukanginAja.solusi.data.model.Order
import com.tukanginAja.solusi.data.model.OrderLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrdersRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val ordersCollection = firestore.collection("orders")
    // Coroutine scope for fire-and-forget logging
    private val loggingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Create a new order
     * Returns Result.success(orderId) on success or Result.failure(exception) on error
     */
    suspend fun createOrder(order: Order): Result<String> = try {
        val docRef = ordersCollection.document()
        
        val newOrder = order.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val dataMap = mapOf(
            "id" to newOrder.id,
            "userId" to newOrder.userId,
            "userName" to newOrder.userName,
            "tukangId" to newOrder.tukangId,
            "status" to newOrder.status,
            "serviceType" to newOrder.serviceType,
            "location" to mapOf(
                "lat" to newOrder.location.lat,
                "lng" to newOrder.location.lng,
                "address" to newOrder.location.address
            ),
            "price" to newOrder.price,
            "createdAt" to newOrder.createdAt,
            "updatedAt" to newOrder.updatedAt
        )
        
        docRef.set(dataMap).await()
        Result.success(newOrder.id)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Update order status with status transition validation
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun updateOrderStatus(id: String, status: String): Result<Unit> {
        return try {
            if (id.isEmpty()) {
                return Result.failure(IllegalArgumentException("Order ID cannot be empty"))
            }

            val docRef = ordersCollection.document(id)
            
            // Use transaction to enforce status transition validation
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Order not found")
                }
                
                val currentStatus = snapshot.getString("status") ?: "requested"
                
                // Allowed transitions map
                val allowedTransitions = mapOf(
                    "requested" to listOf("assigned", "Diterima", "cancelled", "Dibatalkan"),
                    "Menunggu" to listOf("assigned", "Diterima", "cancelled", "Dibatalkan"),
                    "assigned" to listOf("in_progress", "Dikerjakan", "cancelled", "Dibatalkan"),
                    "Diterima" to listOf("in_progress", "Dikerjakan", "cancelled", "Dibatalkan"),
                    "in_progress" to listOf("done", "Selesai", "cancelled", "Dibatalkan"),
                    "Dikerjakan" to listOf("done", "Selesai", "cancelled", "Dibatalkan"),
                    "done" to emptyList(),
                    "Selesai" to emptyList(),
                    "cancelled" to emptyList(),
                    "Dibatalkan" to emptyList()
                )
                
                val allowedNextStatuses = allowedTransitions[currentStatus] ?: emptyList()
                if (!allowedNextStatuses.contains(status)) {
                    throw IllegalStateException("Invalid status transition: $currentStatus -> $status")
                }
                
                transaction.update(docRef, mapOf(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                ))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Log error to system_logs (fire-and-forget, won't block)
            loggingScope.launch {
                logError("order_status_update_error", hashMapOf(
                    "orderId" to id,
                    "status" to status,
                    "error" to (e.message ?: e.toString())
                ))
            }
            Result.failure(e)
        }
    }
    
    /**
     * Attempt to accept an order atomically
     * Returns true if succeeded, false if it was already accepted / not in requested state
     */
    suspend fun acceptOrderAtomically(orderId: String, takerUid: String): Result<Boolean> {
        return try {
            if (orderId.isEmpty() || takerUid.isEmpty()) {
                return Result.failure(IllegalArgumentException("Order ID and Tukang ID cannot be empty"))
            }
            
            val docRef = ordersCollection.document(orderId)
            
            val success = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                
                if (!snapshot.exists()) {
                    return@runTransaction false
                }
                
                val currentStatus = snapshot.getString("status") ?: "requested"
                
                // Only accept if order is in requested/Menunggu state
                if (currentStatus != "requested" && currentStatus != "Menunggu") {
                    return@runTransaction false
                }
                
                // Check if already assigned to someone else
                val existingTukangId = snapshot.getString("tukangId")
                if (!existingTukangId.isNullOrEmpty() && existingTukangId != takerUid) {
                    return@runTransaction false
                }
                
                // Update order atomically
                val updateData = mapOf(
                    "status" to "Diterima",
                    "tukangId" to takerUid,
                    "updatedAt" to System.currentTimeMillis(),
                    "acceptedAt" to System.currentTimeMillis()
                )
                transaction.update(docRef, updateData)
                
                true
            }.await()
            
            Result.success(success)
        } catch (e: Exception) {
            // Log error to system_logs (fire-and-forget, won't block)
            loggingScope.launch {
                logError("order_accept_error", hashMapOf(
                    "orderId" to orderId,
                    "takerUid" to takerUid,
                    "error" to (e.message ?: e.toString())
                ))
            }
            Result.failure(e)
        }
    }
    
    /**
     * Log error to system_logs collection (suspending, call from coroutine scope)
     */
    private suspend fun logError(type: String, details: HashMap<String, Any?>) {
        try {
            val logRef = firestore.collection("system_logs").document(UUID.randomUUID().toString())
            logRef.set(hashMapOf(
                "type" to type,
                "details" to details,
                "timestamp" to System.currentTimeMillis(),
                "timestampMillis" to System.currentTimeMillis()
            )).await()
            Log.d("OrdersRepository", "Error logged: $type - $details")
        } catch (e: Exception) {
            // Don't fail if logging fails
            Log.e("OrdersRepository", "Failed to log error: $e")
        }
    }
    
    /**
     * Get orders stream by role and user ID (real-time updates)
     * Returns a Flow that emits List<Order> whenever orders change
     * 
     * @param role "user", "tukang", or "admin"
     * @param userId The user ID to filter orders for
     */
    fun getOrdersStreamByRole(role: String, userId: String): Flow<List<Order>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val query = when (role) {
            "user" -> ordersCollection.whereEqualTo("userId", userId)
            "tukang" -> ordersCollection.whereEqualTo("tukangId", userId)
            "admin" -> ordersCollection
            else -> ordersCollection.whereEqualTo("userId", userId) // Default to user
        }.orderBy("createdAt", Query.Direction.DESCENDING)
        
        val listenerRegistration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("OrdersRepository", "Snapshot listener error for role $role: ${error.message}", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            if (snapshot == null || snapshot.documents.isEmpty()) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            // Defensive mapping with error handling
            val orders = snapshot.documents.mapNotNull { doc ->
                try {
                    Order.fromMap(doc.id, doc.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e("OrdersRepository", "Order parsing error for doc ${doc.id}: ${e.message}", e)
                    null
                }
            }
            
            trySend(orders)
        }
        
        awaitClose {
            listenerRegistration.remove()
            Log.d("OrdersRepository", "Listener closed for role $role, userId $userId")
        }
    }.catch { e ->
        Log.e("OrdersRepository", "Flow error for role $role: ${e.message}", e)
        emit(emptyList())
    }
}

