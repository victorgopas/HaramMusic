package com.bicheator.harammusic.ui.search

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.ui.auth.SpotifyAuthViewModel
import com.bicheator.harammusic.ui.main.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onGoPlaylists: () -> Unit,
    onGoProfile: () -> Unit,
    onGoAdmin: () -> Unit
) {
    val container = LocalAppContainer.current

    // VM de search (Spotify Web API)
    val vm = remember(container) { SearchViewModel(container.searchUseCase) }
    val state by vm.state.collectAsState()

    // VM de auth Spotify (AppAuth)
    val authVm = remember(container) {
        SpotifyAuthViewModel(container.spotifyAuthManager, container.spotifyTokenStore)
    }
    val authState by authVm.state.collectAsState()

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authVm.handleAuthResult(result.data)
    }

    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Buscar") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, label = { Text("Buscar") }, icon = {})
                NavigationBarItem(selected = false, onClick = onGoPlaylists, label = { Text("Playlists") }, icon = {})
                NavigationBarItem(selected = false, onClick = onGoProfile, label = { Text("Perfil") }, icon = {})
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        ) {
            // ---------- BLOQUE SPOTIFY AUTH ----------
            if (!authState.connected) {
                Text(
                    "Conéctate con Spotify para buscar canciones reales.",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))

                if (authState.error != null) {
                    Text(authState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        authVm.clearError()
                        authLauncher.launch(authVm.createAuthIntent())
                    },
                    enabled = !authState.loading
                ) {
                    if (authState.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text("Conectar con Spotify")
                }

                // No mostramos el buscador si no hay token
                return@Column
            }
            // ---------- FIN BLOQUE SPOTIFY AUTH ----------

            // ---------- BUSCADOR ----------
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar canción") }
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { vm.search(query) },
                enabled = !state.loading
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text("Buscar")
            }

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.songs) { song ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // siguiente paso: abrir detalle y/o reproducir
                                // Ejemplo (cuando integremos Player):
                                // container.playerViewModel.play(song.spotifyUri ?: "", song.title, song.artistName ?: "")
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(song.title, style = MaterialTheme.typography.titleMedium)
                        Text(song.artistName ?: song.artistId, style = MaterialTheme.typography.bodySmall)
                    }
                    Divider()
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onGoAdmin) { Text("Admin") }
                }
            }
        }
    }
}
