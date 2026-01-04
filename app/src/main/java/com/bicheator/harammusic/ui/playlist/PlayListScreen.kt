package com.bicheator.harammusic.ui.playlist


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaylistsScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Mis Playlists", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Aquí irá la lista + botón crear playlist")
    }
}
