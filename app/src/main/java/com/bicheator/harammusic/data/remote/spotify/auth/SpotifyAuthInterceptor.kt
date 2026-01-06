package com.bicheator.harammusic.data.remote.spotify.auth

import com.bicheator.harammusic.core.result.AppResult
import okhttp3.Interceptor
import okhttp3.Response

class SpotifyAuthInterceptor(
    private val tokenProvider: SpotifyTokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()

        val tokenRes = tokenProvider.getValidAccessTokenBlocking()
        if (tokenRes is AppResult.Error) {
            // sin token: lanzamos la request sin auth (Spotify devolverá 401)
            return chain.proceed(req)
        }

        val token = (tokenRes as AppResult.Success).data
        val authed = req.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authed)
    }
}
