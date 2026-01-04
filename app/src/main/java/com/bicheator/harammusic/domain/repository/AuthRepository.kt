package com.bicheator.harammusic.domain.repository

import com.bicheator.harammusic.core.result.AppResult
import com .bicheator.harammusic.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): AppResult<User>
    suspend fun register(username: String, email: String, password: String): AppResult<Unit>
    suspend fun logout(): AppResult<Unit>
}
