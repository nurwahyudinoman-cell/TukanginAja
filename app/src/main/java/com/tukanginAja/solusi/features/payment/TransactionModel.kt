package com.tukanginAja.solusi.features.payment

/**
 * Data class representing a transaction in Firestore
 * 
 * Example Firestore document:
 * {
 *   "transactionId": "tx001",
 *   "orderId": "ord001",
 *   "userId": "u001",
 *   "tukangId": "t001",
 *   "amount": 100000.0,
 *   "commission": 90000.0,
 *   "platformFee": 10000.0,
 *   "status": "SUCCESS",
 *   "createdAt": 1730400000000
 * }
 */
data class TransactionModel(
    val transactionId: String = "",
    val orderId: String = "",
    val userId: String = "",
    val tukangId: String = "",
    val amount: Double = 0.0,
    val commission: Double = 0.0,
    val platformFee: Double = 0.0,
    val status: String = "PENDING", // PENDING, SUCCESS, FAILED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create TransactionModel from Firestore document
         */
        fun fromMap(id: String, data: Map<String, Any?>): TransactionModel {
            return TransactionModel(
                transactionId = id,
                orderId = data["orderId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                tukangId = data["tukangId"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                commission = (data["commission"] as? Number)?.toDouble() ?: 0.0,
                platformFee = (data["platformFee"] as? Number)?.toDouble() ?: 0.0,
                status = data["status"] as? String ?: "PENDING",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Convert TransactionModel to Firestore-compatible map
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "orderId" to orderId,
            "userId" to userId,
            "tukangId" to tukangId,
            "amount" to amount,
            "commission" to commission,
            "platformFee" to platformFee,
            "status" to status,
            "createdAt" to createdAt
        )
    }
    
    /**
     * Check if transaction is successful
     */
    val isSuccess: Boolean
        get() = status == "SUCCESS"
    
    /**
     * Check if transaction is pending
     */
    val isPending: Boolean
        get() = status == "PENDING"
    
    /**
     * Check if transaction failed
     */
    val isFailed: Boolean
        get() = status == "FAILED"
}

