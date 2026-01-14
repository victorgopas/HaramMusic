package com.bicheator.harammusic.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import com.bicheator.harammusic.core.di.AppContainer
import com.bicheator.harammusic.ui.navigate.AppNavGraph
import com.bicheator.harammusic.ui.navigate.Routes


class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val start = if (container.sessionManager.isLoggedIn()) Routes.MAIN else Routes.LOGIN

        setContent {
            val navController = rememberNavController()
            val playerVm = androidx.compose.runtime.remember(container) {
                com.bicheator.harammusic.ui.player.PlayerViewModel(container)
            }

            CompositionLocalProvider(
                LocalAppContainer provides container,
                com.bicheator.harammusic.ui.player.LocalPlayerViewModel provides playerVm
            ) {
                AppNavGraph(navController = navController, startDestination = start)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        container.playerEngine.release()
    }
}
