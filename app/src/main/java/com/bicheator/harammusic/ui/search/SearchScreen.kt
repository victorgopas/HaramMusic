package com.bicheator.harammusic.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(
    onGoPlaylists: () -> Unit,
    onGoProfile: () -> Unit,
    onGoAdmin: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Buscar") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, label = { Text("Buscar") }, icon = {})
                NavigationBarItem(selected = false, onClick = onGoPlaylists, label = { Text("Playlists") }, icon = {})
                NavigationBarItem(selected = false, onClick = onGoProfile, label = { Text("Perfil") }, icon = {})
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar canción / álbum / artista") }
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { /* luego ViewModel.search(query) */ }) { Text("Buscar") }

            Spacer(Modifier.height(24.dp))
            Text("Resultados (placeholder)")
            // aquí luego listas con LazyColumn
            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = onGoAdmin) {
                Text("Ir a Admin (solo si eres ADMIN)")
            }
        }
    }
}
