package com.bicheator.harammusic.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Admin", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Gestión de usuarios / contenido (placeholder)")
    }
}
