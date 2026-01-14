package com.bicheator.harammusic.domain.model

data class Review(
    val id: String,
    val userId: String,
    val songId: String,
    val rating: Double,
    val text: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val userDisplayName: String? = null
)