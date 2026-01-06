package com.bicheator.harammusic.data.remote.spotify.api


import com.bicheator.harammusic.data.remote.spotify.dto.SpotifySearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SpotifyApiService {

    @GET("v1/search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 20
    ): SpotifySearchResponseDto
}
