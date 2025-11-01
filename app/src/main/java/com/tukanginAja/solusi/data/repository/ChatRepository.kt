package com.tukanginAja.solusi.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.tukanginAja.solusi.data.model.ChatMessage
import com.tukanginAja.solusi.data.model.ChatSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Observe messages in a chat session (real-time updates)
     * Returns a Flow that emits List<ChatMessage> whenever messages change
     */
    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (chatId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val messagesRef = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
        
        val listenerRegistration: ListenerRegistration = messagesRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            ChatMessage(
                                id = doc.id,
                                senderId = doc.getString("senderId") ?: "",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(messages)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Send a message to a chat session
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun sendMessage(chatId: String, message: ChatMessage): Result<Unit> {
        return try {
            if (chatId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Chat ID cannot be empty"))
            }
        
        val messagesRef = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
        
        val messageData = message.copy(id = "")
        val dataMap = mapOf(
            "senderId" to messageData.senderId,
            "text" to messageData.text,
            "timestamp" to messageData.timestamp
        )
        
        // Add message to subcollection
        messagesRef.add(dataMap).await()
        
        // Update chat session last message and timestamp
        firestore.collection("chats")
            .document(chatId)
            .update(
                mapOf(
                    "lastMessage" to messageData.text,
                    "updatedAt" to messageData.timestamp
                )
            )
            .await()
        
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create or get existing chat session between two participants
     * Returns the chat ID
     */
    suspend fun createChatIfNotExists(userId: String, tukangId: String): Result<String> {
        return try {
            if (userId.isEmpty() || tukangId.isEmpty()) {
                return Result.failure(IllegalArgumentException("User ID and Tukang ID cannot be empty"))
            }
            
            // Check if chat already exists
            val existingChats = firestore.collection("chats")
                .whereArrayContains("participants", userId)
                .get()
                .await()
            
            val existingChat = existingChats.documents.firstOrNull { doc ->
                val participants = doc.get("participants") as? List<*>
                participants?.contains(tukangId) == true && participants?.contains(userId) == true
            }
            
            if (existingChat != null) {
                // Return existing chat ID
                Result.success(existingChat.id)
            } else {
                // Create new chat session
                val chatData = mapOf(
                    "participants" to listOf(userId, tukangId),
                    "lastMessage" to "",
                    "updatedAt" to System.currentTimeMillis()
                )
                
                val docRef = firestore.collection("chats").add(chatData).await()
                Result.success(docRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe chat sessions for a specific user
     * Returns a Flow that emits List<ChatSession> whenever chats change
     */
    fun observeChatSessions(userId: String): Flow<List<ChatSession>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val chatsRef = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
        
        val listenerRegistration: ListenerRegistration = chatsRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val sessions = snapshot.documents.mapNotNull { doc ->
                        try {
                            ChatSession(
                                id = doc.id,
                                participants = (doc.get("participants") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList(),
                                lastMessage = doc.getString("lastMessage") ?: "",
                                updatedAt = doc.getLong("updatedAt") ?: 0L
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(sessions)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }
}

