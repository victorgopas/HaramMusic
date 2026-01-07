package com.bicheator.harammusic.ui.explore

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.ui.auth.SpotifyAuthViewModel
import com.bicheator.harammusic.ui.main.LocalAppContainer
import com.bicheator.harammusic.ui.search.SearchViewModel

@Composable
fun ExploreScreen(
    onGoAdmin: () -> Unit,
    onOpenSongDetail: (songId: String) -> Unit
) {
    val container = LocalAppContainer.current

    // Auth Spotify
    val authVm = remember(container) {
        SpotifyAuthViewModel(container.spotifyAuthManager, container.spotifyTokenStore)
    }
    val authState by authVm.state.collectAsState()

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authVm.handleAuthResult(result.data)
    }

    // Explore (sugerencias)
    val exploreVm = remember(container) { ExploreViewModel(container.getExploreUseCase) }
    val exploreState by exploreVm.state.collectAsState()

    // Search (cuando el usuario busca)
    val searchVm = remember(container) { SearchViewModel(container.searchUseCase) }
    val searchState by searchVm.state.collectAsState()

    var query by remember { mutableStateOf("") }
    val isSearching = query.trim().isNotBlank()

    LaunchedEffect(authState.connected) {
        if (authState.connected) exploreVm.load()
    }

    // Admin solo si admin
    val user by container.sessionManager.currentUser.collectAsState()
    val isAdmin = user?.role?.name == "ADMIN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Spotify auth gate ---
        if (!authState.connected) {
            Text(
                "Conéctate con Spotify para ver recomendaciones y buscar.",
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
            return@Column
        }

        // --- Search bar ---
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar canción") }
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { searchVm.search(query) },
            enabled = !searchState.loading
        ) {
            if (searchState.loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text("Buscar")
        }

        if (searchState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(searchState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        // --- Contenido: búsqueda vs sugerencias ---
        if (isSearching) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchState.songs) { song ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val uri = song.spotifyUri
                                if (!uri.isNullOrBlank()) {
                                    container.playerViewModel.play(
                                        spotifyUri = uri,
                                        title = song.title,
                                        artist = song.artistName ?: ""
                                    )
                                } else {
                                    onOpenSongDetail(song.id)
                                }
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(song.title, style = MaterialTheme.typography.titleMedium)
                        Text(song.artistName ?: song.artistId, style = MaterialTheme.typography.bodySmall)
                    }
                    Divider()
                }
            }
        } else {
            if (exploreState.loading) {
                CircularProgressIndicator()
            } else if (exploreState.error != null) {
                Text(exploreState.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                Text("Artistas sugeridos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                LazyRow {
                    items(exploreState.suggestedArtists) { artist ->
                        Card(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .width(160.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(artist.name, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Canciones sugeridas", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(exploreState.suggestedSongs) { song ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val uri = song.spotifyUri
                                    if (!uri.isNullOrBlank()) {
                                        container.playerViewModel.play(
                                            spotifyUri = uri,
                                            title = song.title,
                                            artist = song.artistName ?: ""
                                        )
                                    } else {
                                        onOpenSongDetail(song.id)
                                    }
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(song.title, style = MaterialTheme.typography.titleMedium)
                            Text(song.artistName ?: song.artistId, style = MaterialTheme.typography.bodySmall)
                        }
                        Divider()
                    }

                    if (isAdmin == true) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(onClick = onGoAdmin) { Text("Admin") }
                        }
                    }
                }
            }
        }
    }
}
