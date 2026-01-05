package com.bicheator.harammusic.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.bicheator.harammusic.data.remote.spotify.player.PlayerViewModel
import com.bicheator.harammusic.ui.home.HomeScreen
import com.bicheator.harammusic.ui.navigate.Routes
import com.bicheator.harammusic.ui.player.MiniPlayerBar
import com.bicheator.harammusic.ui.search.SearchScreen
import com.bicheator.harammusic.ui.playlist.PlaylistsScreen

@Composable
fun MainScaffoldScreen(
    onGoAdmin: () -> Unit
) {
    val nav = rememberNavController()

    val playerVm = remember { PlayerViewModel() }
    val playerState by playerVm.state.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                // MiniPlayer encima de la bottom bar
                MiniPlayerBar(
                    state = playerState,
                    onPlayPause = { playerVm.togglePlayPause() },
                    onOpenNowPlaying = { /* TODO: navegar a pantalla completa si la creas */ }
                )
                BottomNavBar(
                    currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route,
                    onNavigate = { route -> nav.navigate(route) { launchSingleTop = true } }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onCreatePlaylist = { nav.navigate(Routes.PLAYLIST_CREATE) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onGoPlaylists = { nav.navigate(Routes.LIBRARY) },
                    onGoProfile = { /* puedes navegar fuera si quieres */ },
                    onGoAdmin = onGoAdmin
                )
            }
            composable(Routes.LIBRARY) { PlaylistsScreen() }

            composable(Routes.PLAYLIST_CREATE) {
                //CreatePlaylistScreen(
                //    onBack = { nav.popBackStack() }
               // )
            }
        }
    }
}

