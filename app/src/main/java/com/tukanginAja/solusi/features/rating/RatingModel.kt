package com.tukanginAja.solusi.features.rating

/**
 * Data class representing a rating in Firestore
 * 
 * Example Firestore document:
 * {
 *   "ratingId": "r001",
 *   "orderId": "ord001",
 *   "userId": "u001",
 *   "tukangId": "t001",
 *   "score": 4.5,
 *   "comment": "Kerja bagus, on time",
 *   "createdAt": 1730400000000
 * }
 */
data class RatingModel(
    val ratingId: String = "",
    val orderId: String = "",
    val userId: String = "",
    val tukangId: String = "",
    val score: Double = 0.0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create RatingModel from Firestore document
         */
        fun fromMap(id: String, data: Map<String, Any?>): RatingModel {
            return RatingModel(
                ratingId = id,
                orderId = data["orderId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                tukangId = data["tukangId"] as? String ?: "",
                score = (data["score"] as? Number)?.toDouble() ?: 0.0,
                comment = data["comment"] as? String ?: "",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Convert RatingModel to Firestore-compatible map
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "orderId" to orderId,
            "userId" to userId,
            "tukangId" to tukangId,
            "score" to score,
            "comment" to comment,
            "createdAt" to createdAt
        )
    }
    
    /**
     * Validate rating score (must be between 0.0 and 5.0)
     */
    fun isValid(): Boolean {
        return score in 0.0..5.0 && orderId.isNotEmpty() && userId.isNotEmpty() && tukangId.isNotEmpty()
    }
}

