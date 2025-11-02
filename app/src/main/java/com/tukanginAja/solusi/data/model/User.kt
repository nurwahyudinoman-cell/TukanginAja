package com.tukanginAja.solusi.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val role: String = "user", // Default role: user, tukang, or admin
    val createdAt: Long = System.currentTimeMillis()
)

