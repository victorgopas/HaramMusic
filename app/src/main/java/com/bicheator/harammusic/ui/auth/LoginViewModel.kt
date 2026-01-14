package com.bicheator.harammusic.ui.auth

import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.User
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


class LoginViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        val u = username.trim()
        val p = password

        if (u.isEmpty() || p.isEmpty()) {
            _state.update { it.copy(message = "Usuario y contraseña son obligatorios") }
            return
        }

        scope.launch {
            _state.update { it.copy(loading = true, message = null) }
            val res = withContext(Dispatchers.IO) {
                container.authRepository.login(u, p)
            }
            when (res) {
                is AppResult.Success<*> -> {
                    val user = res.data as? User
                    if (user == null) {
                        _state.update { it.copy(loading = false, message = "Error interno: respuesta de login inválida") }
                        return@launch
                    }
                    container.sessionManager.setCurrentUser(user)
                    _state.update { it.copy(loading = false, message = null) }
                    onSuccess()
                }
                is AppResult.Error -> _state.update { it.copy(loading = false, message = res.message) }
            }
        }
    }

    fun clear() = scope.cancel()
}