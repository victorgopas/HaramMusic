package com.bicheator.harammusic.domain.usecase

import com.bicheator.harammusic.domain.repository.MusicRepository

class GetExploreUseCase(
    private val repo: MusicRepository
) {
    suspend operator fun invoke() = repo.getExplore()
}
