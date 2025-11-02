package com.tukanginAja.solusi.features.payment

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Payment System
 */
class PaymentSystemTest {
    
    @Test
    fun `test CommissionCalculation - standard calculation`() {
        // Given
        val calc = CommissionCalculator()
        
        // When
        val (tukang, platform) = calc.calculate(100000.0)
        
        // Then
        assertEquals(90000.0, tukang, 0.01)  // 90% for tukang
        assertEquals(10000.0, platform, 0.01)  // 10% for platform
        assertEquals(100000.0, tukang + platform, 0.01)  // Total should equal original amount
    }
    
    @Test
    fun `test CommissionCalculation - zero amount`() {
        // Given
        val calc = CommissionCalculator()
        
        // When
        val (tukang, platform) = calc.calculate(0.0)
        
        // Then
        assertEquals(0.0, tukang, 0.01)
        assertEquals(0.0, platform, 0.01)
    }
    
    @Test
    fun `test CommissionCalculation - negative amount throws exception`() {
        // Given
        val calc = CommissionCalculator()
        
        // Then
        assertThrows(IllegalArgumentException::class.java) {
            calc.calculate(-1000.0)
        }
    }
    
    @Test
    fun `test CommissionCalculation - decimal amounts`() {
        // Given
        val calc = CommissionCalculator()
        
        // When
        val (tukang, platform) = calc.calculate(150000.0)
        
        // Then
        assertEquals(135000.0, tukang, 0.01)  // 90% of 150000
        assertEquals(15000.0, platform, 0.01)  // 10% of 150000
        assertEquals(150000.0, tukang + platform, 0.01)
    }
    
    @Test
    fun `test TransactionModel defaults`() {
        // Given
        val transaction = TransactionModel(amount = 100000.0)
        
        // Then
        assertEquals("PENDING", transaction.status)
        assertEquals(100000.0, transaction.amount, 0.0)
        assertTrue(transaction.createdAt > 0)
        assertTrue(transaction.isPending)
        assertFalse(transaction.isSuccess)
        assertFalse(transaction.isFailed)
    }
    
    @Test
    fun `test TransactionModel success status`() {
        // Given
        val transaction = TransactionModel(
            status = "SUCCESS",
            amount = 100000.0
        )
        
        // Then
        assertTrue(transaction.isSuccess)
        assertFalse(transaction.isPending)
        assertFalse(transaction.isFailed)
    }
    
    @Test
    fun `test TransactionModel fromMap`() {
        // Given
        val data = mapOf<String, Any?>(
            "orderId" to "O1",
            "userId" to "U1",
            "tukangId" to "T1",
            "amount" to 100000.0,
            "commission" to 90000.0,
            "platformFee" to 10000.0,
            "status" to "SUCCESS",
            "createdAt" to 1730400000000L
        )
        
        // When
        val transaction = TransactionModel.fromMap("tx001", data)
        
        // Then
        assertEquals("tx001", transaction.transactionId)
        assertEquals("O1", transaction.orderId)
        assertEquals("U1", transaction.userId)
        assertEquals("T1", transaction.tukangId)
        assertEquals(100000.0, transaction.amount, 0.0)
        assertEquals(90000.0, transaction.commission, 0.0)
        assertEquals(10000.0, transaction.platformFee, 0.0)
        assertEquals("SUCCESS", transaction.status)
        assertEquals(1730400000000L, transaction.createdAt)
    }
    
    @Test
    fun `test TransactionModel toMap`() {
        // Given
        val transaction = TransactionModel(
            transactionId = "tx001",
            orderId = "O1",
            userId = "U1",
            tukangId = "T1",
            amount = 100000.0,
            commission = 90000.0,
            platformFee = 10000.0,
            status = "SUCCESS",
            createdAt = 1730400000000L
        )
        
        // When
        val map = transaction.toMap()
        
        // Then
        assertEquals("O1", map["orderId"])
        assertEquals("U1", map["userId"])
        assertEquals("T1", map["tukangId"])
        assertEquals(100000.0, (map["amount"] as? Number)?.toDouble(), 0.0)
        assertEquals(90000.0, (map["commission"] as? Number)?.toDouble(), 0.0)
        assertEquals(10000.0, (map["platformFee"] as? Number)?.toDouble(), 0.0)
        assertEquals("SUCCESS", map["status"])
        assertEquals(1730400000000L, map["createdAt"])
    }
    
    @Test
    fun `test CommissionCalculator calculatePlatformFee`() {
        // Given
        val calc = CommissionCalculator()
        
        // When
        val platformFee = calc.calculatePlatformFee(100000.0)
        
        // Then
        assertEquals(10000.0, platformFee, 0.01)  // 10% of 100000
    }
    
    @Test
    fun `test CommissionCalculator calculateTukangEarning`() {
        // Given
        val calc = CommissionCalculator()
        
        // When
        val tukangEarning = calc.calculateTukangEarning(100000.0)
        
        // Then
        assertEquals(90000.0, tukangEarning, 0.01)  // 90% of 100000
    }
    
    @Test
    fun `test CommissionFormula consistency`() {
        // Given
        val calc = CommissionCalculator()
        val testAmounts = listOf(10000.0, 50000.0, 100000.0, 200000.0, 500000.0)
        
        // When & Then
        testAmounts.forEach { amount ->
            val (tukang, platform) = calc.calculate(amount)
            assertEquals(amount, tukang + platform, 0.01, "Total should equal original amount for $amount")
            assertEquals(amount * 0.9, tukang, 0.01, "Tukang should get 90% for $amount")
            assertEquals(amount * 0.1, platform, 0.01, "Platform should get 10% for $amount")
        }
    }
}

