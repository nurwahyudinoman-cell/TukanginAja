package com.tukanginAja.solusi.features.rating

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling rating operations with Firestore
 * Provides methods to submit, retrieve, and calculate ratings
 */
@Singleton
class RatingService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val ratingsCollection = firestore.collection("ratings")
    
    /**
     * Submit a new rating
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun submitRating(rating: RatingModel): Result<Unit> {
        return try {
            if (!rating.isValid()) {
                return Result.failure(IllegalArgumentException("Invalid rating data"))
            }
            
            val newRating = rating.copy(
                createdAt = System.currentTimeMillis()
            )
            
            ratingsCollection.add(newRating.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all ratings for a specific tukang
     * Returns List<RatingModel> or empty list if no ratings found
     */
    suspend fun getRatingsForTukang(tukangId: String): List<RatingModel> {
        return try {
            if (tukangId.isEmpty()) {
                return emptyList()
            }
            
            val snapshot = ratingsCollection
                .whereEqualTo("tukangId", tukangId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    RatingModel.fromMap(doc.id, data)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Calculate average rating for a specific tukang
     * Returns average rating (0.0 to 5.0) or 0.0 if no ratings found
     */
    suspend fun calculateAverageRating(tukangId: String): Double {
        val ratings = getRatingsForTukang(tukangId)
        return if (ratings.isNotEmpty()) {
            ratings.map { it.score }.average()
        } else {
            0.0
        }
    }
    
    /**
     * Get rating count for a specific tukang
     * Returns number of ratings submitted
     */
    suspend fun getRatingCount(tukangId: String): Int {
        return getRatingsForTukang(tukangId).size
    }
    
    /**
     * Check if a user has already rated a specific order
     * Returns true if rating exists, false otherwise
     */
    suspend fun hasUserRatedOrder(userId: String, orderId: String): Boolean {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("orderId", orderId)
                .get()
                .await()
            
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}

