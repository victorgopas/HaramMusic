package com.bicheator.harammusic.core.di

import android.content.Context
import com.bicheator.harammusic.core.session.SessionManager
import com.bicheator.harammusic.core.storage.LibraryFolderStore
import com.bicheator.harammusic.data.library.CoverStorage
import com.bicheator.harammusic.data.library.LibraryImportRepository
import com.bicheator.harammusic.data.local.db.HaramMusicDbHelper
import com.bicheator.harammusic.data.player.PlayerEngine
import com.bicheator.harammusic.data.repository.AdminRepositorySqliteImpl
import com.bicheator.harammusic.data.repository.AuthRepositorySqliteImpl
import com.bicheator.harammusic.data.repository.LibraryRepositorySqliteImpl
import com.bicheator.harammusic.data.repository.PlaybackQueueRepositorySqlite
import com.bicheator.harammusic.data.repository.PlaylistManagementRepositorySqlite
import com.bicheator.harammusic.data.repository.ReviewRepositorySqliteImpl

class AppContainer(context: Context) {

    val sessionManager = SessionManager()

    val contentResolver = context.contentResolver

    val libraryFolderStore = LibraryFolderStore(context)

    private val dbHelper = HaramMusicDbHelper(context)
    private val dbProvider = { dbHelper.writableDatabase }
    val libraryRepository = LibraryRepositorySqliteImpl(dbProvider)

    val libraryImportRepository = LibraryImportRepository(
        context = context,
        //contentResolver = contentResolver,
        dbProvider = dbProvider
    )
    val playlistManagementRepository = PlaylistManagementRepositorySqlite(dbProvider)
    val playbackQueueRepository = PlaybackQueueRepositorySqlite(dbProvider)
    val playerEngine = PlayerEngine(context)
    val authRepository = AuthRepositorySqliteImpl(sessionManager, dbProvider)
    val reviewRepository = ReviewRepositorySqliteImpl(dbProvider)
    val adminRepository = AdminRepositorySqliteImpl(
        dbProvider = dbProvider,
        coverStorage = CoverStorage(context)
    )
}