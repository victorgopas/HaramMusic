package com.bicheator.harammusic.domain.repository


import com.bicheator.harammusic.core.result.AppResult

import com.bicheator.harammusic.domain.model.Review

interface ReviewRepository {
    suspend fun rateContent(review: Review): AppResult<Unit>
    suspend fun getReviews(contentId: String): AppResult<List<Review>>
}
