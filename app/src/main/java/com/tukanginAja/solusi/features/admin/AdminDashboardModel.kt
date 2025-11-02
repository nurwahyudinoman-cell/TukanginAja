package com.tukanginAja.solusi.features.admin

/**
 * Data class representing admin dashboard summary data
 * Contains aggregated statistics for orders, users, tukangs, and earnings
 * 
 * Example usage:
 * {
 *   totalOrders: 150,
 *   completedOrders: 120,
 *   cancelledOrders: 10,
 *   totalUsers: 500,
 *   totalTukang: 50,
 *   totalPlatformEarnings: 1500000.0,
 *   totalTukangEarnings: 13500000.0,
 *   activeOrders: 20,
 *   dailyEarnings: {"2024-01-01": 500000.0, "2024-01-02": 600000.0}
 * }
 */
data class AdminDashboardModel(
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalUsers: Int = 0,
    val totalTukang: Int = 0,
    val totalPlatformEarnings: Double = 0.0,
    val totalTukangEarnings: Double = 0.0,
    val activeOrders: Int = 0,
    val dailyEarnings: Map<String, Double> = emptyMap()
) {
    /**
     * Calculate completion rate percentage
     * Returns completion rate as percentage (0.0 - 100.0)
     */
    val completionRate: Double
        get() = if (totalOrders > 0) {
            (completedOrders.toDouble() / totalOrders.toDouble()) * 100.0
        } else {
            0.0
        }
    
    /**
     * Calculate cancellation rate percentage
     * Returns cancellation rate as percentage (0.0 - 100.0)
     */
    val cancellationRate: Double
        get() = if (totalOrders > 0) {
            (cancelledOrders.toDouble() / totalOrders.toDouble()) * 100.0
        } else {
            0.0
        }
    
    /**
     * Calculate total revenue (platform + tukang earnings)
     * Returns total revenue from all transactions
     */
    val totalRevenue: Double
        get() = totalPlatformEarnings + totalTukangEarnings
    
    /**
     * Check if dashboard data is empty
     */
    val isEmpty: Boolean
        get() = totalOrders == 0 && totalUsers == 0 && totalTukang == 0
}

