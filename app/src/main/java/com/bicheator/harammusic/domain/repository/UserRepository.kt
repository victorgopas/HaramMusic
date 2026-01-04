package com.bicheator.harammusic.domain.repository

import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.User

interface UserRepository {
    suspend fun getProfile(userId: String): AppResult<User>
    suspend fun updateProfile(user: User): AppResult<User>

    // Admin
    suspend fun blockUser(adminId: String, targetUserId: String): AppResult<Unit>
}
