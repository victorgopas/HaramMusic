package com.bicheator.harammusic.domain.usecase

import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String): AppResult<Unit> {
        return authRepository.register(username, email, password)
    }
}
