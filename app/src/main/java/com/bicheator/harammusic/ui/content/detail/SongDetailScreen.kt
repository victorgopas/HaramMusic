package com.bicheator.harammusic.ui.content.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.domain.model.Role
import com.bicheator.harammusic.ui.common.CoverImage
import com.bicheator.harammusic.ui.common.StarRatingInput
import com.bicheator.harammusic.ui.main.LocalAppContainer

private const val MAXLEN = 500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    songId: String,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val vm = remember(container) { SongDetailViewModel(container) }
    val state by vm.state.collectAsState()

    var showEditor by remember { mutableStateOf(false) }

    val user by container.sessionManager.currentUser.collectAsState()
    val isAdmin = user?.role == Role.ADMIN

    var showAdminDialog by remember { mutableStateOf(false) }
    var genresDraft by remember { mutableStateOf("") }

    var editingReviewId by remember { mutableStateOf<String?>(null) }
    var editRating by remember { mutableDoubleStateOf(4.0) }
    var editText by remember { mutableStateOf("") }
    var confirmDeleteReviewId by remember { mutableStateOf<String?>(null) }
    var confirmDeleteSong by remember { mutableStateOf(false) }
    var adminMenuOpen by remember { mutableStateOf(false) }

    val pickCover = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = container.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) vm.setCoverBytes(songId, bytes)
        }
    }

    LaunchedEffect(songId) { vm.load(songId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.song?.title ?: "Canción") },
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
                                    text = { Text("Editar canción") },
                                    leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                    onClick = {
                                        adminMenuOpen = false
                                        genresDraft = state.song?.genres?.joinToString(", ").orEmpty()
                                        showAdminDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar canción") },
                                    leadingIcon = { Icon(Icons.Filled.Delete, null) },
                                    onClick = {
                                        adminMenuOpen = false
                                        confirmDeleteSong = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEditor = true }) { Text("★") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

            val s = state.song ?: return@Column

            Row(Modifier.fillMaxWidth()) {
                CoverImage(
                    filePath = s.coverPath,
                    modifier = Modifier.size(96.dp).clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        listOfNotNull(s.artistName, s.albumTitle).joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (s.genres.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Géneros: ${s.genres.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Puntuación media: ${"%.2f".format(s.avgRating)} (${s.reviewCount} reseñas)", style = MaterialTheme.typography.bodySmall)



            Spacer(Modifier.height(16.dp))
            Text("Reseñas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(state.reviews, key = { it.id }) { r ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        (r.userDisplayName ?: r.userId),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text("★ ${r.rating}", style = MaterialTheme.typography.titleSmall)
                                }

                                if (isAdmin) {
                                    var openMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { openMenu = true }) { Icon(Icons.Filled.MoreVert, null) }
                                        DropdownMenu(expanded = openMenu, onDismissRequest = { openMenu = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                onClick = {
                                                    openMenu = false
                                                    editingReviewId = r.id
                                                    editRating = r.rating
                                                    editText = r.text.orEmpty()
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar") },
                                                onClick = {
                                                    openMenu = false
                                                    confirmDeleteReviewId = r.id
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            r.text?.takeIf { it.isNotBlank() }?.let { Text(it) }
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
                    val list = genresDraft.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    vm.setSongGenres(songId, list)
                    showAdminDialog = false
                }) { Text("Guardar géneros") }
            },
            dismissButton = { TextButton(onClick = { showAdminDialog = false }) { Text("Cerrar") } },
            title = { Text("Editar canción") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = genresDraft,
                        onValueChange = { genresDraft = it },
                        label = { Text("Géneros (separados por comas)") },
                        modifier = Modifier.fillMaxWidth()
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
    if (showEditor) {
        val my = state.myReview
        ReviewEditorDialog(
            initialRating = my?.rating ?: 0.0,
            initialText = my?.text.orEmpty(),
            onDismiss = { showEditor = false },
            onPublish = { rating, text ->
                vm.publishReview(songId, rating, text)
                showEditor = false
            }
        )
    }
    editingReviewId?.let { rid ->
        AlertDialog(
            onDismissRequest = { editingReviewId = null },
            confirmButton = {
                TextButton(onClick = {
                    vm.adminUpdateReview(songId, rid, editRating, editText.ifBlank { null })
                    editingReviewId = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editingReviewId = null }) { Text("Cancelar") }
            },
            title = { Text("Editar reseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Puntuación: ${"%.1f".format(editRating)}")
                    Slider(
                        value = editRating.toFloat(),
                        onValueChange = { editRating = (it * 2).toInt() / 2.0 },
                        valueRange = 0.5f..5f,
                        steps = 8
                    )
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("Contenido") },
                        supportingText = { Text("${editText.length}/$MAXLEN")},
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        )
    }
    confirmDeleteReviewId?.let { rid ->
        AlertDialog(
            onDismissRequest = { confirmDeleteReviewId = null },
            title = { Text("Eliminar reseña") },
            text = { Text("¿Seguro? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.adminDeleteReview(songId, rid)
                    confirmDeleteReviewId = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteReviewId = null }) { Text("Cancelar") }
            }
        )
    }
    if (confirmDeleteSong) {
        AlertDialog(
            onDismissRequest = { confirmDeleteSong = false },
            title = { Text("Eliminar canción") },
            text = { Text("Se borrará la ficha de la canción. ¿Continuar?") },
            confirmButton = {
                Button(onClick = {
                    confirmDeleteSong = false
                    vm.deleteSong(songId) {
                        onBack()
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteSong = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ReviewEditorDialog(
    initialRating: Double,
    initialText: String,
    onDismiss: () -> Unit,
    onPublish: (Double, String?) -> Unit
) {
    var rating by remember { mutableDoubleStateOf(initialRating) }
    var text by remember { mutableStateOf(initialText) }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onPublish(rating, text) }) { Text("Publicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Reseñar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Puntuación: ${"%.1f".format(rating)}")
                StarRatingInput(
                    rating = rating,
                    onRatingChange = { rating = it }
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(MAXLEN) },
                    label = { Text("Tu reseña") },
                    supportingText = { Text("${text.length}/$MAXLEN")},
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    )
}