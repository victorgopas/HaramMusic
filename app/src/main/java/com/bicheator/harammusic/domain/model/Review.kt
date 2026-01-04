package com.bicheator.harammusic.domain.model

data class Review(
    val id: String,
    val userId: String,
    val contentId: String,
    val contentType: ContentType,
    val rating: Int,          // 1..5
    val text: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
