package com.bicheator.harammusic.data.library

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.util.UUID
import androidx.core.database.sqlite.transaction

class LibraryImportRepository(
    private val context: Context,
    private val dbProvider: () -> SQLiteDatabase
) {
    fun importFromTreeUri(treeUri: Uri): ImportResult {
        return try {
            val root = DocumentFile.fromTreeUri(context, treeUri)
                ?: return ImportResult(ok = false, error = "No se pudo abrir la carpeta")

            val audioFiles = collectAudioFiles(root)

            val db = dbProvider()
            var imported = 0

            db.transaction() {
                try {
                    for (file in audioFiles) {
                        val uri = file.uri
                        val meta = extractMetadata(uri)

                        val artistId = meta.artist?.let { upsertArtist(this, it) }
                        val albumId =
                            meta.album?.let { upsertAlbum(this, artistId, it, meta.coverPath) }

                        upsertSong(
                            db = this,
                            uriString = uri.toString(),
                            fileName = file.name ?: "unknown",
                            title = meta.title ?: (file.name ?: "Unknown"),
                            durationMs = meta.durationMs,
                            artistId = artistId,
                            albumId = albumId,
                            coverPath = meta.coverPath,
                            isPlayable = true
                        )

                        imported++
                    }

                    markMissingSongsAsNotPlayable(
                        this,
                        existingUriStrings = audioFiles.map { it.uri.toString() }.toSet()
                    )

                } finally {
                }
            }

            ImportResult(ok = true, importedCount = imported)
        } catch (t: Throwable) {
            ImportResult(ok = false, error = t.message ?: "Error desconocido")
        }
    }

    private fun collectAudioFiles(root: DocumentFile): List<DocumentFile> {
        val out = mutableListOf<DocumentFile>()
        val stack = ArrayDeque<DocumentFile>()
        stack.add(root)

        while (stack.isNotEmpty()) {
            val dir = stack.removeLast()
            dir.listFiles().forEach { f ->
                if (f.isDirectory) stack.add(f)
                else if (f.isFile && isAudio(f)) out.add(f)
            }
        }
        return out
    }

    private fun isAudio(f: DocumentFile): Boolean {
        val type = f.type ?: return false
        return type.startsWith("audio/")
    }

    private data class Meta(
        val title: String?,
        val artist: String?,
        val album: String?,
        val durationMs: Long,
        val coverPath: String?
    )

    private fun extractMetadata(uri: Uri): Meta {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, uri)

        val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLongOrNull() ?: 0L

        val artBytes = mmr.embeddedPicture
        val coverPath = if (artBytes != null) {
            CoverStorage(context).saveCoverBytes(artBytes)
        } else null

        mmr.release()

        return Meta(title, artist, album, durationMs, coverPath)
    }

    private fun upsertArtist(db: SQLiteDatabase, name: String): String {
        val existingId = db.rawQuery(
            "SELECT id FROM artists WHERE name = ? LIMIT 1",
            arrayOf(name)
        ).use { c -> if (c.moveToFirst()) c.getString(0) else null }

        if (existingId != null) {
            db.execSQL("UPDATE artists SET content_deleted = 0 WHERE id = ?", arrayOf(existingId))
            return existingId
        }

        val id = UUID.randomUUID().toString()
        db.execSQL(
            "INSERT INTO artists (id, name, bio, created_at, content_deleted) VALUES (?, ?, NULL, ?, 0)",
            arrayOf(id, name, System.currentTimeMillis())
        )
        return id
    }

    private fun upsertAlbum(db: SQLiteDatabase, artistId: String?, title: String, coverPath: String?): String {
        val existingId = db.rawQuery(
            "SELECT id FROM albums WHERE artist_id IS ? AND title = ? LIMIT 1",
            arrayOf(artistId, title)
        ).use { c -> if (c.moveToFirst()) c.getString(0) else null }

        if (existingId != null) {
            db.execSQL("UPDATE albums SET content_deleted = 0 WHERE id = ?", arrayOf(existingId))
            if (!coverPath.isNullOrBlank()) {
                db.execSQL(
                    "UPDATE albums SET cover_path = COALESCE(cover_path, ?) WHERE id = ?",
                    arrayOf(coverPath, existingId)
                )
            }
            return existingId
        }

        val id = UUID.randomUUID().toString()
        db.execSQL(
            """
        INSERT INTO albums (id, artist_id, title, cover_path, total_duration_ms, created_at, content_deleted)
        VALUES (?, ?, ?, ?, 0, ?, 0)
        """.trimIndent(),
            arrayOf(id, artistId, title, coverPath, System.currentTimeMillis())
        )
        return id
    }

    private fun findExistingSongBySignature(
        db: SQLiteDatabase,
        title: String,
        fileName: String,
        durationMs: Long,
        artistId: String?,
        albumId: String?
    ): String? {
        val c = db.rawQuery(
            """
        SELECT id
        FROM songs
        WHERE title = ?
          AND file_name = ?
          AND duration_ms = ?
          AND ((artist_id = ?) OR (artist_id IS NULL AND ? IS NULL))
          AND ((album_id = ?) OR (album_id IS NULL AND ? IS NULL))
        LIMIT 1
        """.trimIndent(),
            arrayOf(
                title,
                fileName,
                durationMs.toString(),
                artistId, artistId,
                albumId, albumId
            )
        )
        return c.use { if (it.moveToFirst()) it.getString(0) else null }
    }


    private fun upsertSong(
        db: SQLiteDatabase,
        uriString: String,
        fileName: String,
        title: String,
        durationMs: Long,
        artistId: String?,
        albumId: String?,
        coverPath: String?,
        isPlayable: Boolean
    ) {
        val now = System.currentTimeMillis()

        db.rawQuery("SELECT id FROM songs WHERE file_path = ?", arrayOf(uriString)).use {
            if (it.moveToFirst()) {
                val existingId = it.getString(0)
                db.execSQL(
                    """
                    UPDATE songs
                    SET title = ?, file_name = ?, duration_ms = ?, artist_id = ?, album_id = ?,
                        cover_path = COALESCE(cover_path, ?),
                        is_playable = ?, file_path = ?,
                        content_deleted = 0
                    WHERE id = ?
                    """.trimIndent(),
                    arrayOf(
                        title, fileName, durationMs, artistId, albumId,
                        coverPath,
                        if (isPlayable) 1 else 0, uriString,
                        existingId
                    )
                )
                return
            }
        }

        val bySigId = findExistingSongBySignature(db, title, fileName, durationMs, artistId, albumId)
        if (bySigId != null) {
            db.execSQL(
                """
                UPDATE songs
                SET file_path = ?, is_playable = 1,
                    content_deleted = 0,
                    cover_path = COALESCE(?, cover_path),
                    artist_id = COALESCE(?, artist_id),
                    album_id = COALESCE(?, album_id),
                    title = ?, file_name = ?, duration_ms = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf(uriString, coverPath, artistId, albumId, title, fileName, durationMs, bySigId)
            )
            return
        }

        val id = UUID.randomUUID().toString()
        db.execSQL(
            """
        INSERT INTO songs (id, artist_id, album_id, title, file_name, file_path, duration_ms, cover_path, is_playable, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
            arrayOf(
                id, artistId, albumId, title, fileName, uriString, durationMs, coverPath,
                if (isPlayable) 1 else 0, now
            )
        )
    }

    private fun markMissingSongsAsNotPlayable(db: SQLiteDatabase, existingUriStrings: Set<String>) {
        val c = db.rawQuery("SELECT id, file_path FROM songs WHERE file_path IS NOT NULL", emptyArray())
        val toDisable = mutableListOf<String>()
        c.use {
            while (it.moveToNext()) {
                val id = it.getString(0)
                val path = it.getString(1)
                if (!existingUriStrings.contains(path)) {
                    toDisable.add(id)
                }
            }
        }

        toDisable.forEach { id ->
            db.execSQL(
                "UPDATE songs SET is_playable = 0 WHERE id = ?",
                arrayOf(id)
            )
        }
    }
}