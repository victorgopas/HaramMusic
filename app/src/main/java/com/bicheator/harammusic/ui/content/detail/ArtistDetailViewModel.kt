package com.bicheator.harammusic.ui.content.detail

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.repository.LibraryRepositorySqliteImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ArtistDetailUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val artist: LibraryRepositorySqliteImpl.ArtistDetail? = null
)

class ArtistDetailViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(ArtistDetailUiState())
    val state: StateFlow<ArtistDetailUiState> = _state.asStateFlow()

    fun load(artistId: String) {
        scope.launch {
            _state.update { it.copy(loading = true, message = null) }
            val res = withContext(Dispatchers.IO) { container.libraryRepository.getArtistDetail(artistId) }
            when (res) {
                is AppResult.Success<*> -> _state.update {
                    it.copy(loading = false, artist = res.data as LibraryRepositorySqliteImpl.ArtistDetail)
                }
                is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
            }
        }
    }

    fun updateBio(artistId: String, bio: String?) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.updateArtistBio(artistId, bio) }
            when (res) {
                is AppResult.Success<*> -> load(artistId)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun deleteArtist(artistId: String, onDeleted: () -> Unit) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.deleteArtist(artistId) }
            when (res) {
                is AppResult.Success<*> -> onDeleted()
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun clear() = scope.cancel()
}