package com.tukanginAja.solusi.data.model

/**
 * Data class representing a tukang (service provider).
 */
data class Tukang(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val skills: List<String> = emptyList(),
    val isActive: Boolean = true,
    val rating: Double = 0.0,
    val completedJobs: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

