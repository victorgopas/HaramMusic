package com.bicheator.harammusic.data.repository

import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.core.session.SessionManager
import com.bicheator.harammusic.core.util.IdGenerator
import com.bicheator.harammusic.domain.model.Role
import com.bicheator.harammusic.domain.model.User
import com.bicheator.harammusic.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class AuthRepositoryImpl(
    private val sessionManager: SessionManager
) : AuthRepository {

    // “Base de datos” en memoria para pruebas
    private val users = mutableMapOf<String, Pair<String, User>>()
    // username -> (password, user)

    init {
        // Admin de ejemplo
        val admin = User(
            id = IdGenerator.uuid(),
            username = "admin",
            email = "admin@haram.com",
            displayName = "Administrador",
            role = Role.ADMIN,
            isBlocked = false
        )
        users["admin"] = "admin123" to admin
    }

    override suspend fun login(username: String, password: String): AppResult<User> {
        delay(250) // simula red/DB

        val record = users[username]
            ?: return AppResult.Error("Usuario o contraseña incorrectos")

        val (storedPass, user) = record
        if (storedPass != password) return AppResult.Error("Usuario o contraseña incorrectos")
        if (user.isBlocked) return AppResult.Error("Usuario bloqueado")

        sessionManager.setUser(user)
        return AppResult.Success(user)
    }

    override suspend fun register(username: String, email: String, password: String): AppResult<Unit> {
        delay(250)

        if (users.containsKey(username)) return AppResult.Error("Ese usuario ya existe")

        val user = User(
            id = IdGenerator.uuid(),
            username = username,
            email = email,
            displayName = username,
            role = Role.USER,
            isBlocked = false
        )
        users[username] = password to user
        return AppResult.Success(Unit)
    }

    override suspend fun logout(): AppResult<Unit> {
        sessionManager.clear()
        return AppResult.Success(Unit)
    }
}
