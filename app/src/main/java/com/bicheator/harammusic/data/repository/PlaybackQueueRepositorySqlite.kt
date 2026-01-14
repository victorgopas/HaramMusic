package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.PlaybackItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaybackQueueRepositorySqlite(
    private val dbProvider: () -> SQLiteDatabase
) {
    suspend fun getQueueForPlaylist(
        playlistId: String,
        playlistType: String
    ): AppResult<List<PlaybackItem>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()

            val baseSelect = """
                SELECT 
                  s.id,
                  s.title,
                  a.name,
                  al.title,
                  s.file_path,
                  COALESCE(s.cover_path, al.cover_path)
                FROM songs s
                LEFT JOIN artists a ON a.id = s.artist_id
                LEFT JOIN albums al ON al.id = s.album_id
            """.trimIndent()

            val sql: String
            val args: Array<String>

            if (playlistType == "ALL_SONGS") {
                sql = """
                    $baseSelect
                    WHERE s.is_playable = 1 AND s.file_path IS NOT NULL
                    ORDER BY s.title COLLATE NOCASE ASC
                """.trimIndent()
                args = emptyArray()
            } else {
                sql = """
                    $baseSelect
                    INNER JOIN playlist_songs ps ON ps.song_id = s.id
                    WHERE ps.playlist_id = ?
                      AND s.is_playable = 1 AND s.file_path IS NOT NULL
                    ORDER BY ps.position ASC, s.title COLLATE NOCASE ASC
                """.trimIndent()
                args = arrayOf(playlistId)
            }

            val c = db.rawQuery(sql, args)
            val out = buildList {
                c.use {
                    while (it.moveToNext()) {
                        val uriString = it.getString(4) ?: continue
                        add(
                            PlaybackItem(
                                songId = it.getString(0),
                                title = it.getString(1),
                                artist = it.getString(2),
                                album = it.getString(3),
                                uriString = uriString,
                                coverPath = it.getString(5)
                            )
                        )
                    }
                }
            }
            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudo crear la cola de reproducción", t)
        }
    }
}