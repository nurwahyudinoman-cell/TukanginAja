package com.tukanginAja.solusi.data.model

/**
 * Data class representing a chat message in Firestore
 * 
 * Example Firestore document:
 * {
 *   "id": "msg001",
 *   "senderId": "u001",
 *   "text": "Halo, saya butuh bantuan AC",
 *   "timestamp": 1730400000000
 * }
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

