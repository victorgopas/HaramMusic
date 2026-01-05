package com.bicheator.harammusic.ui.main

import androidx.compose.runtime.staticCompositionLocalOf
import com.bicheator.harammusic.core.di.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no proporcionado")
}
