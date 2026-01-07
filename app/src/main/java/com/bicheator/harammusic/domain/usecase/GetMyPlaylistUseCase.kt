package com.bicheator.harammusic.domain.usecase

import com.bicheator.harammusic.domain.repository.PlaylistRepository

class GetMyPlaylistsUseCase(
    private val repo: PlaylistRepository
) {
    suspend operator fun invoke(userId: String) = repo.getMyPlaylists(userId)
}
