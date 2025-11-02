package com.tukanginAja.solusi.data.repository

import com.google.firebase.auth.FirebaseUser
import com.tukanginAja.solusi.data.firebase.FirestoreHelper
import com.tukanginAja.solusi.data.remote.AuthRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val firestoreHelper: FirestoreHelper
) {
    val currentUser: FirebaseUser?
        get() = authRemoteDataSource.currentUser
    
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        val loginResult = authRemoteDataSource.login(email, password)
        
        // After successful login, fetch and cache user role
        loginResult.onSuccess { user ->
            try {
                val roleResult = firestoreHelper.getUserRole(email)
                val role = roleResult.getOrNull() ?: "user"
                // Role will be saved to SharedPreferences in AuthViewModel or LoginUseCase
            } catch (e: Exception) {
                // Continue even if role fetch fails
            }
        }
        
        return loginResult
    }
    
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return authRemoteDataSource.register(email, password)
    }
    
    suspend fun getUserRole(email: String): Result<String> {
        return firestoreHelper.getUserRole(email).map { it ?: "user" }
    }
    
    /**
     * Get user role by UID (recommended method - uses users/{uid} path)
     */
    suspend fun getUserRoleByUid(uid: String): Result<String> {
        return firestoreHelper.getUserRoleByUid(uid).map { it ?: "user" }
    }
    
    suspend fun signOut() {
        authRemoteDataSource.signOut()
    }
    
    fun isUserLoggedIn(): Boolean {
        return authRemoteDataSource.isUserLoggedIn()
    }
}

