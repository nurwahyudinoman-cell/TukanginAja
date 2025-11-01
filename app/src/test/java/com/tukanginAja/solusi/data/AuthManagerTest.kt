package com.tukanginAja.solusi.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*
import kotlinx.coroutines.tasks.await

/**
 * PHASE 2: Core App Stabilization
 * Unit tests for AuthManager authentication operations
 */
class AuthManagerTest {
    
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var authManager: AuthManager
    
    @Before
    fun setup() {
        mockFirebaseAuth = mock()
        authManager = AuthManager(mockFirebaseAuth)
    }
    
    @Test
    fun `registerUser should return failure when email is blank`() = runTest {
        // Given
        val email = ""
        val password = "password123"
        
        // When
        val result = authManager.registerUser(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `registerUser should return failure when password is blank`() = runTest {
        // Given
        val email = "test@example.com"
        val password = ""
        
        // When
        val result = authManager.registerUser(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `loginUser should return failure when email is blank`() = runTest {
        // Given
        val email = ""
        val password = "password123"
        
        // When
        val result = authManager.loginUser(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `loginUser should return failure when password is blank`() = runTest {
        // Given
        val email = "test@example.com"
        val password = ""
        
        // When
        val result = authManager.loginUser(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
    
    @Test
    fun `logoutUser should return success`() = runTest {
        // Given - FirebaseAuth.signOut() returns void, so no mocking needed
        
        // When
        val result = authManager.logoutUser()
        
        // Then
        assertTrue(result.isSuccess)
        verify(mockFirebaseAuth).signOut()
    }
    
    @Test
    fun `currentUser should return null when no user logged in`() {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)
        
        // When
        val user = authManager.currentUser()
        
        // Then
        assertNull(user)
    }
    
    @Test
    fun `currentUser should return user when logged in`() {
        // Given
        val mockUser = mock<FirebaseUser>()
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockUser)
        
        // When
        val user = authManager.currentUser()
        
        // Then
        assertNotNull(user)
        assertEquals(mockUser, user)
    }
    
    @Test
    fun `isUserLoggedIn should return false when no user`() {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)
        
        // When
        val isLoggedIn = authManager.isUserLoggedIn()
        
        // Then
        assertFalse(isLoggedIn)
    }
    
    @Test
    fun `isUserLoggedIn should return true when user exists`() {
        // Given
        val mockUser = mock<FirebaseUser>()
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockUser)
        
        // When
        val isLoggedIn = authManager.isUserLoggedIn()
        
        // Then
        assertTrue(isLoggedIn)
    }
    
    @Test
    fun `getCurrentUserId should return empty string when no user`() {
        // Given
        whenever(mockFirebaseAuth.currentUser).thenReturn(null)
        
        // When
        val userId = authManager.getCurrentUserId()
        
        // Then
        assertEquals("", userId)
    }
    
    @Test
    fun `getCurrentUserId should return user ID when logged in`() {
        // Given
        val mockUser = mock<FirebaseUser>()
        whenever(mockUser.uid).thenReturn("user123")
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockUser)
        
        // When
        val userId = authManager.getCurrentUserId()
        
        // Then
        assertEquals("user123", userId)
    }
    
    // Note: Full integration tests for registerUser and loginUser would require
    // Firebase Test SDK or integration test setup with real Firebase emulator
    // These tests validate the structure and basic validation logic
}

