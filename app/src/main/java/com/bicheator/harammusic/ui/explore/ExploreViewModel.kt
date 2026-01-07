package com.bicheator.harammusic.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.Artist
import com.bicheator.harammusic.domain.model.Song
import com.bicheator.harammusic.domain.usecase.GetExploreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExploreUiState(
    val loading: Boolean = false,
    val suggestedSongs: List<Song> = emptyList(),
    val suggestedArtists: List<Artist> = emptyList(),
    val error: String? = null
)

class ExploreViewModel(
    private val getExploreUseCase: GetExploreUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = ExploreUiState(loading = true)
            when (val res = getExploreUseCase()) {
                is AppResult.Success -> _state.value = ExploreUiState(
                    suggestedSongs = res.data.first,
                    suggestedArtists = res.data.second
                )
                is AppResult.Error -> _state.value = ExploreUiState(error = res.message)
            }
        }
    }
}
