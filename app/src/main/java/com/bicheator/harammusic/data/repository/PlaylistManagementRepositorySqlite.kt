package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.core.database.sqlite.transaction

data class PlaylistRow(
    val id: String,
    val name: String,
    val type: String, // ALL_SONGS | FAVORITES | CUSTOM
    val songCount: Int
)

data class PlaylistSongRow(
    val songId: String,
    val title: String,
    val artist: String?
)

class PlaylistManagementRepositorySqlite(
    private val dbProvider: () -> SQLiteDatabase
) {

    fun ensureSystemPlaylists(userId: String) {
        val db = dbProvider()
        val now = System.currentTimeMillis()

        db.transaction() {
            try {
                execSQL(
                    """
            INSERT OR IGNORE INTO playlists (id, owner_user_id, name, type, created_at, updated_at)
            VALUES (?, ?, 'Todas las canciones', 'ALL_SONGS', ?, ?)
            """.trimIndent(),
                    arrayOf(UUID.randomUUID().toString(), userId, now, now)
                )

                execSQL(
                    """
            INSERT OR IGNORE INTO playlists (id, owner_user_id, name, type, created_at, updated_at)
            VALUES (?, ?, 'Favoritas', 'FAVORITES', ?, ?)
            """.trimIndent(),
                    arrayOf(UUID.randomUUID().toString(), userId, now, now)
                )

                execSQL(
                    """
            UPDATE playlists
            SET type = 'ALL_SONGS', updated_at = ?
            WHERE owner_user_id = ? AND name = 'Todas las canciones'
            """.trimIndent(),
                    arrayOf(now, userId)
                )

                execSQL(
                    """
            UPDATE playlists
            SET type = 'FAVORITES', updated_at = ?
            WHERE owner_user_id = ? AND name = 'Favoritas'
            """.trimIndent(),
                    arrayOf(now, userId)
                )

            } finally {
            }
        }
    }

    fun getPlaylists(userId: String): AppResult<List<PlaylistRow>> = runCatching {
        val db = dbProvider()
        val c = db.rawQuery(
            """
            SELECT p.id, p.name, p.type,
            CASE 
                WHEN p.type = 'ALL_SONGS' THEN (
                  SELECT COUNT(*) FROM songs s WHERE s.is_playable = 1 AND s.file_path IS NOT NULL
                )
                ELSE (
                  SELECT COUNT(*) FROM playlist_songs ps WHERE ps.playlist_id = p.id
                )
              END as song_count
            FROM playlists p
            WHERE p.owner_user_id = ?
            ORDER BY 
              CASE p.type
                WHEN 'ALL_SONGS' THEN 0
                WHEN 'FAVORITES' THEN 1
                ELSE 2
              END,
              p.name COLLATE NOCASE ASC
            """.trimIndent(),
            arrayOf(userId)
        )

        val out = buildList {
            c.use {
                while (it.moveToNext()) {
                    add(
                        PlaylistRow(
                            id = it.getString(0),
                            name = it.getString(1),
                            type = it.getString(2),
                            songCount = it.getInt(3)
                        )
                    )
                }
            }
        }
        AppResult.Success(out)
    }.getOrElse { AppResult.Error("No se pudieron cargar playlists", it) }

    fun createCustomPlaylist(userId: String, name: String): AppResult<Unit> = runCatching {
        val db = dbProvider()
        val now = System.currentTimeMillis()
        db.execSQL(
            """
            INSERT INTO playlists (id, owner_user_id, name, type, created_at, updated_at)
            VALUES (?, ?, ?, 'CUSTOM', ?, ?)
            """.trimIndent(),
            arrayOf(UUID.randomUUID().toString(), userId, name.trim(), now, now)
        )
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error("No se pudo crear la playlist (¿nombre duplicado?)", it) }

    fun renamePlaylist(playlistId: String, newName: String): AppResult<Unit> = runCatching {
        val db = dbProvider()

        val type = getPlaylistType(db, playlistId)
        if (type == "ALL_SONGS" || type == "FAVORITES") {
            return@runCatching AppResult.Error("No se puede renombrar una playlist del sistema", null)
        }

        db.execSQL(
            "UPDATE playlists SET name = ?, updated_at = ? WHERE id = ?",
            arrayOf(newName.trim(), System.currentTimeMillis(), playlistId)
        )
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error("No se pudo renombrar la playlist", it) }

    fun deletePlaylist(playlistId: String): AppResult<Unit> = runCatching {
        val db = dbProvider()
        val type = getPlaylistType(db, playlistId)
        if (type == "ALL_SONGS" || type == "FAVORITES") {
            return@runCatching AppResult.Error("No se puede borrar una playlist del sistema", null)
        }
        db.execSQL("DELETE FROM playlists WHERE id = ?", arrayOf(playlistId))
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error("No se pudo borrar la playlist", it) }

    fun getPlaylistSongs(playlistId: String): AppResult<List<PlaylistSongRow>> = runCatching {
        val db = dbProvider()
        val c = db.rawQuery(
            """
            SELECT s.id, s.title, a.name
            FROM playlist_songs ps
            INNER JOIN songs s ON s.id = ps.song_id
            LEFT JOIN artists a ON a.id = s.artist_id
            WHERE ps.playlist_id = ?
            ORDER BY ps.position ASC, s.title COLLATE NOCASE ASC
            """.trimIndent(),
            arrayOf(playlistId)
        )

        val out = buildList {
            c.use {
                while (it.moveToNext()) {
                    add(PlaylistSongRow(it.getString(0), it.getString(1), it.getString(2)))
                }
            }
        }
        AppResult.Success(out)
    }.getOrElse { AppResult.Error("No se pudieron cargar canciones de la playlist", it) }

    fun addSongToPlaylist(playlistId: String, songId: String): AppResult<Unit> = runCatching {
        val db = dbProvider()

        val exists = db.rawQuery(
            "SELECT 1 FROM playlist_songs WHERE playlist_id = ? AND song_id = ? LIMIT 1",
            arrayOf(playlistId, songId)
        ).use { it.moveToFirst() }

        if (exists) {
            return@runCatching AppResult.Error("Esa canción ya está en la playlist", null)
        }

        val nextPos = db.rawQuery(
            "SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlist_id = ?",
            arrayOf(playlistId)
        ).use { cur -> cur.moveToFirst(); cur.getInt(0) }

        db.execSQL(
            "INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES (?, ?, ?)",
            arrayOf(playlistId, songId, nextPos)
        )
        db.execSQL("UPDATE playlists SET updated_at = ? WHERE id = ?", arrayOf(System.currentTimeMillis(), playlistId))

        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error("No se pudo añadir la canción", it) }

    fun removeSongFromPlaylist(playlistId: String, songId: String): AppResult<Unit> = runCatching {
        val db = dbProvider()
        db.execSQL("DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?", arrayOf(playlistId, songId))
        db.execSQL("UPDATE playlists SET updated_at = ? WHERE id = ?", arrayOf(System.currentTimeMillis(), playlistId))
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error("No se pudo quitar la canción", it) }

    private fun getPlaylistType(db: SQLiteDatabase, playlistId: String): String {
        val c = db.rawQuery("SELECT type FROM playlists WHERE id = ?", arrayOf(playlistId))
        return c.use { if (it.moveToFirst()) it.getString(0) else "CUSTOM" }
    }

    suspend fun getPlaylistType(playlistId: String): String = withContext(Dispatchers.IO) {
        val db = dbProvider()
        getPlaylistType(db, playlistId)
    }

    suspend fun getPlaylistName(playlistId: String): String = withContext(Dispatchers.IO) {
        val db = dbProvider()
        db.rawQuery("SELECT name FROM playlists WHERE id = ?", arrayOf(playlistId)).use { c ->
            if (c.moveToFirst()) c.getString(0) else "Playlist"
        }
    }
}