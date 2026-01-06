package com.bicheator.harammusic.domain.model

data class Song(
    val id: String,
    val title: String,
    val artistId: String,
    val albumId: String? = null,
    val previewUrl: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val imageUrl: String? = null,
    val spotifyUri: String? = null,
    val durationMs: Long? = null
)
