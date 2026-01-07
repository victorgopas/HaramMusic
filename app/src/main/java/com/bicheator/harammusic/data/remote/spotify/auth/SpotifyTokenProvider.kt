package com.bicheator.harammusic.data.remote.spotify.auth

import com.bicheator.harammusic.core.result.AppResult
import kotlinx.coroutines.runBlocking

class SpotifyTokenProvider(
    private val tokenStore: SpotifyTokenStore,
    private val authManager: SpotifyAuthManager
) {
    /**
     * Devuelve access token válido o Error si no hay sesión Spotify.
     */
    fun getValidAccessTokenBlocking(): AppResult<String> = runBlocking {
        val token = tokenStore.accessToken()
        if (token != null && !tokenStore.isExpired()) {
            return@runBlocking AppResult.Success(token)
        }

        val refresh = tokenStore.refreshToken()
            ?: return@runBlocking AppResult.Error("No hay token de Spotify. Conecta con Spotify.")

        when (val refreshed = authManager.refreshToken(refresh)) {
            is AppResult.Success -> {
                val newAccess = refreshed.data.accessToken
                    ?: return@runBlocking AppResult.Error("Spotify no devolvió access token")

                val newRefresh = refreshed.data.refreshToken ?: refresh
                val expiresAt = refreshed.data.accessTokenExpirationTime
                    ?: (System.currentTimeMillis() + 3600_000) // fallback 1h

                tokenStore.save(
                    accessToken = newAccess,
                    refreshToken = newRefresh,
                    expiresAtMs = expiresAt
                )

                AppResult.Success(newAccess)
            }

            is AppResult.Error -> refreshed
        }
    }
}

