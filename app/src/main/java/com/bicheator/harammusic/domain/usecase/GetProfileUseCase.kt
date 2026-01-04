package com.bicheator.harammusic.domain.usecase

import com.bicheator.harammusic.domain.repository.UserRepository

class GetProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String) = userRepository.getProfile(userId)
}
