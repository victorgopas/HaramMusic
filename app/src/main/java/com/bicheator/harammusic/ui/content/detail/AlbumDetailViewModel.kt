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

data class AlbumDetailUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val album: LibraryRepositorySqliteImpl.AlbumDetail? = null
)

class AlbumDetailViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(AlbumDetailUiState())
    val state: StateFlow<AlbumDetailUiState> = _state.asStateFlow()

    fun load(albumId: String) {
        scope.launch {
            _state.update { it.copy(loading = true, message = null) }
            val res = withContext(Dispatchers.IO) { container.libraryRepository.getAlbumDetail(albumId) }
            when (res) {
                is AppResult.Success<*> -> _state.update {
                    it.copy(loading = false, album = res.data as LibraryRepositorySqliteImpl.AlbumDetail)
                }
                is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
            }
        }
    }

    fun updateDescription(albumId: String, description: String?) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.updateAlbumDescription(albumId, description) }
            when (res) {
                is AppResult.Success<*> -> load(albumId)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun setCoverBytes(albumId: String, bytes: ByteArray) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.setAlbumCoverFromBytes(albumId, bytes) }
            when (res) {
                is AppResult.Success<*> -> load(albumId)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun deleteAlbum(albumId: String, onDeleted: () -> Unit) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.deleteAlbum(albumId) }
            when (res) {
                is AppResult.Success<*> -> onDeleted()
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun clear() = scope.cancel()
}