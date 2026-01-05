package com.bicheator.harammusic.data.remote.spotify.auth
import android.content.Context
import android.net.Uri
import com.bicheator.harammusic.core.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.openid.appauth.*
import kotlin.coroutines.resume

class SpotifyAuthManager(
    private val context: Context,
    private val clientId: String,
    private val redirectUri: Uri
) {
    private val authService = AuthorizationService(context)

    // Spotify endpoints (OAuth)
    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse("https://accounts.spotify.com/authorize"),
        Uri.parse("https://accounts.spotify.com/api/token")
    )

    fun buildAuthRequest(scopes: List<String>): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        )
            .setScopes(scopes)
            // PKCE se genera automático en AppAuth
            .build()
    }

    suspend fun exchangeCode(tokenRequest: TokenRequest): AppResult<TokenResponse> =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                authService.performTokenRequest(tokenRequest) { response, ex ->
                    when {
                        response != null -> cont.resume(AppResult.Success(response))
                        else -> cont.resume(AppResult.Error("Token exchange falló", ex))
                    }
                }
            }
        }

    suspend fun refreshToken(refreshToken: String): AppResult<TokenResponse> =
        withContext(Dispatchers.IO) {
            val req = TokenRequest.Builder(serviceConfig, clientId)
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(refreshToken)
                .setRedirectUri(redirectUri)
                .build()

            suspendCancellableCoroutine { cont ->
                authService.performTokenRequest(req) { response, ex ->
                    when {
                        response != null -> cont.resume(AppResult.Success(response))
                        else -> cont.resume(AppResult.Error("Refresh token falló", ex))
                    }
                }
            }
        }

    fun dispose() = authService.dispose()
}