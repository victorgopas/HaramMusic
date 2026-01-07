package com.bicheator.harammusic.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MiniPlayerBar(
    state: MiniPlayerUiState,
    onToggle: () -> Unit
) {
    if (!state.visible) return

    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(state.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(state.subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                if (state.error != null) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = onToggle) {
                Text(if (state.isPlaying) "Pausar" else "Play")
            }
        }
    }
}
