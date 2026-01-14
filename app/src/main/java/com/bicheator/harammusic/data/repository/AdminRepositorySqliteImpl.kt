package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.library.CoverStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.database.sqlite.transaction

class AdminRepositorySqliteImpl(
    private val dbProvider: () -> SQLiteDatabase,
    private val coverStorage: CoverStorage
) {

    suspend fun updateArtistBio(artistId: String, bio: String?): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val db = dbProvider()
                db.execSQL("UPDATE artists SET bio = ? WHERE id = ?", arrayOf(bio, artistId))
                AppResult.Success(Unit)
            } catch (t: Throwable) {
                AppResult.Error("No se pudo actualizar la bio", t)
            }
        }

    suspend fun updateAlbumDescription(albumId: String, description: String?): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val db = dbProvider()
                db.execSQL("UPDATE albums SET description = ? WHERE id = ?", arrayOf(description, albumId))
                AppResult.Success(Unit)
            } catch (t: Throwable) {
                AppResult.Error("No se pudo actualizar la descripción del álbum", t)
            }
        }

    suspend fun setAlbumCoverFromBytes(albumId: String, bytes: ByteArray): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val db = dbProvider()
                val path = coverStorage.saveCoverBytes(bytes)
                db.execSQL("UPDATE albums SET cover_path = ? WHERE id = ?", arrayOf(path, albumId))
                AppResult.Success(Unit)
            } catch (t: Throwable) {
                AppResult.Error("No se pudo guardar la portada del álbum", t)
            }
        }

    suspend fun setSongCoverFromBytes(songId: String, bytes: ByteArray): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val db = dbProvider()
                val path = coverStorage.saveCoverBytes(bytes)
                db.execSQL("UPDATE songs SET cover_path = ? WHERE id = ?", arrayOf(path, songId))
                AppResult.Success(Unit)
            } catch (t: Throwable) {
                AppResult.Error("No se pudo guardar la portada de la canción", t)
            }
        }

    suspend fun setSongGenres(songId: String, genreNames: List<String>): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val cleaned = genreNames.map { it.trim() }.filter { it.isNotBlank() }.distinct()
                val db = dbProvider()

                db.transaction() {
                    try {
                        execSQL("DELETE FROM song_genres WHERE song_id = ?", arrayOf(songId))

                        cleaned.forEach { name ->
                            val genreId = rawQuery(
                                "SELECT id FROM genres WHERE name = ? LIMIT 1",
                                arrayOf(name)
                            )
                                .use { c -> if (c.moveToFirst()) c.getString(0) else null }

                            val finalGenreId = genreId ?: run {
                                val newId = java.util.UUID.randomUUID().toString()
                                execSQL(
                                    "INSERT INTO genres (id, name) VALUES (?, ?)",
                                    arrayOf(newId, name)
                                )
                                newId
                            }

                            execSQL(
                                "INSERT INTO song_genres (song_id, genre_id) VALUES (?, ?)",
                                arrayOf(songId, finalGenreId)
                            )
                        }

                    } finally {
                    }
                }

                AppResult.Success(Unit)
            } catch (t: Throwable) {
                AppResult.Error("No se pudieron actualizar los géneros", t)
            }
        }

    suspend fun updateReview(reviewId: String, rating: Double, text: String?): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val db = dbProvider()
                val now = System.currentTimeMillis()
                db.execSQL(
                    "UPDATE reviews SET rating = ?, text = ?, updated_at = ? WHERE id = ?",
                    arrayOf(rating, text, now, reviewId)
                )
                AppResult.Success(Unit)
            } catch (t: Throwable) {
                AppResult.Error("No se pudo actualizar la reseña", t)
            }
        }

    suspend fun deleteReview(reviewId: String): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val db = dbProvider()
                db.execSQL("DELETE FROM reviews WHERE id = ?", arrayOf(reviewId))
                AppResult.Success(Unit)
            } catch (t: Throwable) {
                AppResult.Error("No se pudo borrar la reseña", t)
            }
        }

    suspend fun deleteSong(songId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            db.transaction() {
                try {
                    execSQL("DELETE FROM song_genres WHERE song_id = ?", arrayOf(songId))
                    execSQL(
                        "DELETE FROM reviews WHERE song_id = ?",
                        arrayOf(songId)
                    )

                    execSQL("UPDATE songs SET content_deleted = 1 WHERE id = ?", arrayOf(songId))

                } finally {
                }
            }
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error("No se pudo borrar la ficha de la canción", t)
        }
    }

    suspend fun deleteAlbum(albumId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            db.transaction() {
                try {
                    val songIds = rawQuery(
                        "SELECT id FROM songs WHERE album_id = ?",
                        arrayOf(albumId)
                    ).use { c ->
                        buildList { while (c.moveToNext()) add(c.getString(0)) }
                    }

                    execSQL("UPDATE albums SET content_deleted = 1 WHERE id = ?", arrayOf(albumId))

                    execSQL(
                        "UPDATE songs SET content_deleted = 1 WHERE album_id = ?",
                        arrayOf(albumId)
                    )

                    songIds.forEach { sid ->
                        execSQL("DELETE FROM reviews WHERE song_id = ?", arrayOf(sid))
                        execSQL("DELETE FROM song_genres WHERE song_id = ?", arrayOf(sid))
                    }

                } finally {
                }
            }

            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error("No se pudo borrar la ficha del álbum", t)
        }
    }

    suspend fun deleteArtist(artistId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            db.transaction() {
                try {
                    val songIds = rawQuery(
                        "SELECT id FROM songs WHERE artist_id = ?",
                        arrayOf(artistId)
                    ).use { c ->
                        buildList { while (c.moveToNext()) add(c.getString(0)) }
                    }

                    execSQL(
                        "UPDATE artists SET content_deleted = 1 WHERE id = ?",
                        arrayOf(artistId)
                    )
                    execSQL(
                        "UPDATE albums SET content_deleted = 1 WHERE artist_id = ?",
                        arrayOf(artistId)
                    )
                    execSQL(
                        "UPDATE songs SET content_deleted = 1 WHERE artist_id = ?",
                        arrayOf(artistId)
                    )

                    songIds.forEach { sid ->
                        execSQL("DELETE FROM reviews WHERE song_id = ?", arrayOf(sid))
                        execSQL("DELETE FROM song_genres WHERE song_id = ?", arrayOf(sid))
                    }

                } finally {
                }
            }

            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error("No se pudo borrar la ficha del artista", t)
        }
    }
}