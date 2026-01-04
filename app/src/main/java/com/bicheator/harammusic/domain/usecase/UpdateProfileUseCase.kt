package com.bicheator.harammusic.domain.usecase


import com.bicheator.harammusic.domain.model.User
import com.bicheator.harammusic.domain.repository.UserRepository

class UpdateProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User) = userRepository.updateProfile(user)
}
