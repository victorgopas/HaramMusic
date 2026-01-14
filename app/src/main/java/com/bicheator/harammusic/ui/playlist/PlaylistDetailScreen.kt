package com.bicheator.harammusic.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bicheator.harammusic.data.repository.PlaylistSongRow
import com.bicheator.harammusic.ui.main.LocalAppContainer

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val vm = remember(container, playlistId) { PlaylistDetailViewModel(container, playlistId) }
    val state by vm.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) vm.load()
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(Unit) { vm.load() }

    var renameDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.clearMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }

                Row {
                    IconButton(enabled = state.canRenameDelete, onClick = { newName = ""; renameDialog = true }) {
                        Icon(Icons.Filled.Edit, null)
                    }
                    IconButton(enabled = state.canRenameDelete, onClick = { deleteDialog = true }) {
                        Icon(Icons.Filled.Delete, null)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Editar \"${state.playlistName}\"", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))


            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(state.songs, key = { it.songId }) { s ->
                    SongInPlaylistItem(
                        song = s,
                        onRemove = if (state.canRemoveSongs) ({ vm.removeSong(s.songId) }) else null
                    )
                }
            }
        }
    }

    if (renameDialog) {
        AlertDialog(
            onDismissRequest = { renameDialog = false },
            title = { Text("Renombrar playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { vm.rename(newName); renameDialog = false }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { renameDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("Borrar playlist") },
            text = { Text("¿Seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = { vm.delete(onDeleted = onBack) }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SongInPlaylistItem(song: PlaylistSongRow, onRemove: (() -> Unit)?) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(song.artist ?: "Unknown", style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            if (onRemove != null) {
                TextButton(onClick = onRemove) { Text("Quitar") }
            }
        }
    }
}