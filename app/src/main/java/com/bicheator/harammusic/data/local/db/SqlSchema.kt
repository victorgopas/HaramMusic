package com.bicheator.harammusic.data.local.db

object SqlSchema {
    // ---- USERS ----
    const val CREATE_USERS = """
        CREATE TABLE IF NOT EXISTS users (
          id TEXT PRIMARY KEY,
          username TEXT NOT NULL UNIQUE,
          password_hash TEXT NOT NULL,
          email TEXT,
          display_name TEXT NOT NULL,
          role TEXT NOT NULL DEFAULT 'USER',
          is_blocked INTEGER NOT NULL DEFAULT 0,
          created_at INTEGER NOT NULL
        );
    """

    // ---- ARTISTS ----
    const val CREATE_ARTISTS = """
        CREATE TABLE IF NOT EXISTS artists (
          id TEXT PRIMARY KEY,
          name TEXT NOT NULL UNIQUE,
          bio TEXT,
          created_at INTEGER NOT NULL,
          content_deleted INTEGER NOT NULL DEFAULT 0
        );
    """

    // ---- ALBUMS ----
    const val CREATE_ALBUMS = """
        CREATE TABLE IF NOT EXISTS albums (
          id TEXT PRIMARY KEY,
          artist_id TEXT NOT NULL,
          title TEXT NOT NULL,
          description TEXT,
          cover_path TEXT,
          total_duration_ms INTEGER NOT NULL DEFAULT 0,
          created_at INTEGER NOT NULL,
          content_deleted INTEGER NOT NULL DEFAULT 0,
          UNIQUE(artist_id, title),
          FOREIGN KEY(artist_id) REFERENCES artists(id) ON DELETE CASCADE
        );
    """

    // ---- SONGS ----
    const val CREATE_SONGS = """
        CREATE TABLE IF NOT EXISTS songs (
          id TEXT PRIMARY KEY,
          artist_id TEXT,
          album_id TEXT,
          title TEXT NOT NULL,
          file_name TEXT,
          file_path TEXT,
          duration_ms INTEGER NOT NULL DEFAULT 0,
          track_number INTEGER,
          cover_path TEXT,
          is_playable INTEGER NOT NULL DEFAULT 1,
          created_at INTEGER NOT NULL,
          content_deleted INTEGER NOT NULL DEFAULT 0,
          UNIQUE(file_path),
          FOREIGN KEY(artist_id) REFERENCES artists(id) ON DELETE SET NULL,
          FOREIGN KEY(album_id) REFERENCES albums(id) ON DELETE SET NULL
        );
    """

    // ---- GENRES ----
    const val CREATE_GENRES = """
        CREATE TABLE IF NOT EXISTS genres (
          id TEXT PRIMARY KEY,
          name TEXT NOT NULL UNIQUE
        );
    """

    const val CREATE_SONG_GENRES = """
        CREATE TABLE IF NOT EXISTS song_genres (
          song_id TEXT NOT NULL,
          genre_id TEXT NOT NULL,
          PRIMARY KEY(song_id, genre_id),
          FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE,
          FOREIGN KEY(genre_id) REFERENCES genres(id) ON DELETE CASCADE
        );
    """

    // ---- PLAYLISTS ----
    // type: CUSTOM | ALL_SONGS | FAVORITES
    const val CREATE_PLAYLISTS = """
        CREATE TABLE IF NOT EXISTS playlists (
          id TEXT PRIMARY KEY,
          owner_user_id TEXT NOT NULL,
          name TEXT NOT NULL,
          type TEXT NOT NULL DEFAULT 'CUSTOM',
          created_at INTEGER NOT NULL,
          updated_at INTEGER NOT NULL,
          UNIQUE(owner_user_id, name),
          FOREIGN KEY(owner_user_id) REFERENCES users(id) ON DELETE CASCADE
        );
    """

    const val CREATE_PLAYLIST_SONGS = """
        CREATE TABLE IF NOT EXISTS playlist_songs (
          playlist_id TEXT NOT NULL,
          song_id TEXT NOT NULL,
          position INTEGER NOT NULL DEFAULT 0,
          PRIMARY KEY (playlist_id, song_id),
          FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
          FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
        );
    """

    // ---- REVIEWS ----
    // rating REAL: medias estrellas (0.5..5.0)
    const val CREATE_REVIEWS = """
        CREATE TABLE IF NOT EXISTS reviews (
          id TEXT PRIMARY KEY,
          user_id TEXT NOT NULL,
          song_id TEXT NOT NULL,
          rating REAL NOT NULL CHECK (rating >= 0.5 AND rating <= 5.0),
          text TEXT,
          created_at INTEGER NOT NULL,
          updated_at INTEGER NOT NULL,
          UNIQUE(user_id, song_id),
          FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
          FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
        );
    """

    // ---- INDEXES ----
    const val IDX_SONGS_PLAYABLE = "CREATE INDEX IF NOT EXISTS idx_songs_playable ON songs(is_playable);"
    const val IDX_SONGS_TITLE = "CREATE INDEX IF NOT EXISTS idx_songs_title ON songs(title);"
    const val IDX_ARTISTS_NAME = "CREATE INDEX IF NOT EXISTS idx_artists_name ON artists(name);"
    const val IDX_ALBUMS_TITLE = "CREATE INDEX IF NOT EXISTS idx_albums_title ON albums(title);"
    const val IDX_PLAYLIST_OWNER = "CREATE INDEX IF NOT EXISTS idx_playlists_owner ON playlists(owner_user_id);"
    const val IDX_PLAYLIST_SONGS = "CREATE INDEX IF NOT EXISTS idx_playlist_songs_playlist ON playlist_songs(playlist_id, position);"
    const val IDX_REVIEWS_SONG = "CREATE INDEX IF NOT EXISTS idx_reviews_song ON reviews(song_id);"
}