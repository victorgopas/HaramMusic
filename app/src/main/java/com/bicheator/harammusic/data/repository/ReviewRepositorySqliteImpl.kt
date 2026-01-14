package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.Review
import com.bicheator.harammusic.domain.repository.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReviewRepositorySqliteImpl(
    private val dbProvider: () -> SQLiteDatabase
) : ReviewRepository {

    override suspend fun rateSong(review: Review): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val now = System.currentTimeMillis()

            val updatedRows = db.compileStatement(
                """
                UPDATE reviews
                SET rating = ?, text = ?, updated_at = ?
                WHERE user_id = ? AND song_id = ?
                """.trimIndent()
            ).apply {
                bindDouble(1, review.rating)
                if (review.text != null) bindString(2, review.text) else bindNull(2)
                bindLong(3, now)
                bindString(4, review.userId)
                bindString(5, review.songId)
            }.executeUpdateDelete()

            if (updatedRows == 0) {
                db.execSQL(
                    """
                    INSERT INTO reviews (id, user_id, song_id, rating, text, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    arrayOf(
                        review.id,
                        review.userId,
                        review.songId,
                        review.rating,
                        review.text,
                        now,
                        now
                    )
                )
            }

            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error("No se pudo guardar la reseña", t)
        }
    }

    override suspend fun getReviewsForSong(songId: String): AppResult<List<Review>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val cursor = db.rawQuery(
                """
                SELECT r.id, r.user_id, r.song_id, r.rating, r.text, r.created_at, u.display_name
                FROM reviews r
                LEFT JOIN users u ON u.id = r.user_id
                WHERE r.song_id = ?
                ORDER BY r.created_at DESC
                """.trimIndent(),
                arrayOf(songId)
            )

            val out = buildList {
                cursor.use {
                    while (it.moveToNext()) {
                        add(
                            Review(
                                id = it.getString(0),
                                userId = it.getString(1),
                                songId = it.getString(2),
                                rating = it.getDouble(3),
                                text = it.getString(4),
                                createdAt = it.getLong(5),
                                userDisplayName = it.getString(6)
                            )
                        )
                    }
                }
            }

            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron cargar las reseñas", t)
        }
    }
}