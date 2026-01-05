package com.bicheator.harammusic.data.local.db

object SqlSchema {
    const val CREATE_USERS = """
        CREATE TABLE IF NOT EXISTS users (
          id TEXT PRIMARY KEY,
          username TEXT NOT NULL UNIQUE,
          email TEXT,
          display_name TEXT,
          role TEXT NOT NULL,
          is_blocked INTEGER NOT NULL DEFAULT 0
        );
    """

    const val CREATE_REVIEWS = """
        CREATE TABLE IF NOT EXISTS reviews (
          id TEXT PRIMARY KEY,
          user_id TEXT NOT NULL,
          content_id TEXT NOT NULL,
          content_type TEXT NOT NULL,
          rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
          text TEXT,
          created_at INTEGER NOT NULL,
          updated_at INTEGER NOT NULL,
          status TEXT NOT NULL DEFAULT 'VISIBLE',
          original_text TEXT,
          FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
          UNIQUE(user_id, content_id, content_type)
        );
    """

    const val CREATE_PLAYLISTS = """
        CREATE TABLE IF NOT EXISTS playlists (
          id TEXT PRIMARY KEY,
          owner_user_id TEXT NOT NULL,
          name TEXT NOT NULL,
          is_public INTEGER NOT NULL DEFAULT 0,
          created_at INTEGER NOT NULL,
          updated_at INTEGER NOT NULL,
          FOREIGN KEY(owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
          UNIQUE(owner_user_id, name)
        );
    """

    const val CREATE_PLAYLIST_SONGS = """
        CREATE TABLE IF NOT EXISTS playlist_songs (
          playlist_id TEXT NOT NULL,
          song_id TEXT NOT NULL,
          position INTEGER NOT NULL DEFAULT 0,
          PRIMARY KEY (playlist_id, song_id),
          FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE
        );
    """

    const val CREATE_PLAYBACK_LOGS = """
        CREATE TABLE IF NOT EXISTS playback_logs (
          id TEXT PRIMARY KEY,
          user_id TEXT NOT NULL,
          song_id TEXT NOT NULL,
          action TEXT NOT NULL,
          position_ms INTEGER,
          created_at INTEGER NOT NULL,
          meta_json TEXT,
          FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
        );
    """

    // Índices
    const val IDX_REVIEWS_CONTENT = "CREATE INDEX IF NOT EXISTS idx_reviews_content ON reviews(content_id, content_type);"
    const val IDX_REVIEWS_USER = "CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);"
    const val IDX_PLAYLISTS_OWNER = "CREATE INDEX IF NOT EXISTS idx_playlists_owner ON playlists(owner_user_id);"
    const val IDX_PLAYLISTS_PUBLIC = "CREATE INDEX IF NOT EXISTS idx_playlists_public ON playlists(is_public);"
    const val IDX_PLAYLIST_SONGS_PLAYLIST = "CREATE INDEX IF NOT EXISTS idx_playlist_songs_playlist ON playlist_songs(playlist_id);"
    const val IDX_PLAYBACK_USER = "CREATE INDEX IF NOT EXISTS idx_playback_user ON playback_logs(user_id);"
    const val IDX_PLAYBACK_SONG = "CREATE INDEX IF NOT EXISTS idx_playback_song ON playback_logs(song_id);"
}