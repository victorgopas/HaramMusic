package com.bicheator.harammusic.ui.player

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.PlaybackItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class PlayerUiState(
    val queue: List<PlaybackItem> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val endedAtEnd: Boolean = false,
    val message: String? = null
) {
    val current: PlaybackItem? get() = queue.getOrNull(currentIndex)
}

class PlayerViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state

    private var tickerJob: Job? = null

    fun playFromPlaylist(
        playlistId: String,
        playlistType: String,
        songId: String
    ) {
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                container.playbackQueueRepository.getQueueForPlaylist(playlistId, playlistType)
            }

            when (res) {
                is AppResult.Error -> _state.update { it.copy(message = res.message) }
                is AppResult.Success -> {
                    val queue = res.data
                    if (queue.isEmpty()) {
                        _state.update { it.copy(message = "No hay canciones reproducibles en esta playlist") }
                        return@launch
                    }

                    val startIndex = queue.indexOfFirst { it.songId == songId }.let { if (it >= 0) it else 0 }
                    container.playerEngine.setQueueAndPlay(queue.map { it.uriString }, startIndex)

                    _state.update {
                        it.copy(
                            queue = queue,
                            currentIndex = startIndex,
                            isPlaying = true,
                            message = null
                        )
                    }

                    startTicker()
                }
            }
        }
    }

    fun togglePlayPause() {
        val st = _state.value
        if (st.endedAtEnd) {
            container.playerEngine.seekTo(0L)
            container.playerEngine.playPauseToggle()
            syncIndexAndTimes()
            return
        }

        container.playerEngine.playPauseToggle()
        _state.update { it.copy(isPlaying = container.playerEngine.isPlaying()) }
    }

    fun next() {
        container.playerEngine.next()
        scope.launch { syncIndexAndTimesSafe() }
    }

    fun prev() {
        container.playerEngine.prev()
        scope.launch { syncIndexAndTimesSafe() }
    }

    fun seekTo(ms: Long) {
        container.playerEngine.seekTo(ms)
        scope.launch { syncIndexAndTimesSafe() }
    }

    fun currentSongId(): String? = _state.value.current?.songId

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                syncIndexAndTimesSafe()
                delay(400)
            }
        }
    }

    private suspend fun syncIndexAndTimesSafe() {
        // Leer ExoPlayer en MAIN para evitar crash por thread
        val snapshot = withContext(Dispatchers.Main.immediate) {
            val idx = container.playerEngine.currentIndex().coerceAtLeast(0)
            val playing = container.playerEngine.isPlaying()
            val pos = container.playerEngine.currentPosition().coerceAtLeast(0L)
            val dur = container.playerEngine.duration().coerceAtLeast(0L)
            Quad(idx, playing, pos, dur)
        }

        val idx = snapshot.a
        val playing = snapshot.b
        val pos = snapshot.c
        val dur = snapshot.d

        _state.update { st ->
            val safeIdx = idx.coerceIn(0, (st.queue.size - 1).coerceAtLeast(0))
            val lastIndex = st.queue.lastIndex
            val ended = st.queue.isNotEmpty() &&
                    safeIdx == lastIndex &&
                    !playing &&
                    dur > 0L &&
                    pos >= (dur - 350L)

            st.copy(
                currentIndex = safeIdx,
                isPlaying = playing,
                positionMs = pos,
                durationMs = dur,
                endedAtEnd = ended
            )
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    private fun syncIndexAndTimes() {
        val idx = container.playerEngine.currentIndex().coerceAtLeast(0)
        val playing = container.playerEngine.isPlaying()
        val pos = container.playerEngine.currentPosition().coerceAtLeast(0L)
        val dur = container.playerEngine.duration().coerceAtLeast(0L)

        _state.update { st ->
            val safeIdx = idx.coerceIn(0, (st.queue.size - 1).coerceAtLeast(0))
            val lastIndex = (st.queue.size - 1)

            val ended = (st.queue.isNotEmpty()
                    && safeIdx == lastIndex
                    && !playing
                    && dur > 0L
                    && pos >= (dur - 350L))

            st.copy(
                currentIndex = safeIdx,
                isPlaying = playing,
                positionMs = pos,
                durationMs = dur,
                endedAtEnd = ended
            )
        }
    }

    fun clear() {
        tickerJob?.cancel()
        scope.cancel()
    }
}