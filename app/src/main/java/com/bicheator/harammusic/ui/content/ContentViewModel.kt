package com.bicheator.harammusic.ui.content

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.repository.AlbumRow
import com.bicheator.harammusic.data.repository.ArtistRow
import com.bicheator.harammusic.data.repository.SongCardRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ContentTab { ARTISTS, ALBUMS, SONGS }

data class ContentUiState(
    val query: String = "",
    val tab: ContentTab = ContentTab.ARTISTS,
    val loading: Boolean = false,
    val message: String? = null,

    val artists: List<ArtistRow> = emptyList(),
    val albums: List<AlbumRow> = emptyList(),
    val songs: List<SongCardRow> = emptyList()
)

class ContentViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(ContentUiState())
    val state: StateFlow<ContentUiState> = _state

    fun setQuery(q: String) {
        _state.update { it.copy(query = q) }
        refreshDebounced()
    }

    fun setTab(t: ContentTab) {
        _state.update { it.copy(tab = t) }
        refresh()
    }

    private var job: Job? = null
    private fun refreshDebounced() {
        job?.cancel()
        job = scope.launch { delay(250); refresh() }
    }

    fun refresh() {
        val q = _state.value.query.trim()

        scope.launch {
            _state.update { it.copy(loading = true, message = null) }

            when (_state.value.tab) {
                ContentTab.ARTISTS -> when (val res = container.libraryRepository.searchArtists(q.ifEmpty { "" })) {
                    is AppResult.Success<*> -> _state.update {
                        it.copy(artists = res.data as List<ArtistRow>, loading = false)
                    }
                    is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
                }
                ContentTab.ALBUMS -> when (val res = container.libraryRepository.searchAlbums(q.ifEmpty { "" })) {
                    is AppResult.Success<*> -> _state.update {
                        it.copy(albums = res.data as List<AlbumRow>, loading = false)
                    }
                    is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
                }

                ContentTab.SONGS -> when (val res = container.libraryRepository.searchSongsForContent(q.ifEmpty { "" })) {
                    is AppResult.Success<*> -> _state.update {
                        it.copy(songs = res.data as List<SongCardRow>, loading = false)
                    }
                    is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
                }
            }
        }
    }

    fun clear() = scope.cancel()
}