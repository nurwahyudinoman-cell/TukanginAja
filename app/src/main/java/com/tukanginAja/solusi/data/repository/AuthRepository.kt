package com.tukanginAja.solusi.data.repository

import com.google.firebase.auth.FirebaseUser
import com.tukanginAja.solusi.data.remote.AuthRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    val currentUser: FirebaseUser?
        get() = authRemoteDataSource.currentUser
    
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return authRemoteDataSource.login(email, password)
    }
    
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return authRemoteDataSource.register(email, password)
    }
    
    suspend fun signOut() {
        authRemoteDataSource.signOut()
    }
    
    fun isUserLoggedIn(): Boolean {
        return authRemoteDataSource.isUserLoggedIn()
    }
}

