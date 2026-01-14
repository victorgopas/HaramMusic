package com.bicheator.harammusic.ui.songs

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.repository.PlaylistLite
import com.bicheator.harammusic.data.repository.SongRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SongsUiState(
    val playlists: List<PlaylistLite> = emptyList(),
    val selectedPlaylist: PlaylistLite? = null,
    val favoritesPlaylistId: String? = null,

    val query: String = "",
    val filterArtist: String = "",
    val filterAlbum: String = "",
    val filterMenuOpen: Boolean = false,

    val songs: List<SongRow> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null
)

class SongsViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(SongsUiState())
    val state: StateFlow<SongsUiState> = _state.asStateFlow()

    fun load() {
        scope.launch {
            val userId = container.sessionManager.getUserIdOrNull()
            if (userId == null) {
                _state.update { it.copy(message = "Sesión no válida") }
                return@launch
            }

            _state.update { it.copy(loading = true, message = null) }
            when (val res = container.libraryRepository.getPlaylistsForUser(userId)) {
                is AppResult.Success -> {
                    val pls = res.data
                    val selected = pls.firstOrNull()
                    val favId = pls.firstOrNull { it.type == "FAVORITES" }?.id

                    _state.update {
                        it.copy(
                            playlists = pls,
                            selectedPlaylist = selected,
                            favoritesPlaylistId = favId,
                            loading = false
                        )
                    }
                    refreshSongs()
                }
                is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
            }
        }
    }

    fun selectPlaylist(pl: PlaylistLite) {
        _state.update { it.copy(selectedPlaylist = pl) }
        refreshSongs()
    }

    fun setQuery(q: String) {
        _state.update { it.copy(query = q) }
        refreshSongsDebounced()
    }

    fun toggleFilterMenu() {
        _state.update { it.copy(filterMenuOpen = !it.filterMenuOpen) }
    }

    fun setFilterArtist(v: String) {
        _state.update { it.copy(filterArtist = v) }
        refreshSongsDebounced()
    }

    fun setFilterAlbum(v: String) {
        _state.update { it.copy(filterAlbum = v) }
        refreshSongsDebounced()
    }

    fun addSongToPlaylist(songId: String, playlistId: String, onResultMessage: (String) -> Unit) {
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.addSongToPlaylist(playlistId, songId)
            }
            when (res) {
                is AppResult.Success -> {
                    onResultMessage("Canción añadida a la playlist")
                    refreshSongs()
                    loadPlaylistsOnly()
                }
                is AppResult.Error -> onResultMessage(res.message)
            }
        }
    }

    private fun loadPlaylistsOnly() {
        scope.launch {
            val userId = container.sessionManager.getUserIdOrNull() ?: return@launch
            val res = withContext(Dispatchers.IO) { container.libraryRepository.getPlaylistsForUser(userId) }
            if (res is AppResult.Success<*>) {
                @Suppress("UNCHECKED_CAST")
                val pls = res.data as List<PlaylistLite>
                _state.update { it.copy(playlists = pls) }
            }
        }
    }

    fun toggleFavorite(songId: String, isFavorite: Boolean, onResultMessage: (String) -> Unit) {
        val favId = _state.value.favoritesPlaylistId
        if (favId == null) {
            onResultMessage("No existe la playlist de Favoritas")
            return
        }

        scope.launch {
            val res = withContext(Dispatchers.IO) {
                if (isFavorite) {
                    container.playlistManagementRepository.removeSongFromPlaylist(favId, songId)
                } else {
                    container.playlistManagementRepository.addSongToPlaylist(favId, songId)
                }
            }

            when (res) {
                is AppResult.Success -> {
                    onResultMessage(if (isFavorite) "Quitada de Favoritas" else "Añadida a Favoritas")
                    refreshSongs()
                }
                is AppResult.Error -> onResultMessage(res.message)
            }
        }
    }

    private var refreshJob: Job? = null

    private fun refreshSongsDebounced() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            delay(250)
            refreshSongs()
        }
    }

    private fun refreshSongs() {
        val pl = _state.value.selectedPlaylist ?: return
        scope.launch {
            _state.update { it.copy(loading = true, message = null) }
            val res = withContext(Dispatchers.IO) {
                container.libraryRepository.getPlayableSongs(
                    playlistId = pl.id,
                    playlistType = pl.type,
                    query = _state.value.query,
                    filterArtist = _state.value.filterArtist.ifBlank { null },
                    filterAlbum = _state.value.filterAlbum.ifBlank { null },
                    favoritesPlaylistId = _state.value.favoritesPlaylistId
                )
            }
            when (res) {
                is AppResult.Success -> _state.update { it.copy(songs = res.data, loading = false) }
                is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
            }
        }
    }

    fun clear() = scope.cancel()
}