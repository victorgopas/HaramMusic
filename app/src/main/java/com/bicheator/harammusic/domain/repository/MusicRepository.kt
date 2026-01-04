package com.bicheator.harammusic.domain.repository

import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.*

interface MusicRepository {
    suspend fun searchSongs(query: String): AppResult<List<Song>>
    suspend fun searchAlbums(query: String): AppResult<List<Album>>
    suspend fun searchArtists(query: String): AppResult<List<Artist>>

    suspend fun getSongDetail(id: String): AppResult<Song>
    suspend fun getAlbumDetail(id: String): AppResult<Album>
}
