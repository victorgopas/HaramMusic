package com.bicheator.harammusic.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import com.bicheator.harammusic.ui.navigate.Routes

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = { onNavigate(Routes.HOME) },
            label = { Text("Inicio") },
            icon = { Icon(Icons.Default.Home, null) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.SEARCH,
            onClick = { onNavigate(Routes.SEARCH) },
            label = { Text("Buscar") },
            icon = { Icon(Icons.Default.Search, null) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.LIBRARY,
            onClick = { onNavigate(Routes.LIBRARY) },
            label = { Text("Biblioteca") },
            icon = { Icon(Icons.Default.List, null) }
        )
    }
}