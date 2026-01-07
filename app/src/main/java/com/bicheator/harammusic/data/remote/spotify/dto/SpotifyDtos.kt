package com.bicheator.harammusic.data.remote.spotify.dto

import com.squareup.moshi.Json

data class SpotifySearchResponseDto(
    @Json(name = "tracks") val tracks: TracksDto
)

data class TracksDto(
    @Json(name = "items") val items: List<TrackDto>
)

data class TrackDto(
    val id: String,
    val name: String,
    val uri: String,
    @Json(name = "preview_url") val previewUrl: String?,
    @Json(name = "duration_ms") val durationMs: Long,
    val album: AlbumDto,
    val artists: List<ArtistDto>
)

data class AlbumDto(
    val id: String,
    val name: String,
    val images: List<ImageDto>
)


data class ImageDto(
    val url: String
)
data class SpotifySearchMultiResponseDto(
    @Json(name="tracks") val tracks: TracksDto? = null,
    @Json(name="artists") val artists: ArtistsDto? = null
)

data class ArtistItemDto(
    val id: String,
    val name: String,
    val uri: String,
    val images: List<ImageDto> = emptyList()
)
