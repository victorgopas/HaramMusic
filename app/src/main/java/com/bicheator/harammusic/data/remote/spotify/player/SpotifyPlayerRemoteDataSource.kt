package com.bicheator.harammusic.data.remote.spotify.player

import android.content.Context
import com.bicheator.harammusic.core.result.AppResult
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class SpotifyPlayerRemoteDataSource(
    private val context: Context,
    private val clientId: String,
    private val redirectUri: String
) {
    private var appRemote: SpotifyAppRemote? = null

    fun isConnected(): Boolean = appRemote != null

    suspend fun connect(): AppResult<Unit> = suspendCancellableCoroutine { cont ->
        val params = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, params, object : Connector.ConnectionListener {
            override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                appRemote = spotifyAppRemote
                if (cont.isActive) cont.resume(AppResult.Success(Unit))
            }

            override fun onFailure(throwable: Throwable) {
                appRemote = null
                if (cont.isActive) cont.resume(AppResult.Error("No se pudo conectar con Spotify App Remote", throwable))
            }
        })

        cont.invokeOnCancellation {
            // no-op
        }
    }

    suspend fun connectIfNeeded(): AppResult<Unit> {
        return if (isConnected()) AppResult.Success(Unit) else connect()
    }

    fun disconnect() {
        appRemote?.let { SpotifyAppRemote.disconnect(it) }
        appRemote = null
    }

    fun playTrack(spotifyTrackUri: String): AppResult<Unit> =
        runCatching {
            requireNotNull(appRemote) { "SpotifyAppRemote no conectado" }
                .playerApi.play(spotifyTrackUri)
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error("No se pudo reproducir", it) }

    fun pause(): AppResult<Unit> =
        runCatching {
            requireNotNull(appRemote) { "SpotifyAppRemote no conectado" }
                .playerApi.pause()
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error("No se pudo pausar", it) }

    fun resume(): AppResult<Unit> =
        runCatching {
            requireNotNull(appRemote) { "SpotifyAppRemote no conectado" }
                .playerApi.resume()
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error("No se pudo reanudar", it) }

    fun playerStateFlow(): Flow<PlayerState> = callbackFlow {
        val remote = appRemote
        if (remote == null) {
            close(IllegalStateException("SpotifyAppRemote no conectado"))
            return@callbackFlow
        }

        val sub = remote.playerApi.subscribeToPlayerState()
        sub.setEventCallback { state -> trySend(state).isSuccess }
        sub.setErrorCallback { err -> close(err) }

        awaitClose { sub.cancel() }
    }
}
