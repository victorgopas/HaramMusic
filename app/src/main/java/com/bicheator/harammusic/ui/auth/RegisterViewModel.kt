package com.bicheator.harammusic.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = RegisterUiState(error = "Rellena todos los campos")
            return
        }
        viewModelScope.launch {
            _state.value = RegisterUiState(loading = true)
            when (val res = registerUseCase(username.trim(), email.trim(), password)) {
                is AppResult.Success -> _state.value = RegisterUiState(success = true)
                is AppResult.Error -> _state.value = RegisterUiState(error = res.message)
            }
        }
    }

    fun consumeSuccess() {
        _state.value = _state.value.copy(success = false)
    }
}
