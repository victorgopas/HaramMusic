package com.bicheator.harammusic.domain.model

data class Playlist(
    val id: String,
    val ownerUserId: String,
    val name: String,
    val isPublic: Boolean = false,
    val songIds: List<String> = emptyList()
)
