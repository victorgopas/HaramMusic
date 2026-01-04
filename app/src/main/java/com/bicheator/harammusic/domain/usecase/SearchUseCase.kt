package com.bicheator.harammusic.domain.usecase


import com.bicheator.harammusic.domain.repository.MusicRepository

class SearchUseCase(
    private val musicRepository: MusicRepository
) {
    suspend fun songs(q: String) = musicRepository.searchSongs(q)
    suspend fun albums(q: String) = musicRepository.searchAlbums(q)
    suspend fun artists(q: String) = musicRepository.searchArtists(q)
}
