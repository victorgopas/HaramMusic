package com.bicheator.harammusic.domain.model

data class Album(
    val id: String,
    val title: String,
    val artistId: String,
    val releaseDate: String? = null
)
