package com.bicheator.harammusic.data.remote.spotify.dto

import com.squareup.moshi.Json

data class SpotifySearchArtistsResponseDto(
    @Json(name = "artists") val artists: ArtistsDto
)

data class ArtistsDto(
    @Json(name = "items") val items: List<ArtistDto>
)

data class ArtistDto(
    val id: String,
    val name: String,
    val uri: String,
    val images: List<ImageDto> = emptyList()
)
