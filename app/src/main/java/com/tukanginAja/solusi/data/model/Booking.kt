package com.tukanginAja.solusi.data.model

/**
 * Data class representing a booking between user and tukang.
 */
data class Booking(
    val id: String = "",
    val userId: String = "",
    val tukangId: String = "",
    val serviceCategoryId: String = "",
    val status: String = "pending", // pending, accepted, in_progress, completed, cancelled
    val scheduledAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

