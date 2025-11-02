package com.tukanginAja.solusi.features.rating

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for calculating and updating trust scores for tukangs
 * Trust Score Formula: (avgRating × 0.7) + (completionRate × 100 × 0.3)
 * 
 * - avgRating: Average rating from all submitted ratings (0.0 - 5.0)
 * - completionRate: Percentage of completed orders (0.0 - 1.0)
 */
@Singleton
class TrustScoreCalculator @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val ratingService: RatingService
) {
    private val ordersCollection = firestore.collection("orders")
    
    /**
     * Calculate and update trust score for a specific tukang
     * Updates the trustScore field in tukang_locations collection
     * Returns Result.success(trustScore) on success or Result.failure(exception) on error
     */
    suspend fun updateTrustScore(tukangId: String): Result<Double> {
        return try {
            if (tukangId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Tukang ID cannot be empty"))
            }
            
            // Calculate average rating
            val avgRating = ratingService.calculateAverageRating(tukangId)
            
            // Fetch order completion rate
            val ordersSnapshot = ordersCollection
                .whereEqualTo("tukangId", tukangId)
                .get()
                .await()
            
            val total = ordersSnapshot.size()
            val completed = ordersSnapshot.documents.count { 
                it.getString("status") == "Selesai" 
            }
            
            val completionRate = if (total > 0) {
                completed.toDouble() / total
            } else {
                0.0
            }
            
            // Calculate trust score: (avgRating × 0.7) + (completionRate × 100 × 0.3)
            val trustScore = (avgRating * 0.7) + (completionRate * 100 * 0.3)
            
            // Update trust score in tukang_locations collection
            // Note: Using tukang_locations as per existing project structure
            firestore.collection("tukang_locations")
                .document(tukangId)
                .update(mapOf("trustScore" to trustScore))
                .await()
            
            Result.success(trustScore)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current trust score for a specific tukang
     * Returns trust score or 0.0 if not found
     */
    suspend fun getTrustScore(tukangId: String): Double {
        return try {
            if (tukangId.isEmpty()) {
                return 0.0
            }
            
            val doc = firestore.collection("tukang_locations")
                .document(tukangId)
                .get()
                .await()
            
            if (doc.exists()) {
                (doc.get("trustScore") as? Number)?.toDouble() ?: 0.0
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
    
    /**
     * Calculate completion rate for a specific tukang
     * Returns completion rate (0.0 - 1.0)
     */
    suspend fun calculateCompletionRate(tukangId: String): Double {
        return try {
            val ordersSnapshot = ordersCollection
                .whereEqualTo("tukangId", tukangId)
                .get()
                .await()
            
            val total = ordersSnapshot.size()
            if (total == 0) return 0.0
            
            val completed = ordersSnapshot.documents.count { 
                it.getString("status") == "Selesai" 
            }
            
            completed.toDouble() / total
        } catch (e: Exception) {
            0.0
        }
    }
}

