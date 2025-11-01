package com.tukangin.modules.tukang

data class TukangModel(
    val id: String = "",
    val name: String = "",
    val serviceCategory: String = "",
    val rating: Double = 0.0,
    val available: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

