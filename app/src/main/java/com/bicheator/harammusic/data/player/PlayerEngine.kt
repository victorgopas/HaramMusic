package com.bicheator.harammusic.data.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri


class PlayerEngine(context: Context) {
    private val player = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        setHandleAudioBecomingNoisy(true)
    }

    fun setQueueAndPlay(uris: List<String>, startIndex: Int) {
        val items = uris.map { MediaItem.fromUri(it.toUri()) }
        player.setMediaItems(items, startIndex, 0L)
        player.prepare()
        player.playWhenReady = true
    }

    fun playPauseToggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() = player.seekToNext()
    fun prev() = player.seekToPrevious()

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun isPlaying(): Boolean = player.isPlaying
    fun currentIndex(): Int = player.currentMediaItemIndex
    fun currentPosition(): Long = player.currentPosition
    fun duration(): Long = player.duration.coerceAtLeast(0L)

    fun release() = player.release()
}