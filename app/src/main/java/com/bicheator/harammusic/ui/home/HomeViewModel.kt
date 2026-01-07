package com.bicheator.harammusic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.Playlist
import com.bicheator.harammusic.domain.usecase.GetMyPlaylistsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val getMyPlaylists: GetMyPlaylistsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = HomeUiState(loading = true)
            when (val res = getMyPlaylists(userId)) {
                is AppResult.Success -> _state.value = HomeUiState(playlists = res.data)
                is AppResult.Error -> _state.value = HomeUiState(error = res.message)
            }
        }
    }
}
