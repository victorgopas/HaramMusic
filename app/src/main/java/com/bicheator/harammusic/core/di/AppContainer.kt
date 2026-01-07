package com.bicheator.harammusic.core.di

import android.content.Context
import android.net.Uri
import com.bicheator.harammusic.core.session.SessionManager
import com.bicheator.harammusic.data.local.db.HaramMusicDbHelper
import com.bicheator.harammusic.data.remote.spotify.api.SpotifyApiService
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyAuthInterceptor
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyAuthManager
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyTokenProvider
import com.bicheator.harammusic.data.remote.spotify.auth.SpotifyTokenStore
import com.bicheator.harammusic.data.repository.AuthRepositoryImpl
import com.bicheator.harammusic.data.repository.MusicRepositorySpotifyImpl
import com.bicheator.harammusic.data.repository.PlaylistRepositorySqliteImpl
import com.bicheator.harammusic.data.repository.ReviewRepositorySqliteImpl
import com.bicheator.harammusic.domain.usecase.CreatePlaylistUseCase
import com.bicheator.harammusic.domain.usecase.GetExploreUseCase
import com.bicheator.harammusic.domain.usecase.GetMyPlaylistsUseCase
import com.bicheator.harammusic.domain.usecase.LoginUseCase
import com.bicheator.harammusic.domain.usecase.RegisterUseCase
import com.bicheator.harammusic.domain.usecase.SearchUseCase
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.bicheator.harammusic.data.remote.spotify.player.SpotifyPlayerRemoteDataSource
import com.bicheator.harammusic.ui.player.PlayerViewModel

class AppContainer(context: Context) {

    // Usa applicationContext para evitar leaks
    private val appContext = context.applicationContext

    // Core
    val sessionManager = SessionManager()

    // --- SQLite ---
    private val dbHelper = HaramMusicDbHelper(appContext)
    private val dbProvider = { dbHelper.writableDatabase }

    val reviewRepository = ReviewRepositorySqliteImpl(dbProvider)
    val playlistRepository = PlaylistRepositorySqliteImpl(dbProvider)

    // --- Auth app (vuestro login interno) ---
    private val authRepository = AuthRepositoryImpl(sessionManager)

    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)

    // --- Playlists ---
    val createPlaylistUseCase = CreatePlaylistUseCase(playlistRepository)
    val getMyPlaylistsUseCase = GetMyPlaylistsUseCase(playlistRepository)

    // --- Spotify OAuth / Tokens ---
    // ⚠️ Rellena esto con tu client id real de Spotify Dev Dashboard
    private val spotifyClientId = "TU_CLIENT_ID"
    private val spotifyRedirectUri = Uri.parse("com.bicheator.harammusic://callback")

    val spotifyTokenStore = SpotifyTokenStore(appContext)
    val spotifyAuthManager = SpotifyAuthManager(appContext, spotifyClientId, spotifyRedirectUri)

    private val tokenProvider = SpotifyTokenProvider(spotifyTokenStore, spotifyAuthManager)

    // --- Spotify Web API (Retrofit) ---
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(SpotifyAuthInterceptor(tokenProvider))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
        .build()

    private val spotifyApi = retrofit.create(SpotifyApiService::class.java)

    private val musicRepository = MusicRepositorySpotifyImpl(spotifyApi)

    val searchUseCase = SearchUseCase(musicRepository)
    val getExploreUseCase = GetExploreUseCase(musicRepository)
    private val spotifyPlayerRemote = SpotifyPlayerRemoteDataSource(
        context = appContext,
        clientId = spotifyClientId,
        redirectUri = spotifyRedirectUri.toString()
    )

    val playerViewModel = PlayerViewModel(spotifyPlayerRemote)

}
