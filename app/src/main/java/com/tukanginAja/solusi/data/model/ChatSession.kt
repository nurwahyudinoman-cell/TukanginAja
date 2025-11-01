package com.tukanginAja.solusi.data.model

/**
 * Data class representing a chat session in Firestore
 * 
 * Example Firestore document:
 * {
 *   "id": "chat001",
 *   "participants": ["u001", "t001"],
 *   "lastMessage": "Halo, saya butuh bantuan AC",
 *   "updatedAt": 1730400000000
 * }
 */
data class ChatSession(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

