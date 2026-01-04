package com.bicheator.harammusic.ui.navigate


import androidx.compose.runtime.Composable
import androidx.navigate.NavHostController
import androidx.navigate.compose.NavHost
import androidx.navigate.compose.composable
import com.bicheator.harammusic.ui.admin.AdminScreen
import com.bicheator.harammusic.ui.auth.LoginScreen
import com.bicheator.harammusic.ui.auth.RegisterScreen
import com.bicheator.harammusic.ui.playlist.PlaylistsScreen
import com.bicheator.harammusic.ui.profile.ProfileScreen
import com.bicheator.harammusic.ui.search.SearchScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.SEARCH) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onGoLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onGoPlaylists = { navController.navigate(Routes.PLAYLISTS) },
                onGoProfile = { navController.navigate(Routes.PROFILE) },
                onGoAdmin = { navController.navigate(Routes.ADMIN) }
            )
        }

        composable(Routes.PLAYLISTS) { PlaylistsScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }
        composable(Routes.ADMIN) { AdminScreen() }
    }
}
