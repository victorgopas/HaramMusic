package com.bicheator.harammusic.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = LoginUiState(error = "Rellena usuario y contraseña")
            return
        }
        viewModelScope.launch {
            _state.value = LoginUiState(loading = true)
            when (val res = loginUseCase(username.trim(), password)) {
                is AppResult.Success -> _state.value = LoginUiState(user = res.data)
                is AppResult.Error -> _state.value = LoginUiState(error = res.message)
            }
        }
    }

    fun consumeUser() {
        _state.value = _state.value.copy(user = null)
    }
}
