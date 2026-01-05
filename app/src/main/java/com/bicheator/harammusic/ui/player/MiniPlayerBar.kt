package com.bicheator.harammusic.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class MiniPlayerUiState(
    val visible: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val isPlaying: Boolean = false
)

@Composable
fun MiniPlayerBar(
    state: MiniPlayerUiState,
    onPlayPause: () -> Unit,
    onOpenNowPlaying: () -> Unit
) {
    if (!state.visible) return

    Surface(tonalElevation = 2.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(state.title, maxLines = 1)
                Text(state.subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
        }
    }
}