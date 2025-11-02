package com.tukanginAja.solusi.features.payment

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculator for commission and platform fee
 * Formula: 90% for Tukang, 10% for Platform
 */
@Singleton
class CommissionCalculator @Inject constructor() {
    
    companion object {
        /**
         * Platform fee percentage (10%)
         */
        const val PLATFORM_FEE_PERCENTAGE = 0.1
        
        /**
         * Tukang earning percentage (90%)
         */
        const val TUKANG_EARNING_PERCENTAGE = 0.9
    }
    
    /**
     * Calculate commission and platform fee from total amount
     * Returns Pair<tukangEarning, platformFee>
     * 
     * @param amount Total transaction amount
     * @return Pair where first is tukang earning (90%), second is platform fee (10%)
     */
    fun calculate(amount: Double): Pair<Double, Double> {
        if (amount < 0) {
            throw IllegalArgumentException("Amount cannot be negative")
        }
        
        val platformFee = amount * PLATFORM_FEE_PERCENTAGE   // 10% untuk platform
        val tukangEarning = amount * TUKANG_EARNING_PERCENTAGE // 90% untuk tukang
        
        // Ensure rounding to 2 decimal places
        return Pair(
            kotlin.math.round(tukangEarning * 100) / 100,
            kotlin.math.round(platformFee * 100) / 100
        )
    }
    
    /**
     * Calculate only platform fee
     * Returns platform fee (10% of amount)
     */
    fun calculatePlatformFee(amount: Double): Double {
        if (amount < 0) {
            throw IllegalArgumentException("Amount cannot be negative")
        }
        
        return kotlin.math.round((amount * PLATFORM_FEE_PERCENTAGE) * 100) / 100
    }
    
    /**
     * Calculate only tukang earning
     * Returns tukang earning (90% of amount)
     */
    fun calculateTukangEarning(amount: Double): Double {
        if (amount < 0) {
            throw IllegalArgumentException("Amount cannot be negative")
        }
        
        return kotlin.math.round((amount * TUKANG_EARNING_PERCENTAGE) * 100) / 100
    }
}

