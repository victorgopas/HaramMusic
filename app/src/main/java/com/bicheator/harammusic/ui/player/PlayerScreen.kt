package com.bicheator.harammusic.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.ui.common.CoverImage
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen() {
    val vm = LocalPlayerViewModel.current
    val state by vm.state.collectAsState()

    val current = state.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reproductor") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (current == null) {
                Text("No hay ninguna canción reproduciéndose.")
                return@Column
            }

            CoverImage(
                filePath = current.coverPath,
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = current.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = listOfNotNull(current.artist, current.album).joinToString(" • ").ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(Modifier.height(18.dp))

            val durationMs = max(0L, state.durationMs)
            val positionMs = min(durationMs, max(0L, state.positionMs))

            var dragging by remember { mutableStateOf(false) }
            var sliderValue by remember { mutableStateOf(0f) }

            LaunchedEffect(positionMs, durationMs, dragging) {
                if (!dragging && durationMs > 0L) {
                    sliderValue = positionMs.toFloat() / durationMs.toFloat()
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(positionMs), style = MaterialTheme.typography.bodySmall)
                Text(formatTime(durationMs), style = MaterialTheme.typography.bodySmall)
            }

            Slider(
                value = if (durationMs == 0L) 0f else sliderValue,
                onValueChange = { v ->
                    dragging = true
                    sliderValue = v
                },
                onValueChangeFinished = {
                    dragging = false
                    if (durationMs > 0L) vm.seekTo((sliderValue * durationMs).toLong())
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            val hasPrev = state.currentIndex > 0
            val hasNext = state.currentIndex < (state.queue.size - 1)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = vm::prev, enabled = hasPrev) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior")
                }

                Spacer(Modifier.width(18.dp))

                FilledIconButton(
                    onClick = vm::togglePlayPause,
                    modifier = Modifier.size(64.dp)
                ) {
                    val centerIcon = when {
                        state.isPlaying -> Icons.Filled.Pause
                        state.endedAtEnd -> Icons.Filled.Replay
                        else -> Icons.Filled.PlayArrow
                    }

                    val centerDesc = when {
                        state.isPlaying -> "Pausa"
                        state.endedAtEnd -> "Reiniciar"
                        else -> "Play"
                    }

                    Icon(centerIcon, contentDescription = centerDesc)
                }

                Spacer(Modifier.width(18.dp))

                IconButton(onClick = vm::next, enabled = hasNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Siguiente")
                }
            }

            state.message?.let {
                Spacer(Modifier.height(14.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).toInt().coerceAtLeast(0)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}