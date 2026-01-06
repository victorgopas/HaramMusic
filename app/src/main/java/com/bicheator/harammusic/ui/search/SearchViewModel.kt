package com.bicheator.harammusic.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.Song
import com.bicheator.harammusic.domain.usecase.SearchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val loading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)

class SearchViewModel(
    private val searchUseCase: SearchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    fun search(query: String) {
        val q = query.trim()
        if (q.isBlank()) {
            _state.value = SearchUiState(error = "Escribe algo para buscar")
            return
        }

        viewModelScope.launch {
            _state.value = SearchUiState(loading = true)

            when (val res = searchUseCase.songs(q)) {
                is AppResult.Success -> _state.value = SearchUiState(songs = res.data)
                is AppResult.Error -> _state.value = SearchUiState(error = res.message)
            }
        }
    }
}
