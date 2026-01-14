package com.bicheator.harammusic.ui.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bicheator.harammusic.data.library.LibraryImportViewModel
import com.bicheator.harammusic.ui.main.LocalAppContainer

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onGoAdmin: () -> Unit
) {
    val container = LocalAppContainer.current
    val user by container.sessionManager.currentUser.collectAsState()

    val importVm = remember(container) { LibraryImportViewModel(container) }
    val importState by importVm.state.collectAsState()

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching {
                container.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION )
            }
            importVm.setFolder(uri)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)

        Text("Usuario: ${user?.username ?: "—"}")
        Text("Biblioteca: ${importState.folderUri?.toString() ?: "No seleccionada"}")

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { folderPicker.launch(null) }) {
                Text("Elegir carpeta")
            }

            Button(
                enabled = !importState.loading && importState.folderUri != null,
                onClick = { importVm.importNow() }
            ) {
                Text(if (importState.loading) "Importando..." else "Importar")
            }
        }

        importState.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        importState.lastImportedCount?.let { cnt ->
            Text("Última importación: $cnt canciones")
        }

        HorizontalDivider()

        Button(onClick = {
            container.sessionManager.clear()
            onLogout()
        }) { Text("Cerrar sesión") }
    }
}