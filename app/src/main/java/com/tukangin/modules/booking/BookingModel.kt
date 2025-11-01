package com.tukangin.modules.booking

data class BookingModel(
    val id: String = "",
    val userId: String = "",
    val tukangId: String = "",
    val serviceType: String = "",
    val status: String = "pending", // pending, accepted, completed, cancelled
    val scheduledAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val notes: String = ""
)

