package com.bicheator.harammusic.data.remote.spotify.player

import com.bicheator.harammusic.ui.player.MiniPlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel (
    // private val spotifyPlayer: SpotifyPlayerRemoteDataSource,
    // private val playbackLogRepo: PlaybackLogRepositorySqlite,
    // private val sessionManager: SessionManager
) {
    private val _state = MutableStateFlow(MiniPlayerUiState())
    val state: StateFlow<MiniPlayerUiState> = _state

    fun show(trackTitle: String, artist: String, isPlaying: Boolean) {
        _state.value = MiniPlayerUiState(
            visible = true,
            title = trackTitle,
            subtitle = artist,
            isPlaying = isPlaying
        )
    }

    fun togglePlayPause() {
        val now = _state.value
        _state.value = now.copy(isPlaying = !now.isPlaying)
        // TODO: spotifyPlayer.pause()/resume() + log
    }
}