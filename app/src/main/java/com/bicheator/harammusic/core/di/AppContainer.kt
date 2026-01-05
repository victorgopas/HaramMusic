package com.bicheator.harammusic.core.di

import android.content.Context
import com.bicheator.harammusic.core.session.SessionManager
import com.bicheator.harammusic.data.local.db.HaramMusicDbHelper
import com.bicheator.harammusic.data.repository.AuthRepositoryImpl
import com.bicheator.harammusic.data.repository.MusicRepositoryFakeImpl
import com.bicheator.harammusic.data.repository.PlaylistRepositorySqliteImpl
import com.bicheator.harammusic.data.repository.ReviewRepositorySqliteImpl
import com.bicheator.harammusic.domain.usecase.CreatePlaylistUseCase
import com.bicheator.harammusic.domain.usecase.LoginUseCase
import com.bicheator.harammusic.domain.usecase.RegisterUseCase
import com.bicheator.harammusic.domain.usecase.SearchUseCase

class AppContainer(context: Context) {

    // Core
    val sessionManager = SessionManager()

    private val dbHelper = HaramMusicDbHelper(context)
    private val dbProvider = { dbHelper.writableDatabase }

    private val authRepository = AuthRepositoryImpl(sessionManager)
    private val musicRepository = MusicRepositoryFakeImpl()

    val reviewRepository = ReviewRepositorySqliteImpl(dbProvider)
    val playlistRepository = PlaylistRepositorySqliteImpl(dbProvider)

    // UseCases
    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    val searchUseCase = SearchUseCase(musicRepository)

    val createPlaylistUseCase = CreatePlaylistUseCase(playlistRepository)
}
