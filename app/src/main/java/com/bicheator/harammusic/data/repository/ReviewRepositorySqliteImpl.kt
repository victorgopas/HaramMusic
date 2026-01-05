package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.ContentType
import com.bicheator.harammusic.domain.model.Review
import com.bicheator.harammusic.domain.repository.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReviewRepositorySqliteImpl(
    private val dbProvider: () -> SQLiteDatabase
) : ReviewRepository {
    override suspend fun rateContent(review: Review): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val db = dbProvider()

            // UPSERT por (user_id, content_id, content_type)
            db.execSQL(
                """
                INSERT INTO reviews (id, user_id, content_id, content_type, rating, text, created_at, updated_at, status, original_text)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'VISIBLE', NULL)
                ON CONFLICT(user_id, content_id, content_type)
                DO UPDATE SET
                    rating=excluded.rating,
                    text=excluded.text,
                    updated_at=excluded.updated_at,
                    status='VISIBLE';
                """.trimIndent(),
                arrayOf(
                    review.id,
                    review.userId,
                    review.contentId,
                    review.contentType.name,
                    review.rating,
                    review.text,
                    review.createdAt,
                    now
                )
            )

            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error("No se pudo guardar la valoración", t)
        }
    }

    override suspend fun getReviews(contentId: String): AppResult<List<Review>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val cursor = db.rawQuery(
                """
                SELECT id, user_id, content_id, content_type, rating, text, created_at
                FROM reviews
                WHERE content_id = ? AND status != 'DELETED'
                ORDER BY created_at DESC
                """.trimIndent(),
                arrayOf(contentId)
            )

            val out = buildList {
                cursor.use {
                    while (it.moveToNext()) {
                        add(
                            Review(
                                id = it.getString(0),
                                userId = it.getString(1),
                                contentId = it.getString(2),
                                contentType = ContentType.valueOf(it.getString(3)),
                                rating = it.getInt(4),
                                text = it.getString(5),
                                createdAt = it.getLong(6)
                            )
                        )
                    }
                }
            }
            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron cargar reseñas.", t)
        }
    }
}
