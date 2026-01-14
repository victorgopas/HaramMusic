package com.bicheator.harammusic.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bicheator.harammusic.ui.navigate.Routes

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.SONGS,
            onClick = { onNavigate(Routes.SONGS) },
            label = { Text("Canciones") },
            icon = { Icon(Icons.Filled.LibraryMusic, null) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PLAYER,
            onClick = { onNavigate(Routes.PLAYER) },
            label = { Text("Reproductor") },
            icon = { Icon(Icons.Filled.PlayArrow, null) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.CONTENT,
            onClick = { onNavigate(Routes.CONTENT) },
            label = { Text("Contenido") },
            icon = { Icon(Icons.Filled.Info, null) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = { onNavigate(Routes.PROFILE) },
            label = { Text("Perfil") },
            icon = { Icon(Icons.Filled.Person, null) }
        )
    }
}