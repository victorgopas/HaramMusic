package com.bicheator.harammusic.ui.content.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.domain.model.Role
import com.bicheator.harammusic.ui.common.CoverImage
import com.bicheator.harammusic.ui.main.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBack: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenSong: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val vm = remember(container) { ArtistDetailViewModel(container) }
    val state by vm.state.collectAsState()

    val user by container.sessionManager.currentUser.collectAsState()
    val isAdmin = user?.role == Role.ADMIN
    var showAdminDialog by remember { mutableStateOf(false) }
    var bioDraft by remember { mutableStateOf("") }

    var adminMenu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(artistId) { vm.load(artistId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.artist?.name ?: "Artista") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                },
                actions = {
                    if (isAdmin) {
                        Box {
                            IconButton(onClick = { adminMenu = true }) { Icon(Icons.Filled.MoreVert, null) }
                            DropdownMenu(expanded = adminMenu, onDismissRequest = { adminMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Editar bio") },
                                    onClick = {
                                        adminMenu = false
                                        bioDraft = state.artist?.bio.orEmpty()
                                        showAdminDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar artista") },
                                    leadingIcon = { Icon(Icons.Filled.Delete, null) },
                                    onClick = {
                                        adminMenu = false
                                        confirmDelete = true
                                    }
                                )
                            }
                        }
                    }
                }
            )

        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

            val a = state.artist ?: return@Column

            a.bio?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(it)
            }

            Spacer(Modifier.height(16.dp))
            Text("Álbumes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(a.albums, key = { it.id }) { al ->
                    OutlinedCard(
                        Modifier.width(220.dp).clickable { onOpenAlbum(al.id) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            CoverImage(
                                filePath = al.coverPath,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(al.title, style = MaterialTheme.typography.titleMedium)
                            Text(al.artistName ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Canciones", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(a.songs, key = { it.id }) { s ->
                    OutlinedCard(Modifier.fillMaxWidth().clickable { onOpenSong(s.id) }) {
                        Row(Modifier.fillMaxWidth().padding(12.dp)) {
                            CoverImage(
                                filePath = s.coverPath,
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(s.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Text(
                                    listOfNotNull(s.artistName, s.albumTitle).joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateBio(artistId, bioDraft.ifBlank { null })
                    showAdminDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { showAdminDialog = false }) { Text("Cancelar") } },
            title = { Text("Editar bio del artista") },
            text = {
                OutlinedTextField(
                    value = bioDraft,
                    onValueChange = { bioDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text("Bio") }
                )
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar artista") },
            text = { Text("Se eliminarán también sus álbumes y canciones. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = {
                    confirmDelete = false
                    vm.deleteArtist(artistId) { onBack() }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}