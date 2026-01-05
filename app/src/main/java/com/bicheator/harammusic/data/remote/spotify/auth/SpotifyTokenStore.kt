package com.bicheator.harammusic.data.remote.spotify.auth

import android.content.Context

class SpotifyTokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("spotify_tokens", Context.MODE_PRIVATE)

    fun save(accessToken: String, refreshToken: String?, expiresAtMs: Long) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("expires_at", expiresAtMs)
            .apply()
    }

    fun accessToken(): String? = prefs.getString("access_token", null)
    fun refreshToken(): String? = prefs.getString("refresh_token", null)
    fun expiresAtMs(): Long = prefs.getLong("expires_at", 0L)

    fun isExpired(leewayMs: Long = 30_000): Boolean =
        System.currentTimeMillis() + leewayMs >= expiresAtMs()

    fun clear() = prefs.edit().clear().apply()
}