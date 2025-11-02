package com.tukanginAja.solusi.features.order

import com.google.firebase.firestore.Timestamp
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for Order System
 */
class OrderSystemTest {
    
    @Test
    fun `OrderModel conversion test`() {
        // Given
        val data = mapOf<String, Any>(
            "userId" to "U1",
            "tukangId" to "T1",
            "status" to "Menunggu",
            "createdAt" to Timestamp.now(),
            "detail" to mapOf("serviceType" to "Listrik")
        )
        
        // When
        val model = OrderModel.fromMap("O1", data)
        
        // Then
        assertEquals("O1", model.orderId)
        assertEquals("U1", model.userId)
        assertEquals("T1", model.tukangId)
        assertEquals("Menunggu", model.status)
        assertTrue(model.detail.containsKey("serviceType"))
        assertEquals("Listrik", model.detail["serviceType"])
    }
    
    @Test
    fun `OrderModel toMap test`() {
        // Given
        val model = OrderModel(
            orderId = "O1",
            userId = "U1",
            tukangId = "T1",
            status = "Menunggu",
            createdAt = Date(),
            detail = mapOf("serviceType" to "Listrik")
        )
        
        // When
        val map = model.toMap()
        
        // Then
        assertEquals("U1", map["userId"])
        assertEquals("T1", map["tukangId"])
        assertEquals("Menunggu", map["status"])
        assertTrue(map.containsKey("detail"))
        assertTrue(map.containsKey("createdAt"))
    }
    
    @Test
    fun `OrderModel handles missing fields gracefully`() {
        // Given - missing some fields
        val data = mapOf<String, Any?>(
            "userId" to "U1",
            "tukangId" to null,  // Missing field
            "status" to null,    // Missing field
            "createdAt" to Timestamp.now(),
            "detail" to null     // Missing field
        )
        
        // When
        val model = OrderModel.fromMap("O1", data)
        
        // Then - should use defaults
        assertEquals("O1", model.orderId)
        assertEquals("U1", model.userId)
        assertEquals("", model.tukangId)  // Default empty string
        assertEquals("Menunggu", model.status)  // Default status
        assertTrue(model.detail.isEmpty())  // Default empty map
    }
}

