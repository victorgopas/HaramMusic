package com.bicheator.harammusic.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.ui.main.LocalAppContainer

@Composable
fun HomeScreen(
    onOpenPlaylist: (String) -> Unit = {},
    onCreatePlaylist: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val user by container.sessionManager.currentUser.collectAsState()

    val vm = remember(container) { HomeViewModel(container.getMyPlaylistsUseCase) }
    val state by vm.state.collectAsState()

    LaunchedEffect(user?.id) {
        val id = user?.id ?: return@LaunchedEffect
        vm.load(id)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tus playlists", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (state.loading) {
            CircularProgressIndicator()
            return@Column
        }

        if (state.error != null) {
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (state.playlists.isEmpty()) {
            Text("Aún no tienes playlists.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onCreatePlaylist) { Text("Crear playlist") }
        } else {
            LazyColumn {
                items(state.playlists) { pl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onOpenPlaylist(pl.id) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(pl.name, style = MaterialTheme.typography.titleMedium)
                            Text("${pl.songIds.size} canciones", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
