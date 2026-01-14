package com.bicheator.harammusic.domain.model

data class PlaybackItem(
    val songId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val uriString: String,
    val coverPath: String?
)