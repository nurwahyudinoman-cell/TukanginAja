package com.tukanginAja.solusi.features.rating

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Rating System
 */
class RatingSystemTest {
    
    @Test
    fun `test RatingModel defaults`() {
        // Given
        val rating = RatingModel(
            score = 4.5,
            comment = "Good job"
        )
        
        // Then
        assertEquals(4.5, rating.score, 0.0)
        assertEquals("Good job", rating.comment)
        assertTrue(rating.createdAt > 0)
    }
    
    @Test
    fun `test RatingModel fromMap`() {
        // Given
        val data = mapOf<String, Any?>(
            "orderId" to "O1",
            "userId" to "U1",
            "tukangId" to "T1",
            "score" to 4.5,
            "comment" to "Good job",
            "createdAt" to 1730400000000L
        )
        
        // When
        val rating = RatingModel.fromMap("r001", data)
        
        // Then
        assertEquals("r001", rating.ratingId)
        assertEquals("O1", rating.orderId)
        assertEquals("U1", rating.userId)
        assertEquals("T1", rating.tukangId)
        assertEquals(4.5, rating.score, 0.0)
        assertEquals("Good job", rating.comment)
        assertEquals(1730400000000L, rating.createdAt)
    }
    
    @Test
    fun `test RatingModel toMap`() {
        // Given
        val rating = RatingModel(
            ratingId = "r001",
            orderId = "O1",
            userId = "U1",
            tukangId = "T1",
            score = 4.5,
            comment = "Good job",
            createdAt = 1730400000000L
        )
        
        // When
        val map = rating.toMap()
        
        // Then
        assertEquals("O1", map["orderId"])
        assertEquals("U1", map["userId"])
        assertEquals("T1", map["tukangId"])
        assertEquals(4.5, (map["score"] as? Number)?.toDouble(), 0.0)
        assertEquals("Good job", map["comment"])
        assertEquals(1730400000000L, map["createdAt"])
    }
    
    @Test
    fun `test RatingModel validation - valid rating`() {
        // Given
        val rating = RatingModel(
            orderId = "O1",
            userId = "U1",
            tukangId = "T1",
            score = 4.5
        )
        
        // Then
        assertTrue(rating.isValid())
    }
    
    @Test
    fun `test RatingModel validation - invalid score too high`() {
        // Given
        val rating = RatingModel(
            orderId = "O1",
            userId = "U1",
            tukangId = "T1",
            score = 6.0  // Invalid: > 5.0
        )
        
        // Then
        assertFalse(rating.isValid())
    }
    
    @Test
    fun `test RatingModel validation - invalid score too low`() {
        // Given
        val rating = RatingModel(
            orderId = "O1",
            userId = "U1",
            tukangId = "T1",
            score = -1.0  // Invalid: < 0.0
        )
        
        // Then
        assertFalse(rating.isValid())
    }
    
    @Test
    fun `test RatingModel validation - missing orderId`() {
        // Given
        val rating = RatingModel(
            orderId = "",  // Missing
            userId = "U1",
            tukangId = "T1",
            score = 4.5
        )
        
        // Then
        assertFalse(rating.isValid())
    }
    
    @Test
    fun `test RatingModel validation - missing userId`() {
        // Given
        val rating = RatingModel(
            orderId = "O1",
            userId = "",  // Missing
            tukangId = "T1",
            score = 4.5
        )
        
        // Then
        assertFalse(rating.isValid())
    }
    
    @Test
    fun `test RatingModel validation - missing tukangId`() {
        // Given
        val rating = RatingModel(
            orderId = "O1",
            userId = "U1",
            tukangId = "",  // Missing
            score = 4.5
        )
        
        // Then
        assertFalse(rating.isValid())
    }
}

