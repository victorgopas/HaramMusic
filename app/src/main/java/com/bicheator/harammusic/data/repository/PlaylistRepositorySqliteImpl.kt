package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.repository.PlaylistRepository
import com.bicheator.harammusic.core.util.IdGenerator
import com.bicheator.harammusic.domain.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepositorySqliteImpl(
    private val dbProvider: () -> SQLiteDatabase
) : PlaylistRepository {

    override suspend fun createPlaylist(
        ownerUserId: String,
        name: String,
        songIds: List<String>
    ): AppResult<Playlist> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val now = System.currentTimeMillis()
            val playlistId = IdGenerator.uuid()

            db.beginTransaction()
            try {
                db.execSQL(
                    """
                    INSERT INTO playlists (id, owner_user_id, name, is_public, created_at, updated_at)
                    VALUES (?, ?, ?, 0, ?, ?)
                    """.trimIndent(),
                    arrayOf(playlistId, ownerUserId, name, now, now)
                )

                songIds.forEachIndexed { index, songId ->
                    db.execSQL(
                        """
                        INSERT INTO playlist_songs (playlist_id, song_id, position)
                        VALUES (?, ?, ?)
                        """.trimIndent(),
                        arrayOf(playlistId, songId, index)
                    )
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }

            AppResult.Success(
                Playlist(
                    id = playlistId,
                    ownerUserId = ownerUserId,
                    name = name,
                    isPublic = false,
                    songIds = songIds
                )
            )
        } catch (t: Throwable) {
            AppResult.Error("No se pudo crear la playlist (¿nombre duplicado?)", t)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist): AppResult<Playlist> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val now = System.currentTimeMillis()

            db.beginTransaction()
            try {
                db.execSQL(
                    """
                    UPDATE playlists
                    SET name = ?, is_public = ?, updated_at = ?
                    WHERE id = ?
                    """.trimIndent(),
                    arrayOf(playlist.name, if (playlist.isPublic) 1 else 0, now, playlist.id)
                )

                // Re-sincroniza canciones (simple y robusto)
                db.execSQL("DELETE FROM playlist_songs WHERE playlist_id = ?", arrayOf(playlist.id))
                playlist.songIds.forEachIndexed { index, songId ->
                    db.execSQL(
                        "INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES (?, ?, ?)",
                        arrayOf(playlist.id, songId, index)
                    )
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }

            AppResult.Success(playlist)
        } catch (t: Throwable) {
            AppResult.Error("No se pudo actualizar la playlist", t)
        }
    }

    override suspend fun getMyPlaylists(userId: String): AppResult<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val cursor = db.rawQuery(
                """
                SELECT p.id, p.owner_user_id, p.name, p.is_public
                FROM playlists p
                WHERE p.owner_user_id = ?
                ORDER BY p.updated_at DESC
                """.trimIndent(),
                arrayOf(userId)
            )

            val playlists = mutableListOf<Playlist>()
            cursor.use {
                while (it.moveToNext()) {
                    val playlistId = it.getString(0)
                    playlists.add(
                        Playlist(
                            id = playlistId,
                            ownerUserId = it.getString(1),
                            name = it.getString(2),
                            isPublic = it.getInt(3) == 1,
                            songIds = getSongIds(db, playlistId)
                        )
                    )
                }
            }

            AppResult.Success(playlists)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron cargar tus playlists", t)
        }
    }

    override suspend fun getPublicPlaylists(query: String): AppResult<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val like = "%$query%"
            val cursor = db.rawQuery(
                """
                SELECT p.id, p.owner_user_id, p.name, p.is_public
                FROM playlists p
                WHERE p.is_public = 1 AND p.name LIKE ?
                ORDER BY p.updated_at DESC
                """.trimIndent(),
                arrayOf(like)
            )

            val playlists = mutableListOf<Playlist>()
            cursor.use {
                while (it.moveToNext()) {
                    val playlistId = it.getString(0)
                    playlists.add(
                        Playlist(
                            id = playlistId,
                            ownerUserId = it.getString(1),
                            name = it.getString(2),
                            isPublic = it.getInt(3) == 1,
                            songIds = getSongIds(db, playlistId)
                        )
                    )
                }
            }
            AppResult.Success(playlists)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron cargar playlists públicas", t)
        }
    }

    private fun getSongIds(db: SQLiteDatabase, playlistId: String): List<String> {
        val c = db.rawQuery(
            "SELECT song_id FROM playlist_songs WHERE playlist_id = ? ORDER BY position ASC",
            arrayOf(playlistId)
        )
        return buildList {
            c.use { while (it.moveToNext()) add(it.getString(0)) }
        }
    }
}
