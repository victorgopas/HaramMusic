package com.bicheator.harammusic.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HaramMusicDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SqlSchema.CREATE_USERS)

        db.execSQL(SqlSchema.CREATE_ARTISTS)
        db.execSQL(SqlSchema.CREATE_ALBUMS)
        db.execSQL(SqlSchema.CREATE_SONGS)

        db.execSQL(SqlSchema.CREATE_GENRES)
        db.execSQL(SqlSchema.CREATE_SONG_GENRES)

        db.execSQL(SqlSchema.CREATE_PLAYLISTS)
        db.execSQL(SqlSchema.CREATE_PLAYLIST_SONGS)

        db.execSQL(SqlSchema.CREATE_REVIEWS)

        // Índices
        db.execSQL(SqlSchema.IDX_SONGS_PLAYABLE)
        db.execSQL(SqlSchema.IDX_SONGS_TITLE)
        db.execSQL(SqlSchema.IDX_ARTISTS_NAME)
        db.execSQL(SqlSchema.IDX_ALBUMS_TITLE)
        db.execSQL(SqlSchema.IDX_PLAYLIST_OWNER)
        db.execSQL(SqlSchema.IDX_PLAYLIST_SONGS)
        db.execSQL(SqlSchema.IDX_REVIEWS_SONG)
        // Admin por defecto
        db.execSQL(
            """
            INSERT OR IGNORE INTO users (id, username, password_hash, email, display_name, role, is_blocked, created_at)
            VALUES ('admin-id', 'admin', ?, 'admin@haram.com', 'Administrador', 'ADMIN', 0, ?)
            """.trimIndent(),
            arrayOf(
                com.bicheator.harammusic.core.util.PasswordHasher.sha256("admin123"),
                System.currentTimeMillis()
            )
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS reviews")
        db.execSQL("DROP TABLE IF EXISTS playlist_songs")
        db.execSQL("DROP TABLE IF EXISTS playlists")
        db.execSQL("DROP TABLE IF EXISTS song_genres")
        db.execSQL("DROP TABLE IF EXISTS genres")
        db.execSQL("DROP TABLE IF EXISTS songs")
        db.execSQL("DROP TABLE IF EXISTS albums")
        db.execSQL("DROP TABLE IF EXISTS artists")
        db.execSQL("DROP TABLE IF EXISTS users")

        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "harammusic.db"
        private const val DATABASE_VERSION = 6
    }
}