package com.bicheator.harammusic.ui.songs.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.data.repository.SongRow
import com.bicheator.harammusic.ui.common.CoverImage


@Composable
fun SongItem(
    song: SongRow,
    isCurrent: Boolean,
    onPlay: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGoToDetail: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    val colors = if (isCurrent) {
        CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    } else {
        CardDefaults.outlinedCardColors()
    }

    OutlinedCard(
        Modifier.fillMaxWidth(),
        colors = colors
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp).clickable { onPlay() },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CoverImage(
                filePath = song.coverPath,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    listOfNotNull(song.artist, song.album).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Añadir a playlist…") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (song.isFavorite) "Quitar de Favoritas" else "Añadir a Favoritas") },
                        leadingIcon = {
                            Icon(
                                if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onToggleFavorite()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ir a ficha") },
                        onClick = {
                            menuOpen = false
                            onGoToDetail()
                        }
                    )
                }
            }
        }
    }
}