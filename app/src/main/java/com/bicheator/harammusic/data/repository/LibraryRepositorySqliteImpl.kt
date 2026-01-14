package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PlaylistLite(val id: String, val name: String, val type: String)
data class SongRow(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val coverPath: String?,
    val isPlayable: Boolean,
    val isFavorite: Boolean
)

data class ArtistRow(val id: String, val name: String)
data class AlbumRow(val id: String, val title: String, val artistName: String?, val coverPath: String?)
data class SongCardRow(val id: String, val title: String, val artistName: String?, val albumTitle: String?, val coverPath: String?)

class LibraryRepositorySqliteImpl(
    private val dbProvider: () -> SQLiteDatabase
) {
    suspend fun getPlaylistsForUser(userId: String): AppResult<List<PlaylistLite>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()

            val cursor = db.rawQuery(
                """
                SELECT id, name, type
                FROM playlists
                WHERE owner_user_id = ?
                ORDER BY 
                  CASE type
                    WHEN 'ALL_SONGS' THEN 0
                    WHEN 'FAVORITES' THEN 1
                    ELSE 2
                  END,
                  name ASC
                """.trimIndent(),
                arrayOf(userId)
            )

            val out = buildList {
                cursor.use {
                    while (it.moveToNext()) {
                        add(PlaylistLite(it.getString(0), it.getString(1), it.getString(2)))
                    }
                }
            }
            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron cargar playlists", t)
        }
    }

    suspend fun getPlayableSongs(
        playlistId: String,
        playlistType: String,
        query: String?,
        filterArtist: String?,
        filterAlbum: String?,
        favoritesPlaylistId: String?
    ): AppResult<List<SongRow>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val q = query?.trim().orEmpty()

            val where = mutableListOf<String>()
            val args = mutableListOf<String>()

            where += "s.is_playable = 1 AND s.file_path IS NOT NULL"

            if (!filterArtist.isNullOrBlank()) {
                where += "a.name LIKE ? COLLATE NOCASE"
                args += "%${filterArtist.trim()}%"
            }
            if (!filterAlbum.isNullOrBlank()) {
                where += "al.title LIKE ? COLLATE NOCASE"
                args += "%${filterAlbum.trim()}%"
            }

            if (q.isNotEmpty()) {
                val like = "%$q%"
                where += "(s.title LIKE ? COLLATE NOCASE OR a.name LIKE ? COLLATE NOCASE OR al.title LIKE ? COLLATE NOCASE)"
                args += like; args += like; args += like
            }

            val baseSelect = """
            SELECT 
              s.id, 
              s.title, 
              a.name, 
              al.title, 
              COALESCE(s.cover_path, al.cover_path), 
              s.is_playable,
              CASE WHEN fav.song_id IS NULL THEN 0 ELSE 1 END AS is_fav
            FROM songs s
            LEFT JOIN artists a ON a.id = s.artist_id
            LEFT JOIN albums al ON al.id = s.album_id
            LEFT JOIN playlist_songs fav 
              ON fav.song_id = s.id AND fav.playlist_id = ?
            """.trimIndent()

            val sql: String
            val finalArgs = mutableListOf<String>()

            finalArgs += (favoritesPlaylistId ?: "__NO_FAV__")

            if (playlistType == "ALL_SONGS") {
                sql = """
                    $baseSelect
                    WHERE ${where.joinToString(" AND ")}
                    ORDER BY s.title COLLATE NOCASE ASC
                """.trimIndent()

                finalArgs += args
            } else {
                where += "ps.playlist_id = ?"
                sql = """
                    $baseSelect
                    INNER JOIN playlist_songs ps ON ps.song_id = s.id
                    WHERE ${where.joinToString(" AND ")}
                    ORDER BY ps.position ASC, s.title COLLATE NOCASE ASC
                """.trimIndent()

                finalArgs += args
                finalArgs += playlistId
            }

            val c = db.rawQuery(sql, finalArgs.toTypedArray())
            val out = buildList {
                c.use {
                    while (it.moveToNext()) {
                        add(
                            SongRow(
                                id = it.getString(0),
                                title = it.getString(1),
                                artist = it.getString(2),
                                album = it.getString(3),
                                coverPath = it.getString(4),
                                isPlayable = it.getInt(5) == 1,
                                isFavorite = it.getInt(6) == 1
                            )
                        )
                    }
                }
            }
            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron cargar canciones", t)
        }
    }

    suspend fun searchArtists(query: String): AppResult<List<ArtistRow>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val like = "%${query.trim()}%"
            val c = db.rawQuery(
                """
                SELECT id, name
                FROM artists
                WHERE content_deleted = 0 AND name LIKE ?
                ORDER BY name COLLATE NOCASE ASC
                """.trimIndent(),
                arrayOf(like)
            )
            val out = buildList { c.use { while (it.moveToNext()) add(ArtistRow(it.getString(0), it.getString(1))) } }
            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron buscar artistas", t)
        }
    }

    suspend fun searchAlbums(query: String): AppResult<List<AlbumRow>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val like = "%${query.trim()}%"
            val c = db.rawQuery(
                """
                SELECT al.id, al.title, a.name, al.cover_path
                FROM albums al
                LEFT JOIN artists a ON a.id = al.artist_id
                WHERE al.content_deleted = 0 AND (al.title LIKE ? OR a.name LIKE ?)
                ORDER BY al.title COLLATE NOCASE ASC
                """.trimIndent(),
                arrayOf(like, like)
            )
            val out = buildList {
                c.use {
                    while (it.moveToNext()) {
                        add(AlbumRow(it.getString(0), it.getString(1), it.getString(2), it.getString(3)))
                    }
                }
            }
            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron buscar álbumes", t)
        }
    }

    suspend fun searchSongsForContent(query: String): AppResult<List<SongCardRow>> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()
            val like = "%${query.trim()}%"
            val c = db.rawQuery(
                """
                SELECT s.id, s.title, a.name, al.title, COALESCE(s.cover_path, al.cover_path)
                FROM songs s
                LEFT JOIN artists a ON a.id = s.artist_id
                LEFT JOIN albums al ON al.id = s.album_id
                WHERE s.content_deleted = 0 AND (s.title LIKE ? OR a.name LIKE ? OR al.title LIKE ?)
                ORDER BY s.title COLLATE NOCASE ASC
                """.trimIndent(),
                arrayOf(like, like, like)
            )
            val out = buildList {
                c.use {
                    while (it.moveToNext()) {
                        add(SongCardRow(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4)))
                    }
                }
            }
            AppResult.Success(out)
        } catch (t: Throwable) {
            AppResult.Error("No se pudieron buscar canciones", t)
        }
    }

    data class ArtistDetail(
        val id: String,
        val name: String,
        val bio: String?,
        val albums: List<AlbumRow>,
        val songs: List<SongCardRow>
    )

    data class AlbumDetail(
        val id: String,
        val title: String,
        val artistId: String?,
        val artistName: String?,
        val coverPath: String?,
        val description: String?,
        val totalDurationMs: Long,
        val songs: List<SongCardRow>
    )

    data class SongDetail(
        val id: String,
        val title: String,
        val artistName: String?,
        val albumTitle: String?,
        val coverPath: String?,
        val durationMs: Long,
        val genres: List<String>,
        val avgRating: Double,
        val reviewCount: Int
    )

    suspend fun getArtistDetail(artistId: String): AppResult<ArtistDetail> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()

            val artist = db.rawQuery(
                "SELECT id, name, bio FROM artists WHERE id = ? AND content_deleted = 0",
                arrayOf(artistId)
            ).use { c ->
                if (!c.moveToFirst()) return@withContext AppResult.Error("Artista no encontrado")
                Triple(c.getString(0), c.getString(1), c.getString(2))
            }

            val albums = db.rawQuery(
                """
            SELECT al.id, al.title, a.name, al.cover_path
            FROM albums al
            LEFT JOIN artists a ON a.id = al.artist_id
            WHERE al.artist_id = ? AND al.content_deleted = 0
            ORDER BY al.title COLLATE NOCASE ASC
            """.trimIndent(),
                arrayOf(artistId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(AlbumRow(c.getString(0), c.getString(1), c.getString(2), c.getString(3)))
                    }
                }
            }

            val songs = db.rawQuery(
                """
            SELECT s.id, s.title, a.name, al.title, COALESCE(s.cover_path, al.cover_path)
            FROM songs s
            LEFT JOIN artists a ON a.id = s.artist_id
            LEFT JOIN albums al ON al.id = s.album_id
            WHERE s.artist_id = ? AND s.content_deleted = 0
            ORDER BY s.title COLLATE NOCASE ASC
            """.trimIndent(),
                arrayOf(artistId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(SongCardRow(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)))
                    }
                }
            }

            AppResult.Success(
                ArtistDetail(
                    id = artist.first,
                    name = artist.second,
                    bio = artist.third,
                    albums = albums,
                    songs = songs
                )
            )
        } catch (t: Throwable) {
            AppResult.Error("No se pudo cargar el artista", t)
        }
    }

    suspend fun getAlbumDetail(albumId: String): AppResult<AlbumDetail> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()

            val album = db.rawQuery(
                """
            SELECT 
            al.id,
            al.title,
            al.artist_id,
            a.name,
            al.cover_path,
            al.description,
            COALESCE((SELECT SUM(s2.duration_ms) FROM songs s2 WHERE s2.album_id = al.id), 0) AS total_duration_ms
            FROM albums al
            LEFT JOIN artists a ON a.id = al.artist_id
            WHERE al.id = ?
            """.trimIndent(),
                arrayOf(albumId)
            ).use { c ->
                if (!c.moveToFirst()) return@withContext AppResult.Error("Álbum no encontrado")
                AlbumDetail(
                    id = c.getString(0),
                    title = c.getString(1),
                    artistId = c.getString(2),
                    artistName = c.getString(3),
                    coverPath = c.getString(4),
                    description = c.getString(5),
                    totalDurationMs = c.getLong(6),
                    songs = emptyList()
                )
            }

            val songs = db.rawQuery(
                """
            SELECT s.id, s.title, a.name, al.title, COALESCE(s.cover_path, al.cover_path)
            FROM songs s
            LEFT JOIN artists a ON a.id = s.artist_id
            LEFT JOIN albums al ON al.id = s.album_id
            WHERE s.album_id = ?
            ORDER BY COALESCE(s.track_number, 9999) ASC, s.title COLLATE NOCASE ASC
            """.trimIndent(),
                arrayOf(albumId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(SongCardRow(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)))
                    }
                }
            }

            AppResult.Success(album.copy(songs = songs))
        } catch (t: Throwable) {
            AppResult.Error("No se pudo cargar el álbum", t)
        }
    }

    suspend fun getSongDetail(songId: String): AppResult<SongDetail> = withContext(Dispatchers.IO) {
        try {
            val db = dbProvider()

            val info = db.rawQuery(
                """
            SELECT s.id, s.title, a.name, al.title, COALESCE(s.cover_path, al.cover_path), s.duration_ms, s.content_deleted
            FROM songs s
            LEFT JOIN artists a ON a.id = s.artist_id
            LEFT JOIN albums al ON al.id = s.album_id
            WHERE s.id = ?
            """.trimIndent(),
                arrayOf(songId)
            ).use { c ->
                if (!c.moveToFirst()) return@withContext AppResult.Error("Canción no encontrada")

                val deleted = c.getInt(6) == 1
                if (deleted) return@withContext AppResult.Error("Ficha no disponible")

                arrayOf(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getLong(5).toString()
                )
            }

            val genres = db.rawQuery(
                """
            SELECT g.name
            FROM song_genres sg
            INNER JOIN genres g ON g.id = sg.genre_id
            WHERE sg.song_id = ?
            ORDER BY g.name COLLATE NOCASE ASC
            """.trimIndent(),
                arrayOf(songId)
            ).use { c ->
                buildList { while (c.moveToNext()) add(c.getString(0)) }
            }

            val ratingAgg = db.rawQuery(
                "SELECT AVG(rating), COUNT(*) FROM reviews WHERE song_id = ?",
                arrayOf(songId)
            ).use { c ->
                c.moveToFirst()
                val avg = if (c.isNull(0)) 0.0 else c.getDouble(0)
                val cnt = c.getInt(1)
                avg to cnt
            }

            AppResult.Success(
                SongDetail(
                    id = info[0],
                    title = info[1],
                    artistName = info[2],
                    albumTitle = info[3],
                    coverPath = info[4],
                    durationMs = info[5].toLong(),
                    genres = genres,
                    avgRating = ratingAgg.first,
                    reviewCount = ratingAgg.second
                )
            )
        } catch (t: Throwable) {
            AppResult.Error("No se pudo cargar la canción", t)
        }
    }




}