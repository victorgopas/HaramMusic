package com.bicheator.harammusic.ui.playlist

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.repository.PlaylistRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaylistsUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val playlists: List<PlaylistRow> = emptyList(),
    val showCreateDialog: Boolean = false
)

class PlaylistsViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(PlaylistsUiState())
    val state: StateFlow<PlaylistsUiState> = _state

    fun load() {
        scope.launch {
            val userId = container.sessionManager.getUserIdOrNull()
            if (userId == null) {
                _state.update { it.copy(message = "Sesión no válida") }
                return@launch
            }

            withContext(Dispatchers.IO) {
                container.playlistManagementRepository.ensureSystemPlaylists(userId)
            }

            _state.update { it.copy(loading = true, message = null) }
            val res = withContext(Dispatchers.IO) { container.playlistManagementRepository.getPlaylists(userId) }

            when (res) {
                is AppResult.Success -> _state.update { it.copy(loading = false, playlists = res.data) }
                is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
            }
        }
    }

    fun openCreateDialog() = _state.update { it.copy(showCreateDialog = true) }
    fun closeCreateDialog() = _state.update { it.copy(showCreateDialog = false) }

    fun create(name: String) {
        val n = name.trim()

        if (n.isEmpty()) {
            _state.update { it.copy(showCreateDialog = false, message = "El nombre no puede estar vacío") }
            return
        }

        scope.launch {
            val userId = container.sessionManager.getUserIdOrNull() ?: return@launch
            val res = withContext(Dispatchers.IO) {
                container.playlistManagementRepository.createCustomPlaylist(userId, n)
            }
            when (res) {
                is AppResult.Success -> {
                    _state.update { it.copy(showCreateDialog = false, message = "Playlist creada") }
                    load()
                }
                is AppResult.Error ->
                {
                    val raw = res.message.lowercase()
                    val friendly =
                        if (raw.contains("unique") || raw.contains("constraint") || raw.contains("duplic")) {
                            "Ya existe una playlist con ese nombre"
                        } else {
                            res.message
                        }

                    _state.update { it.copy(showCreateDialog = false, message = friendly) }
                }
            }
        }
    }

    fun clearMessage() { _state.update { it.copy(message = null) } }

    fun clear() = scope.cancel()
}