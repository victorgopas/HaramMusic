package com.bicheator.harammusic.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onCreatePlaylist: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Inicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Button(onClick = onCreatePlaylist) {
            Text("Nueva playlist")
        }
    }
}