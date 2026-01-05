package com.bicheator.harammusic.ui.auth


import com.bicheator.harammusic.domain.model.User

data class LoginUiState(
    val loading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)
