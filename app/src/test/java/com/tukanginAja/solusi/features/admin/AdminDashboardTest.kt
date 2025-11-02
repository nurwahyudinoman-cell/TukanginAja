package com.tukanginAja.solusi.features.admin

import com.tukanginAja.solusi.features.payment.TransactionModel
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Admin Dashboard System
 */
class AdminDashboardTest {
    
    @Test
    fun `test CompletionRateCalculation - standard calculation`() {
        // Given
        val calc = AdminSummaryCalculator()
        
        // When
        val rate = calc.calculateCompletionRate(5, 10)
        
        // Then
        assertEquals(50.0, rate, 0.01)
    }
    
    @Test
    fun `test CompletionRateCalculation - zero total`() {
        // Given
        val calc = AdminSummaryCalculator()
        
        // When
        val rate = calc.calculateCompletionRate(0, 0)
        
        // Then
        assertEquals(0.0, rate, 0.01)
    }
    
    @Test
    fun `test CompletionRateCalculation - all completed`() {
        // Given
        val calc = AdminSummaryCalculator()
        
        // When
        val rate = calc.calculateCompletionRate(10, 10)
        
        // Then
        assertEquals(100.0, rate, 0.01)
    }
    
    @Test
    fun `test CancellationRateCalculation`() {
        // Given
        val calc = AdminSummaryCalculator()
        
        // When
        val rate = calc.calculateCancellationRate(2, 10)
        
        // Then
        assertEquals(20.0, rate, 0.01)
    }
    
    @Test
    fun `test RevenueSplitCalculation - standard calculation`() {
        // Given
        val calc = AdminSummaryCalculator()
        val transactions = listOf(
            TransactionModel(
                commission = 90000.0,
                platformFee = 10000.0,
                status = "SUCCESS"
            ),
            TransactionModel(
                commission = 45000.0,
                platformFee = 5000.0,
                status = "SUCCESS"
            )
        )
        
        // When
        val (platform, tukang) = calc.calculateRevenueSplit(transactions)
        
        // Then
        assertEquals(15000.0, platform, 0.01)
        assertEquals(135000.0, tukang, 0.01)
    }
    
    @Test
    fun `test RevenueSplitCalculation - only successful transactions`() {
        // Given
        val calc = AdminSummaryCalculator()
        val transactions = listOf(
            TransactionModel(
                commission = 90000.0,
                platformFee = 10000.0,
                status = "SUCCESS"
            ),
            TransactionModel(
                commission = 45000.0,
                platformFee = 5000.0,
                status = "FAILED"  // Should be ignored
            ),
            TransactionModel(
                commission = 45000.0,
                platformFee = 5000.0,
                status = "PENDING"  // Should be ignored
            )
        )
        
        // When
        val (platform, tukang) = calc.calculateRevenueSplit(transactions)
        
        // Then - only first transaction should be counted
        assertEquals(10000.0, platform, 0.01)
        assertEquals(90000.0, tukang, 0.01)
    }
    
    @Test
    fun `test RevenueSplitCalculation - empty list`() {
        // Given
        val calc = AdminSummaryCalculator()
        val transactions = emptyList<TransactionModel>()
        
        // When
        val (platform, tukang) = calc.calculateRevenueSplit(transactions)
        
        // Then
        assertEquals(0.0, platform, 0.01)
        assertEquals(0.0, tukang, 0.01)
    }
    
    @Test
    fun `test DailyEarningsCalculation`() {
        // Given
        val calc = AdminSummaryCalculator()
        val transactions = listOf(
            TransactionModel(
                platformFee = 10000.0,
                status = "SUCCESS",
                createdAt = 1730400000000L  // Fixed date for testing
            ),
            TransactionModel(
                platformFee = 5000.0,
                status = "SUCCESS",
                createdAt = 1730400000000L  // Same date
            )
        )
        
        // When
        val dailyEarnings = calc.calculateDailyEarnings(transactions)
        
        // Then
        assertTrue(dailyEarnings.isNotEmpty())
        val totalEarnings = dailyEarnings.values.sum()
        assertEquals(15000.0, totalEarnings, 0.01)
    }
    
    @Test
    fun `test AverageOrderValueCalculation`() {
        // Given
        val calc = AdminSummaryCalculator()
        val transactions = listOf(
            TransactionModel(
                amount = 100000.0,
                status = "SUCCESS"
            ),
            TransactionModel(
                amount = 200000.0,
                status = "SUCCESS"
            ),
            TransactionModel(
                amount = 150000.0,
                status = "FAILED"  // Should be ignored
            )
        )
        
        // When
        val avgValue = calc.calculateAverageOrderValue(transactions)
        
        // Then - average of 100000 and 200000
        assertEquals(150000.0, avgValue, 0.01)
    }
    
    @Test
    fun `test AdminDashboardModel completionRate`() {
        // Given
        val dashboard = AdminDashboardModel(
            totalOrders = 100,
            completedOrders = 80
        )
        
        // Then
        assertEquals(80.0, dashboard.completionRate, 0.01)
    }
    
    @Test
    fun `test AdminDashboardModel cancellationRate`() {
        // Given
        val dashboard = AdminDashboardModel(
            totalOrders = 100,
            cancelledOrders = 10
        )
        
        // Then
        assertEquals(10.0, dashboard.cancellationRate, 0.01)
    }
    
    @Test
    fun `test AdminDashboardModel totalRevenue`() {
        // Given
        val dashboard = AdminDashboardModel(
            totalPlatformEarnings = 100000.0,
            totalTukangEarnings = 900000.0
        )
        
        // Then
        assertEquals(1000000.0, dashboard.totalRevenue, 0.01)
    }
    
    @Test
    fun `test AdminDashboardModel isEmpty`() {
        // Given
        val emptyDashboard = AdminDashboardModel()
        val nonEmptyDashboard = AdminDashboardModel(
            totalOrders = 10,
            totalUsers = 5,
            totalTukang = 2
        )
        
        // Then
        assertTrue(emptyDashboard.isEmpty)
        assertFalse(nonEmptyDashboard.isEmpty)
    }
}

