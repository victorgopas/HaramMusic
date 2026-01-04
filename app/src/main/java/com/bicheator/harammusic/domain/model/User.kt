package com.bicheator.harammusic.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val displayName: String? = null,
    val role: Role = Role.USER,
    val isBlocked: Boolean = false
)
