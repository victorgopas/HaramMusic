package com.bicheator.harammusic.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.bicheator.harammusic.ui.explore.ExploreScreen
import com.bicheator.harammusic.ui.home.HomeScreen
import com.bicheator.harammusic.ui.navigate.Routes
import com.bicheator.harammusic.ui.player.MiniPlayerBar
import com.bicheator.harammusic.ui.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldScreen(
    onGoAdmin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val container = LocalAppContainer.current
    val user by container.sessionManager.currentUser.collectAsState()
    val isAdmin = user?.role?.name == "ADMIN"

    val tabNav = rememberNavController()
    val currentRoute = tabNav.currentBackStackEntryAsState().value?.destination?.route

    val playerState by container.playerViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentRoute) {
                            Routes.HOME -> "Inicio"
                            Routes.EXPLORE -> "Explorar"
                            Routes.PROFILE -> "Perfil"
                            else -> "Haram Music"
                        }
                    )
                },
                actions = {
                    if (isAdmin == true) {
                        TextButton(onClick = onGoAdmin) { Text("Admin") }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // ✅ MiniPlayer encima del bottom nav
                MiniPlayerBar(
                    state = playerState,
                    onToggle = { container.playerViewModel.togglePlayPause() }
                )

                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            tabNav.navigate(Routes.HOME) {
                                launchSingleTop = true
                                popUpTo(Routes.HOME) { inclusive = false }
                            }
                        },
                        label = { Text("Inicio") },
                        icon = {}
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.EXPLORE,
                        onClick = { tabNav.navigate(Routes.EXPLORE) { launchSingleTop = true } },
                        label = { Text("Explorar") },
                        icon = {}
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.PROFILE,
                        onClick = { tabNav.navigate(Routes.PROFILE) { launchSingleTop = true } },
                        label = { Text("Perfil") },
                        icon = {}
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = tabNav,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onCreatePlaylist = {
                            // luego: navegar a crear playlist
                        }
                    )
                }

                composable(Routes.EXPLORE) {
                    ExploreScreen(
                        onGoAdmin = onGoAdmin,
                        onOpenSongDetail = {
                            // siguiente paso: detalle canción
                        }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onLogout = {
                            container.sessionManager.clear()
                            onGoLogin()
                        },
                        onDisconnectSpotify = {
                            container.spotifyTokenStore.clear()
                        }
                    )
                }
            }
        }
    }
}
