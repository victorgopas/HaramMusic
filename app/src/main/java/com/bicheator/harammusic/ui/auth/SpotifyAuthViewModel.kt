package com.bicheator.harammusic.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyAuthManager
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyTokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

data class SpotifyAuthUiState(
    val connected: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

class SpotifyAuthViewModel(
    private val authManager: SpotifyAuthManager,
    private val tokenStore: SpotifyTokenStore
) : ViewModel() {

    private val _state = MutableStateFlow(
        SpotifyAuthUiState(
            connected = tokenStore.accessToken() != null && !tokenStore.isExpired()
        )
    )
    val state: StateFlow<SpotifyAuthUiState> = _state

    fun createAuthIntent(): Intent {
        val scopes = listOf(
            // para perfil/usuario (opcional, pero útil)
            "user-read-email",
            "user-read-private",
            // para App Remote “tipo Spotify”
            "streaming",
            "app-remote-control"
        )
        val req = authManager.buildAuthRequest(scopes)
        return authManager.getAuthorizationIntent(req)
    }

    fun handleAuthResult(data: Intent?) {
        val resp = AuthorizationResponse.fromIntent(data ?: return)
        val ex = AuthorizationException.fromIntent(data)

        if (ex != null) {
            _state.value = _state.value.copy(loading = false, error = "Login Spotify cancelado o falló")
            return
        }

        val tokenRequest = resp.createTokenExchangeRequest()

        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            when (val tokenRes = authManager.exchangeCode(tokenRequest)) {
                is AppResult.Success -> {
                    val tokenResponse = tokenRes.data
                    val access = tokenResponse.accessToken
                    val refresh = tokenResponse.refreshToken
                    val expiresAtMs = tokenResponse.accessTokenExpirationTime ?: (System.currentTimeMillis() + 3600_000)

                    if (access == null) {
                        _state.value = SpotifyAuthUiState(connected = false, loading = false, error = "Spotify no devolvió access token")
                        return@launch
                    }

                    tokenStore.save(
                        accessToken = access,
                        refreshToken = refresh,
                        expiresAtMs = expiresAtMs
                    )

                    _state.value = SpotifyAuthUiState(connected = true, loading = false, error = null)
                }

                is AppResult.Error -> {
                    _state.value = SpotifyAuthUiState(connected = false, loading = false, error = tokenRes.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
