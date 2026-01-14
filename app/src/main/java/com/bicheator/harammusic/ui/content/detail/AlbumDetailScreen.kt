package com.bicheator.harammusic.ui.content.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.domain.model.Role
import com.bicheator.harammusic.ui.common.CoverImage
import com.bicheator.harammusic.ui.main.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    onBack: () -> Unit,
    onOpenSong: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val vm = remember(container) { AlbumDetailViewModel(container) }
    val state by vm.state.collectAsState()

    val user by container.sessionManager.currentUser.collectAsState()
    val isAdmin = user?.role == Role.ADMIN
    var adminMenuOpen by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var descDraft by remember { mutableStateOf("") }
    var confirmDeleteAlbum by remember { mutableStateOf(false) }
    val pickCover = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = container.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) vm.setCoverBytes(albumId, bytes)
        }
    }

    LaunchedEffect(albumId) { vm.load(albumId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.album?.title ?: "Álbum") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                actions = {
                    if (isAdmin) {
                        Box {
                            IconButton(onClick = { adminMenuOpen = true }) {
                                Icon(Icons.Filled.MoreVert, null)
                            }
                            DropdownMenu(
                                expanded = adminMenuOpen,
                                onDismissRequest = { adminMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar álbum") },
                                    onClick = {
                                        adminMenuOpen = false
                                        descDraft = state.album?.description.orEmpty()
                                        showAdminDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar álbum") },
                                    leadingIcon = { Icon(Icons.Filled.Delete, null) },
                                    onClick = {
                                        adminMenuOpen = false
                                        confirmDeleteAlbum = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },

    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

            val al = state.album ?: return@Column

            CoverImage(
                filePath = al.coverPath,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))

            Text(al.artistName ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text("Duración total: ${(al.totalDurationMs / 60000)} min", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))

            state.album?.description?.takeIf { it.isNotBlank() }?.let {
                Text("Descripción:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Text(it)
            }

            Spacer(Modifier.height(8.dp))
            Text("Canciones", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(al.songs, key = { it.id }) { s ->
                    OutlinedCard(Modifier.fillMaxWidth().clickable { onOpenSong(s.id) }) {
                        Column(Modifier.padding(12.dp)) {
                            Text(s.title, style = MaterialTheme.typography.titleMedium)
                            Text(listOfNotNull(s.artistName, s.albumTitle).joinToString(" • "), style = MaterialTheme.typography.bodySmall)
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
                    vm.updateDescription(albumId, descDraft.ifBlank { null })
                    showAdminDialog = false
                }) { Text("Guardar descripción") }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) { Text("Cerrar") }
            },
            title = { Text("Editar álbum") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = descDraft,
                        onValueChange = { descDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text("Descripción") }
                    )
                    OutlinedButton(onClick = { pickCover.launch("image/*") }) {
                        Icon(Icons.Filled.Image, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir / cambiar portada")
                    }
                }
            }
        )
    }
    if (confirmDeleteAlbum) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAlbum = false },
            title = { Text("Eliminar álbum") },
            text = {
                Text("¿Seguro que quieres eliminar este álbum? Sus canciones seguirán existiendo, pero el álbum dejará de tener ficha.")
            },
            confirmButton = {
                Button(onClick = {
                    confirmDeleteAlbum = false
                    vm.deleteAlbum(albumId) { onBack() }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAlbum = false }) { Text("Cancelar") }
            }
        )
    }
}