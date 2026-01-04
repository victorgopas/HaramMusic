package com.bicheator.harammusic.domain.usecase


import com.bicheator.harammusic.domain.model.Review
import com.bicheator.harammusic.domain.repository.ReviewRepository

class RateContentUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(review: Review) =
        reviewRepository.rateContent(review)
}
