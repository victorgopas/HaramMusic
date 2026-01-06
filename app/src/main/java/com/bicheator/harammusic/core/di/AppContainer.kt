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


import android.net.Uri
import com.bicheator.harammusic.data.remote.spotify.api.SpotifyApiService
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyAuthInterceptor
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyAuthManager
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyTokenProvider
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyTokenStore
import com.bicheator.harammusic.data.repository.MusicRepositorySpotifyImpl
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
class AppContainer(context: Context) {

    // Core
    val sessionManager = SessionManager()

    private val dbHelper = HaramMusicDbHelper(context)
    private val dbProvider = { dbHelper.writableDatabase }

    private val authRepository = AuthRepositoryImpl(sessionManager)
    //private val musicRepository = MusicRepositoryFakeImpl()

    val reviewRepository = ReviewRepositorySqliteImpl(dbProvider)
    val playlistRepository = PlaylistRepositorySqliteImpl(dbProvider)

    // UseCases
    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    //val searchUseCase = SearchUseCase(musicRepository)

    val createPlaylistUseCase = CreatePlaylistUseCase(playlistRepository)
    // TODO
    // ⚠️ pon tu clientId real + redirectUri real aquí
    private val spotifyClientId = "TU_CLIENT_ID"
    private val spotifyRedirectUri = Uri.parse("com.bicheator.harammusic://callback")

    val spotifyTokenStore = SpotifyTokenStore(context)
    val spotifyAuthManager = SpotifyAuthManager(context, spotifyClientId, spotifyRedirectUri)

    private val tokenProvider = SpotifyTokenProvider(spotifyTokenStore, spotifyAuthManager)

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(SpotifyAuthInterceptor(tokenProvider))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
        .build()

    private val spotifyApi = retrofit.create(SpotifyApiService::class.java)

    // ✅ Repository real
    private val musicRepository = MusicRepositorySpotifyImpl(spotifyApi)

    // ✅ UseCase sigue igual
    val searchUseCase = SearchUseCase(musicRepository)


}
