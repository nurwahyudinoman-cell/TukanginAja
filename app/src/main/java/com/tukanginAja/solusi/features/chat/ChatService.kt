package com.tukanginAja.solusi.features.chat

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling chat operations with Firestore
 * Provides real-time listeners for messages in orders/{orderId}/messages
 */
@Singleton
class ChatService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Listen to messages for a specific order (real-time updates)
     * Returns a Flow that emits List<ChatModel> whenever messages change
     * Messages are stored in orders/{orderId}/messages subcollection
     */
    fun listenMessages(orderId: String): Flow<List<ChatModel>> = callbackFlow {
        if (orderId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val messagesRef = firestore.collection("orders")
            .document(orderId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
        
        val listener: ListenerRegistration = messagesRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: emptyMap()
                        ChatModel.fromMap(doc.id, data)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose {
            listener.remove()
        }
    }
    
    /**
     * Send a message to an order's chat
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun sendMessage(orderId: String, msg: ChatModel): Result<Unit> {
        return try {
            if (orderId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Order ID cannot be empty"))
            }
            
            val newMessage = msg.copy(
                orderId = orderId,
                createdAt = System.currentTimeMillis()
            )
            
            val messagesRef = firestore.collection("orders")
                .document(orderId)
                .collection("messages")
            
            messagesRef.add(newMessage.toMap()).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

