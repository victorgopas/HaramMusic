package com.bicheator.harammusic.ui.content.detail

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.core.util.IdGenerator
import com.bicheator.harammusic.data.repository.LibraryRepositorySqliteImpl
import com.bicheator.harammusic.domain.model.Review
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

data class SongDetailUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val song: LibraryRepositorySqliteImpl.SongDetail? = null,
    val reviews: List<Review> = emptyList(),
    val myReview: Review? = null
)

class SongDetailViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(SongDetailUiState())
    val state: StateFlow<SongDetailUiState> = _state.asStateFlow()

    fun load(songId: String) {
        scope.launch {
            _state.update { it.copy(loading = true, message = null) }

            val detailRes = withContext(Dispatchers.IO) { container.libraryRepository.getSongDetail(songId) }
            val reviewsRes = withContext(Dispatchers.IO) { container.reviewRepository.getReviewsForSong(songId) }

            val detail = when (detailRes) {
                is AppResult.Success<*> -> detailRes.data as LibraryRepositorySqliteImpl.SongDetail
                is AppResult.Error -> {
                    _state.update { it.copy(loading = false, message = detailRes.message) }
                    return@launch
                }
            }

            val reviews = when (reviewsRes) {
                is AppResult.Success<*> -> reviewsRes.data as List<Review>
                is AppResult.Error -> emptyList()
            }

            val me = container.sessionManager.getUserIdOrNull()
            val my = me?.let { uid -> reviews.firstOrNull { it.userId == uid } }

            _state.update { it.copy(loading = false, song = detail, reviews = reviews, myReview = my) }
        }
    }

    fun publishReview(songId: String, rating: Double, text: String?) {
        val userId = container.sessionManager.getUserIdOrNull()
        if (userId == null) {
            _state.update { it.copy(message = "Sesión no válida") }
            return
        }

        scope.launch {
            val res = withContext(Dispatchers.IO) {
                container.reviewRepository.rateSong(
                    Review(
                        id = IdGenerator.uuid(),
                        userId = userId,
                        songId = songId,
                        rating = rating,
                        text = text?.takeIf { it.isNotBlank() }
                    )
                )
            }

            when (res) {
                is AppResult.Success<*> -> load(songId) // recarga (media + lista)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun setSongGenres(songId: String, genres: List<String>) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.setSongGenres(songId, genres) }
            when (res) {
                is AppResult.Success<*> -> load(songId)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun setCoverBytes(songId: String, bytes: ByteArray) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.setSongCoverFromBytes(songId, bytes) }
            when (res) {
                is AppResult.Success<*> -> load(songId)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun adminUpdateReview(songId: String, reviewId: String, rating: Double, text: String?) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.updateReview(reviewId, rating, text) }
            when (res) {
                is AppResult.Success<*> -> load(songId)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun adminDeleteReview(songId: String, reviewId: String) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.deleteReview(reviewId) }
            when (res) {
                is AppResult.Success<*> -> load(songId)
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun deleteSong(songId: String, onDeleted: () -> Unit) {
        scope.launch {
            val res = withContext(Dispatchers.IO) { container.adminRepository.deleteSong(songId) }
            when (res) {
                is AppResult.Success<*> -> onDeleted()
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
            }
        }
    }

    fun clear() = scope.cancel()
}