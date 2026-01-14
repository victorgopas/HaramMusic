package com.bicheator.harammusic.core.session

import com.bicheator.harammusic.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun clear() {
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null

    fun getUserIdOrNull(): String? = _currentUser.value?.id

    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }
}
