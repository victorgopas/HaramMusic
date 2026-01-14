package com.bicheator.harammusic.ui.content

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.data.repository.AlbumRow
import com.bicheator.harammusic.data.repository.ArtistRow
import com.bicheator.harammusic.data.repository.SongCardRow
import com.bicheator.harammusic.ui.main.LocalAppContainer

@Composable
fun ContentScreen(
    onOpenArtist: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenSong: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val vm = remember(container) { ContentViewModel(container) }
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Contenido", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = vm::setQuery,
            label = { Text("Buscar artistas, álbumes o canciones") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        TabRow(selectedTabIndex = state.tab.ordinal) {
            Tab(selected = state.tab == ContentTab.ARTISTS, onClick = { vm.setTab(ContentTab.ARTISTS) }, text = { Text("Artistas") })
            Tab(selected = state.tab == ContentTab.ALBUMS, onClick = { vm.setTab(ContentTab.ALBUMS) }, text = { Text("Álbumes") })
            Tab(selected = state.tab == ContentTab.SONGS, onClick = { vm.setTab(ContentTab.SONGS) }, text = { Text("Canciones") })
        }

        Spacer(Modifier.height(10.dp))

        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }

        when (state.tab) {
            ContentTab.ARTISTS -> ArtistList(state.artists, onOpen = { onOpenArtist(it.id) })
            ContentTab.ALBUMS -> AlbumList(state.albums, onOpen = { onOpenAlbum(it.id) })
            ContentTab.SONGS -> SongCardList(state.songs, onOpen = { onOpenSong(it.id) })
        }
    }
}

@Composable
private fun ArtistList(items: List<ArtistRow>, onOpen: (ArtistRow) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        items(items, key = { it.id }) { a ->
            OutlinedCard(Modifier.fillMaxWidth().clickable { onOpen(a) }) {
                Row(Modifier.padding(12.dp)) { Text(a.name) }
            }
        }
    }
}

@Composable
private fun AlbumList(items: List<AlbumRow>, onOpen: (AlbumRow) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        items(items, key = { it.id }) { al ->
            OutlinedCard(Modifier.fillMaxWidth().clickable { onOpen(al) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(al.title, style = MaterialTheme.typography.titleMedium)
                    Text(al.artistName ?: "Unknown", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SongCardList(items: List<SongCardRow>, onOpen: (SongCardRow) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        items(items, key = { it.id }) { s ->
            OutlinedCard(Modifier.fillMaxWidth().clickable { onOpen(s) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(s.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        listOfNotNull(s.artistName, s.albumTitle).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}