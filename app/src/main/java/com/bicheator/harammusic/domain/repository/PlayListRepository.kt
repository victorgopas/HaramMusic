package com.bicheator.harammusic.domain.repository

import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.Playlist

interface PlaylistRepository {
    suspend fun createPlaylist(ownerUserId: String, name: String, songIds: List<String>): AppResult<Playlist>
    suspend fun updatePlaylist(playlist: Playlist): AppResult<Playlist>
    suspend fun getMyPlaylists(userId: String): AppResult<List<Playlist>>
    suspend fun getPublicPlaylists(query: String): AppResult<List<Playlist>>
}
