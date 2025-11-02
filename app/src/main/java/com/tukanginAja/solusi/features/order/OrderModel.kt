package com.tukanginAja.solusi.features.order

import com.google.firebase.firestore.Timestamp

/**
 * Data class representing an order in Firestore
 * 
 * Example Firestore document:
 * {
 *   "orderId": "O1",
 *   "userId": "U1",
 *   "tukangId": "T1",
 *   "status": "Menunggu",
 *   "createdAt": Timestamp,
 *   "detail": {
 *     "serviceType": "Listrik",
 *     "description": "Perbaikan listrik"
 *   }
 * }
 */
data class OrderModel(
    val orderId: String = "",
    val userId: String = "",
    val tukangId: String = "",
    val status: String = "Menunggu",
    val createdAt: java.util.Date = java.util.Date(),
    val detail: Map<String, Any> = emptyMap()
) {
    companion object {
        /**
         * Create OrderModel from Firestore document
         */
        fun fromMap(id: String, data: Map<String, Any?>): OrderModel {
            val timestamp = data["createdAt"]
            val createdAtDate = when (timestamp) {
                is Timestamp -> timestamp.toDate()
                is java.util.Date -> timestamp
                is Long -> java.util.Date(timestamp)
                else -> java.util.Date()
            }
            
            return OrderModel(
                orderId = id,
                userId = data["userId"] as? String ?: "",
                tukangId = data["tukangId"] as? String ?: "",
                status = data["status"] as? String ?: "Menunggu",
                createdAt = createdAtDate,
                detail = (data["detail"] as? Map<String, Any>) ?: emptyMap()
            )
        }
    }
    
    /**
     * Convert OrderModel to Firestore-compatible map
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "tukangId" to tukangId,
            "status" to status,
            "createdAt" to Timestamp(createdAt),
            "detail" to detail
        )
    }
}

