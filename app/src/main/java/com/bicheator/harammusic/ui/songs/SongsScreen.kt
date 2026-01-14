package com.bicheator.harammusic.ui.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.data.repository.PlaylistLite
import com.bicheator.harammusic.ui.main.LocalAppContainer
import com.bicheator.harammusic.ui.player.LocalPlayerViewModel
import com.bicheator.harammusic.ui.songs.components.SongItem
import kotlinx.coroutines.launch

@Composable
fun SongsScreen(
    onOpenPlaylists: () -> Unit,
    onOpenSongDetail: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val playerVm = LocalPlayerViewModel.current

    val vm = remember(container) { SongsViewModel(container) }
    val state by vm.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var addDialogOpen by remember { mutableStateOf(false) }
    var pendingSongId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Canciones", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            PlaylistSelector(
                playlists = state.playlists,
                selected = state.selectedPlaylist,
                onSelect = vm::selectPlaylist
            )
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onOpenPlaylists) { Text("Gestionar playlists") }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::setQuery,
                    label = { Text("Buscar") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = vm::toggleFilterMenu) {
                    Icon(Icons.Filled.FilterList, contentDescription = null)
                }
            }

            if (state.filterMenuOpen) {
                Spacer(Modifier.height(8.dp))
                FilterPanel(
                    filterArtist = state.filterArtist,
                    filterAlbum = state.filterAlbum,
                    onArtistChange = vm::setFilterArtist,
                    onAlbumChange = vm::setFilterAlbum
                )
            }

            Spacer(Modifier.height(12.dp))

            state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            val selected = state.selectedPlaylist
            val currentSongId = playerVm.currentSongId()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(state.songs, key = { it.id }) { song ->
                    SongItem(
                        song = song,
                        isCurrent = (currentSongId == song.id),
                        onPlay = {
                            if (selected != null) {
                                playerVm.playFromPlaylist(selected.id, selected.type, song.id)
                            }
                        },
                        onAddToPlaylist = {
                            pendingSongId = song.id
                            addDialogOpen = true
                        },
                        onToggleFavorite = {
                            vm.toggleFavorite(song.id, song.isFavorite) { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        },
                        onGoToDetail = {
                            scope.launch {
                                val res = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    container.libraryRepository.getSongDetail(song.id)
                                }
                                when (res) {
                                    is com.bicheator.harammusic.core.result.AppResult.Success<*> -> onOpenSongDetail(song.id)
                                    is com.bicheator.harammusic.core.result.AppResult.Error ->
                                        snackbarHostState.showSnackbar("Ficha no disponible")
                                }
                            }
                        }
                    )
                }
            }
        }

        if (addDialogOpen) {
            val songId = pendingSongId
            AddToPlaylistDialog(
                playlists = state.playlists,
                onDismiss = { addDialogOpen = false; pendingSongId = null },
                onPick = { playlist ->
                    addDialogOpen = false
                    pendingSongId = null
                    if (songId == null) return@AddToPlaylistDialog

                    if (playlist.type == "ALL_SONGS") {
                        scope.launch { snackbarHostState.showSnackbar("No puedes añadir a 'Todas las canciones'") }
                        return@AddToPlaylistDialog
                    }

                    vm.addSongToPlaylist(songId, playlist.id) { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            )
        }
    }
}

@Composable
private fun PlaylistSelector(
    playlists: List<PlaylistLite>,
    selected: PlaylistLite?,
    onSelect: (PlaylistLite) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = true }.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(selected?.name ?: "Selecciona playlist")
            Text("▼")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                playlists.forEach { pl ->
                    DropdownMenuItem(
                        text = { Text(pl.name) },
                        onClick = { expanded = false; onSelect(pl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(
    filterArtist: String,
    filterAlbum: String,
    onArtistChange: (String) -> Unit,
    onAlbumChange: (String) -> Unit
) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = filterArtist,
                onValueChange = onArtistChange,
                label = { Text("Artista") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = filterAlbum,
                onValueChange = onAlbumChange,
                label = { Text("Álbum") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<PlaylistLite>,
    onDismiss: () -> Unit,
    onPick: (PlaylistLite) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a playlist") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(playlists, key = { it.id }) { pl ->
                    OutlinedButton(
                        onClick = { onPick(pl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(pl.name, maxLines = 1)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}