package com.bicheator.harammusic.domain.usecase


import com.bicheator.harammusic.domain.repository.PlaylistRepository

class CreatePlaylistUseCase(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(ownerUserId: String, name: String, songIds: List<String>) =
        playlistRepository.createPlaylist(ownerUserId, name, songIds)
}
