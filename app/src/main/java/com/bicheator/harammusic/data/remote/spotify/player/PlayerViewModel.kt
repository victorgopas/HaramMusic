package com.bicheator.harammusic.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.remote.spotify.player.SpotifyPlayerRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MiniPlayerUiState(
    val visible: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val isPlaying: Boolean = false,
    val error: String? = null
)

class PlayerViewModel(
    private val player: SpotifyPlayerRemoteDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(MiniPlayerUiState())
    val state: StateFlow<MiniPlayerUiState> = _state

    fun play(spotifyUri: String, title: String, artist: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null)

            when (val conn = player.connectIfNeeded()) {
                is AppResult.Error -> {
                    _state.value = _state.value.copy(error = conn.message)
                    return@launch
                }
                is AppResult.Success -> Unit
            }

            when (val res = player.playTrack(spotifyUri)) {
                is AppResult.Success -> {
                    _state.value = MiniPlayerUiState(
                        visible = true,
                        title = title,
                        subtitle = artist,
                        isPlaying = true
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(error = res.message)
                }
            }
        }
    }

    fun togglePlayPause() {
        val now = _state.value
        viewModelScope.launch {
            if (!player.isConnected()) {
                _state.value = now.copy(error = "Spotify no conectado")
                return@launch
            }
            if (now.isPlaying) player.pause() else player.resume()
            _state.value = now.copy(isPlaying = !now.isPlaying, error = null)
        }
    }

    fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        player.disconnect()
        super.onCleared()
    }
}
