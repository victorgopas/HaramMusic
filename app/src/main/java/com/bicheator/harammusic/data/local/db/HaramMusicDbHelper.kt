package com.bicheator.harammusic.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HaramMusicDbHelper(context : Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db:SQLiteDatabase) {
        db.execSQL(SqlSchema.CREATE_USERS)
        db.execSQL(SqlSchema.CREATE_REVIEWS)
        db.execSQL(SqlSchema.CREATE_PLAYLISTS)
        db.execSQL(SqlSchema.CREATE_PLAYLIST_SONGS)
        db.execSQL(SqlSchema.CREATE_PLAYBACK_LOGS)

        db.execSQL(SqlSchema.IDX_REVIEWS_CONTENT)
        db.execSQL(SqlSchema.IDX_REVIEWS_USER)
        db.execSQL(SqlSchema.IDX_PLAYLISTS_OWNER)
        db.execSQL(SqlSchema.IDX_PLAYLISTS_PUBLIC)
        db.execSQL(SqlSchema.IDX_PLAYLIST_SONGS_PLAYLIST)
        db.execSQL(SqlSchema.IDX_PLAYBACK_USER)
        db.execSQL(SqlSchema.IDX_PLAYBACK_SONG)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS playback_logs")
        db.execSQL("DROP TABLE IF EXISTS playlist_songs")
        db.execSQL("DROP TABLE IF EXISTS playlists")
        db.execSQL("DROP TABLE IF EXISTS reviews")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    companion object{
        private const val DATABASE_NAME = "harammusic.db"
        private const val DATABASE_VERSION = 1
    }
}