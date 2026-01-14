package com.bicheator.harammusic.ui.playlist

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.repository.PlaylistSongRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaylistDetailUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val songs: List<PlaylistSongRow> = emptyList(),
    val canRemoveSongs: Boolean = false,
    val canRenameDelete: Boolean = false,
    val playlistName: String = "Playlist"
)

class PlaylistDetailViewModel(
    private val container: AppContainer,
    private val playlistId: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(PlaylistDetailUiState())
    val state: StateFlow<PlaylistDetailUiState> = _state

    fun load() {
        scope.launch {
            _state.update { it.copy(loading = true, message = null) }

            val type = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.getPlaylistType(playlistId)
            }

            val name = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.getPlaylistName(playlistId)
            }
            val canRemove = (type == "CUSTOM" || type == "FAVORITES")
            val canRenameDelete = (type == "CUSTOM")

            //val editable = (type == "CUSTOM")
            _state.update { it.copy(canRemoveSongs = canRemove, canRenameDelete = canRenameDelete, playlistName = name) }

            val res = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.getPlaylistSongs(playlistId)
            }

            when (res) {
                is AppResult.Success -> _state.update { it.copy(loading = false, songs = res.data) }
                is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
            }
        }
    }

    fun rename(newName: String) {
        val n = newName.trim()
        if (n.isEmpty()) {
            _state.update { it.copy(message = "Nombre inválido") }
            return
        }
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.renamePlaylist(playlistId, n)
            }
            when (res) {
                is AppResult.Success -> _state.update { it.copy(message = "Renombrada", playlistName = newName.trim()) }
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.deletePlaylist(playlistId)
            }
            when (res) {
                is AppResult.Success -> onDeleted()
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun removeSong(songId: String) {
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.removeSongFromPlaylist(playlistId, songId)
            }
            when (res) {
                is AppResult.Success -> load()
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun clear() = scope.cancel()
}