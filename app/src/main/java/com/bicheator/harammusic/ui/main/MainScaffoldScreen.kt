package com.bicheator.harammusic.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bicheator.harammusic.ui.content.ContentScreen
import com.bicheator.harammusic.ui.navigate.Routes
import com.bicheator.harammusic.ui.player.PlayerScreen
import com.bicheator.harammusic.ui.playlist.PlaylistDetailScreen
import com.bicheator.harammusic.ui.playlist.PlaylistsScreen
import com.bicheator.harammusic.ui.profile.ProfileScreen
import com.bicheator.harammusic.ui.songs.SongsScreen


@Composable
fun MainScaffoldScreen(
    onGoAdmin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val nav = rememberNavController()
    val currentRoute by nav.currentBackStackEntryAsState()

    val container = LocalAppContainer.current
    val importVm = remember(container) { com.bicheator.harammusic.data.library.LibraryImportViewModel(container) }

    LaunchedEffect(Unit) {
        importVm.autoImportIfPossible()
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { route ->
                    val current = nav.currentBackStackEntry?.destination?.route
                    if (current == route) return@BottomNavBar

                    nav.navigate(route) {
                        launchSingleTop = true

                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = false
                        }

                        restoreState = false
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)){
            NavHost(
                navController = nav,
                startDestination = Routes.SONGS
            ) {
                composable(Routes.SONGS) {
                    SongsScreen(
                        onOpenPlaylists = { nav.navigate(Routes.PLAYLISTS) },
                        onOpenSongDetail = { id -> nav.navigate("song_detail/$id") }
                    )
                }
                composable(Routes.PLAYER) { PlayerScreen() }
                composable(Routes.CONTENT) { ContentScreen(
                    onOpenArtist = { id -> nav.navigate("artist_detail/$id") },
                    onOpenAlbum = { id -> nav.navigate("album_detail/$id") },
                    onOpenSong = { id -> nav.navigate("song_detail/$id") }
                ) }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = onGoLogin,
                        onGoAdmin = onGoAdmin
                    )
                }
                composable(Routes.PLAYLISTS) { PlaylistsScreen(
                    onBack = { nav.popBackStack()},
                    onOpenPlaylist = { id ->
                    nav.navigate("playlist_detail/$id")
                }) }

                composable(
                    route = Routes.PLAYLIST_DETAIL,
                    arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                ) { backStack ->
                    val playlistId = backStack.arguments?.getString("playlistId")!!
                    PlaylistDetailScreen(
                        playlistId = playlistId,
                        onBack = { nav.popBackStack() }
                    )
                }

                composable(
                    route = Routes.ARTIST_DETAIL,
                    arguments = listOf(navArgument("artistId") { type = NavType.StringType })
                ) { backStack ->
                    val artistId = backStack.arguments?.getString("artistId")!!
                    com.bicheator.harammusic.ui.content.detail.ArtistDetailScreen(
                        artistId = artistId,
                        onBack = { nav.popBackStack() },
                        onOpenAlbum = { id -> nav.navigate("album_detail/$id") },
                        onOpenSong = { id -> nav.navigate("song_detail/$id") }
                    )
                }

                composable(
                    route = Routes.ALBUM_DETAIL,
                    arguments = listOf(navArgument("albumId") { type = NavType.StringType })
                ) { backStack ->
                    val albumId = backStack.arguments?.getString("albumId")!!
                    com.bicheator.harammusic.ui.content.detail.AlbumDetailScreen(
                        albumId = albumId,
                        onBack = { nav.popBackStack() },
                        onOpenSong = { id -> nav.navigate("song_detail/$id") }
                    )
                }

                composable(
                    route = Routes.SONG_DETAIL,
                    arguments = listOf(navArgument("songId") { type = NavType.StringType })
                ) { backStack ->
                    val songId = backStack.arguments?.getString("songId")!!
                    com.bicheator.harammusic.ui.content.detail.SongDetailScreen(
                        songId = songId,
                        onBack = { nav.popBackStack() }
                    )
                }


            }
        }
    }
}