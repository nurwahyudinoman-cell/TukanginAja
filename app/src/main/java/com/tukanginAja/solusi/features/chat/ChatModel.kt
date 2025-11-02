package com.tukanginAja.solusi.features.chat

/**
 * Data class representing a chat message in Firestore
 * Messages are stored in orders/{orderId}/messages subcollection
 * 
 * Example Firestore document:
 * {
 *   "messageId": "msg001",
 *   "senderId": "u001",
 *   "receiverId": "t001",
 *   "orderId": "ord001",
 *   "message": "Halo, kapan bisa dikerjakan?",
 *   "createdAt": 1730400000000
 * }
 */
data class ChatModel(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val orderId: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create ChatModel from Firestore document
         */
        fun fromMap(id: String, data: Map<String, Any?>): ChatModel {
            return ChatModel(
                messageId = id,
                senderId = data["senderId"] as? String ?: "",
                receiverId = data["receiverId"] as? String ?: "",
                orderId = data["orderId"] as? String ?: "",
                message = data["message"] as? String ?: "",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Convert ChatModel to Firestore-compatible map
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "orderId" to orderId,
            "message" to message,
            "createdAt" to createdAt
        )
    }
}

