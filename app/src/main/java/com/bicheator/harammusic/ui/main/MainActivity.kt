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

        val start = if (container.sessionManager.isLoggedIn()) Routes.EXPLORE else Routes.LOGIN

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController, startDestination = start)
            }
        }
    }
}
