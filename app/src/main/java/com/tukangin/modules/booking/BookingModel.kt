package com.tukangin.modules.booking

data class BookingModel(
    val id: String = "",
    val userId: String = "",
    val tukangId: String = "",
    val status: String = BookingStatus.PENDING,
    val scheduledAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val price: Double = 0.0
)

object BookingStatus {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val ONGOING = "ongoing"
    const val COMPLETED = "completed"
    const val CANCELLED = "cancelled"
}

