package com.tukanginAja.solusi.features.chat

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Chat System
 */
class ChatSystemTest {
    
    @Test
    fun `test ChatModel defaults`() {
        // Given
        val chat = ChatModel(
            senderId = "U1",
            receiverId = "T1",
            message = "Test"
        )
        
        // Then
        assertEquals("Test", chat.message)
        assertEquals("U1", chat.senderId)
        assertEquals("T1", chat.receiverId)
        assertTrue(chat.createdAt > 0)
    }
    
    @Test
    fun `test ChatModel fromMap`() {
        // Given
        val data = mapOf<String, Any?>(
            "senderId" to "U1",
            "receiverId" to "T1",
            "orderId" to "O1",
            "message" to "Test message",
            "createdAt" to 1730400000000L
        )
        
        // When
        val chat = ChatModel.fromMap("msg001", data)
        
        // Then
        assertEquals("msg001", chat.messageId)
        assertEquals("U1", chat.senderId)
        assertEquals("T1", chat.receiverId)
        assertEquals("O1", chat.orderId)
        assertEquals("Test message", chat.message)
        assertEquals(1730400000000L, chat.createdAt)
    }
    
    @Test
    fun `test ChatModel toMap`() {
        // Given
        val chat = ChatModel(
            messageId = "msg001",
            senderId = "U1",
            receiverId = "T1",
            orderId = "O1",
            message = "Test message",
            createdAt = 1730400000000L
        )
        
        // When
        val map = chat.toMap()
        
        // Then
        assertEquals("U1", map["senderId"])
        assertEquals("T1", map["receiverId"])
        assertEquals("O1", map["orderId"])
        assertEquals("Test message", map["message"])
        assertEquals(1730400000000L, map["createdAt"])
    }
    
    @Test
    fun `test ChatModel handles missing fields gracefully`() {
        // Given - missing some fields
        val data = mapOf<String, Any?>(
            "senderId" to "U1",
            "receiverId" to null,  // Missing field
            "orderId" to null,     // Missing field
            "message" to "Test",
            "createdAt" to null    // Missing field
        )
        
        // When
        val chat = ChatModel.fromMap("msg001", data)
        
        // Then - should use defaults
        assertEquals("msg001", chat.messageId)
        assertEquals("U1", chat.senderId)
        assertEquals("", chat.receiverId)  // Default empty string
        assertEquals("", chat.orderId)      // Default empty string
        assertEquals("Test", chat.message)
        assertTrue(chat.createdAt > 0)     // Should default to current time
    }
}

