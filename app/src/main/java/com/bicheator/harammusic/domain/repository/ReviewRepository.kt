package com.bicheator.harammusic.domain.repository


import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.Review

interface ReviewRepository {
    suspend fun rateSong(review: Review): AppResult<Unit>
    suspend fun getReviewsForSong(songId: String): AppResult<List<Review>>
}
