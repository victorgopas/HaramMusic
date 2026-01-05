package com.bicheator.harammusic.core.di


import com.bicheator.harammusic.core.session.SessionManager
import com.bicheator.harammusic.data.repository.AuthRepositoryImpl
import com.bicheator.harammusic.data.repository.MusicRepositoryFakeImpl
import com.bicheator.harammusic.domain.usecase.LoginUseCase
import com.bicheator.harammusic.domain.usecase.RegisterUseCase
import com.bicheator.harammusic.domain.usecase.SearchUseCase

class AppContainer {

    // Core
    val sessionManager = SessionManager()

    // Repositories (fake por ahora)
    private val authRepository = AuthRepositoryImpl(sessionManager)
    private val musicRepository = MusicRepositoryFakeImpl()

    // UseCases
    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    val searchUseCase = SearchUseCase(musicRepository)
}
