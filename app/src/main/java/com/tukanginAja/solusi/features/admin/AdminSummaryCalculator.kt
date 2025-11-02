package com.tukanginAja.solusi.features.admin

import com.tukanginAja.solusi.features.payment.TransactionModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculator for admin dashboard summary statistics
 * Provides methods to calculate completion rates, revenue splits, and other metrics
 */
@Singleton
class AdminSummaryCalculator @Inject constructor() {
    
    /**
     * Calculate completion rate percentage
     * Returns completion rate as percentage (0.0 - 100.0)
     * 
     * @param completed Number of completed orders
     * @param total Total number of orders
     * @return Completion rate percentage
     */
    fun calculateCompletionRate(completed: Int, total: Int): Double {
        return if (total == 0) {
            0.0
        } else {
            (completed.toDouble() / total.toDouble()) * 100.0
        }
    }
    
    /**
     * Calculate cancellation rate percentage
     * Returns cancellation rate as percentage (0.0 - 100.0)
     * 
     * @param cancelled Number of cancelled orders
     * @param total Total number of orders
     * @return Cancellation rate percentage
     */
    fun calculateCancellationRate(cancelled: Int, total: Int): Double {
        return if (total == 0) {
            0.0
        } else {
            (cancelled.toDouble() / total.toDouble()) * 100.0
        }
    }
    
    /**
     * Calculate revenue split from transactions
     * Returns Pair<totalPlatformEarnings, totalTukangEarnings>
     * 
     * @param transactions List of transactions to calculate from
     * @return Pair where first is total platform earnings, second is total tukang earnings
     */
    fun calculateRevenueSplit(transactions: List<TransactionModel>): Pair<Double, Double> {
        var totalPlatform = 0.0
        var totalTukang = 0.0
        
        for (transaction in transactions) {
            if (transaction.isSuccess) {  // Only count successful transactions
                totalPlatform += transaction.platformFee
                totalTukang += transaction.commission
            }
        }
        
        // Round to 2 decimal places
        return Pair(
            kotlin.math.round(totalPlatform * 100) / 100,
            kotlin.math.round(totalTukang * 100) / 100
        )
    }
    
    /**
     * Calculate daily earnings from transactions
     * Groups transactions by date and sums up platform fees
     * Returns Map<date, totalPlatformEarnings>
     * 
     * @param transactions List of transactions to calculate from
     * @return Map where key is date (YYYY-MM-DD) and value is total platform earnings
     */
    fun calculateDailyEarnings(transactions: List<TransactionModel>): Map<String, Double> {
        val dailyEarnings = mutableMapOf<String, Double>()
        
        for (transaction in transactions) {
            if (transaction.isSuccess) {
                val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date(transaction.createdAt))
                
                val currentEarnings = dailyEarnings.getOrDefault(date, 0.0)
                dailyEarnings[date] = currentEarnings + transaction.platformFee
            }
        }
        
        // Round all values to 2 decimal places
        return dailyEarnings.mapValues { 
            kotlin.math.round(it.value * 100) / 100 
        }
    }
    
    /**
     * Calculate average order value
     * Returns average transaction amount from successful transactions
     * 
     * @param transactions List of transactions to calculate from
     * @return Average order value or 0.0 if no transactions
     */
    fun calculateAverageOrderValue(transactions: List<TransactionModel>): Double {
        val successfulTransactions = transactions.filter { it.isSuccess }
        
        return if (successfulTransactions.isNotEmpty()) {
            successfulTransactions.map { it.amount }.average()
        } else {
            0.0
        }
    }
}

