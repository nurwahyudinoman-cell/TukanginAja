package com.tukanginAja.solusi.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PHASE 2: Core App Stabilization
 * AuthManager provides consolidated authentication operations
 * Wraps Firebase Auth for easier testing and usage
 */
@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    /**
     * Register a new user with email and password
     * Returns Result.success(FirebaseUser) on success or Result.failure(exception) on error
     */
    suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
            }
            
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("User creation returned null"))
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Login user with email and password
     * Returns Result.success(FirebaseUser) on success or Result.failure(exception) on error
     */
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
            }
            
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.failure(IllegalStateException("Login returned null user"))
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logout current user
     * Returns Result.success(Unit) on success or Result.failure(exception) on error
     */
    suspend fun logoutUser(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current logged-in user
     * Returns FirebaseUser if logged in, null otherwise
     */
    fun currentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    /**
     * Check if user is currently logged in
     * Returns true if user is logged in, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    /**
     * Get current user ID
     * Returns user ID if logged in, empty string otherwise
     */
    fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }
}

