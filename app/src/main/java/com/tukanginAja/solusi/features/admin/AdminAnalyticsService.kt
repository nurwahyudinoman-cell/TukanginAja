package com.tukanginAja.solusi.features.admin

import com.google.firebase.firestore.FirebaseFirestore
import com.tukanginAja.solusi.features.payment.TransactionModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling admin analytics operations with Firestore
 * Provides methods to fetch dashboard statistics and calculate metrics
 */
@Singleton
class AdminAnalyticsService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val calculator: AdminSummaryCalculator
) {
    private val ordersCollection = firestore.collection("orders")
    private val transactionsCollection = firestore.collection("transactions")
    private val usersCollection = firestore.collection("users")
    private val tukangCollection = firestore.collection("tukang_locations")
    
    /**
     * Get complete dashboard summary
     * Fetches data from all collections and calculates metrics
     * Returns Result.success(AdminDashboardModel) on success or Result.failure(exception) on error
     */
    suspend fun getDashboardSummary(): Result<AdminDashboardModel> {
        return try {
            // Fetch all data in parallel
            val ordersSnapshot = ordersCollection.get().await()
            val transactionsSnapshot = transactionsCollection.get().await()
            val usersSnapshot = usersCollection.get().await()
            val tukangSnapshot = tukangCollection.get().await()
            
            // Calculate order statistics
            val totalOrders = ordersSnapshot.size()
            val completedOrders = ordersSnapshot.documents.count { 
                it.getString("status") == "Selesai" || it.getString("status") == "done"
            }
            val cancelledOrders = ordersSnapshot.documents.count { 
                it.getString("status") == "Dibatalkan" || it.getString("status") == "cancelled"
            }
            val activeOrders = totalOrders - (completedOrders + cancelledOrders)
            
            // Calculate user and tukang counts
            val totalUsers = usersSnapshot.size()
            val totalTukang = tukangSnapshot.size()
            
            // Parse transactions
            val transactions = transactionsSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    TransactionModel.fromMap(doc.id, data)
                } catch (e: Exception) {
                    null
                }
            }
            
            // Calculate revenue split
            val (platformEarnings, tukangEarnings) = calculator.calculateRevenueSplit(transactions)
            
            // Calculate daily earnings
            val dailyEarnings = calculator.calculateDailyEarnings(transactions)
            
            // Build dashboard model
            val dashboard = AdminDashboardModel(
                totalOrders = totalOrders,
                completedOrders = completedOrders,
                cancelledOrders = cancelledOrders,
                totalUsers = totalUsers,
                totalTukang = totalTukang,
                totalPlatformEarnings = platformEarnings,
                totalTukangEarnings = tukangEarnings,
                activeOrders = activeOrders,
                dailyEarnings = dailyEarnings
            )
            
            Result.success(dashboard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get dashboard summary with caching
     * Returns cached data if available, otherwise fetches fresh data
     * This is a simplified version - in production, implement proper caching
     */
    suspend fun getDashboardSummaryCached(): Result<AdminDashboardModel> {
        // For now, just return fresh data
        // In production, implement proper caching mechanism
        return getDashboardSummary()
    }
    
    /**
     * Refresh dashboard summary (force fetch from Firestore)
     * Same as getDashboardSummary() but explicit for cache invalidation
     */
    suspend fun refreshDashboardSummary(): Result<AdminDashboardModel> {
        return getDashboardSummary()
    }
}

