package com.bicheator.harammusic.ui.navigate

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bicheator.harammusic.ui.admin.AdminScreen
import com.bicheator.harammusic.ui.auth.LoginScreen
import com.bicheator.harammusic.ui.auth.RegisterScreen
import com.bicheator.harammusic.ui.main.MainScaffoldScreen
import com.bicheator.harammusic.ui.main.LocalAppContainer
import androidx.compose.runtime.collectAsState
import com.bicheator.harammusic.domain.model.Role

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
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
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

        composable(Routes.MAIN) {
            MainScaffoldScreen(
                onGoAdmin = { navController.navigate(Routes.ADMIN) },
                onGoLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.ADMIN) {
            val container = LocalAppContainer.current
            val userState = container.sessionManager.currentUser.collectAsState()
            val user = userState.value
            val isAdmin = user?.role == Role.ADMIN
            if (isAdmin) AdminScreen() else MainScaffoldScreen(onGoAdmin = {}, onGoLogin = {})

        }

    }
}
