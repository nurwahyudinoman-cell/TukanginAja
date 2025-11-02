package com.tukanginAja.solusi.features.order

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling order operations with Firestore
 * Provides real-time listeners for orders
 */
@Singleton
class OrderService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val ordersCollection = firestore.collection("orders")
    
    /**
     * Listen to orders for a specific tukang (real-time updates)
     * Returns a Flow that emits List<OrderModel> whenever orders change
     */
    fun listenOrdersForTukang(tukangId: String): Flow<List<OrderModel>> = callbackFlow {
        if (tukangId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val listenerRegistration: ListenerRegistration = ordersCollection
            .whereEqualTo("tukangId", tukangId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        try {
                            OrderModel.fromMap(doc.id, doc.data())
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(orders)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Listen to orders for a specific user (real-time updates)
     * Returns a Flow that emits List<OrderModel> whenever orders change
     */
    fun listenOrdersForUser(userId: String): Flow<List<OrderModel>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val listenerRegistration: ListenerRegistration = ordersCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        try {
                            OrderModel.fromMap(doc.id, doc.data())
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(orders)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Update order status
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            if (orderId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Order ID cannot be empty"))
            }
            
            ordersCollection.document(orderId)
                .update("status", status)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

