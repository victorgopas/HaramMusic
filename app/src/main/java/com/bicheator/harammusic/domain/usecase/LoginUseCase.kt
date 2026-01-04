package com.bicheator.harammusic.domain.usecase


import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.User
import com.bicheator.harammusic.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): AppResult<User> {
        return authRepository.login(username, password)
    }
}
