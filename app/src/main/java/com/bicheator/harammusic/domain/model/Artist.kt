package com.bicheator.harammusic.domain.model

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val spotifyUri: String? = null
)
