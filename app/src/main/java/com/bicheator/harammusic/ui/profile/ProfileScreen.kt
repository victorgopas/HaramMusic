package com.bicheator.harammusic.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.ui.main.LocalAppContainer

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onDisconnectSpotify: () -> Unit
) {
    val container = LocalAppContainer.current
    val user by container.sessionManager.currentUser.collectAsState()

    val spotifyConnected = container.spotifyTokenStore.accessToken() != null &&
            !container.spotifyTokenStore.isExpired()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Perfil", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Text("Usuario: ${user?.username ?: "-"}")
        Text("Rol: ${user?.role?.name ?: "-"}")

        Spacer(Modifier.height(16.dp))
        Text("Spotify: " + if (spotifyConnected) "Conectado" else "No conectado")

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDisconnectSpotify, enabled = spotifyConnected) {
            Text("Desconectar Spotify")
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogout) { Text("Cerrar sesión") }
    }
}
