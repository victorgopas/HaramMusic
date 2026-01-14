package com.bicheator.harammusic.ui.player

import androidx.compose.runtime.staticCompositionLocalOf

val LocalPlayerViewModel = staticCompositionLocalOf<PlayerViewModel> {
    error("PlayerViewModel not provided")
}