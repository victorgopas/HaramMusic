package com.bicheator.harammusic.data.library

import android.net.Uri
import com.bicheator.harammusic.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LibraryImportUiState(
    val folderUri: Uri? = null,
    val loading: Boolean = false,
    val message: String? = null,
    val lastImportedCount: Int? = null
)

class LibraryImportViewModel(private val container: AppContainer) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(
        LibraryImportUiState(folderUri = container.libraryFolderStore.getTreeUri())
    )

    private var didAutoImport = false

    val state: StateFlow<LibraryImportUiState> = _state.asStateFlow()

    fun setFolder(uri: Uri) {
        container.libraryFolderStore.saveTreeUri(uri)
        _state.update { it.copy(folderUri = uri, message = null) }
    }

    fun importNow() {
        val uri = _state.value.folderUri
        if (uri == null) {
            _state.update { it.copy(message = "Primero selecciona una carpeta") }
            return
        }

        scope.launch {
            _state.update { it.copy(loading = true, message = null, lastImportedCount = null) }

            val userId = container.sessionManager.getUserIdOrNull()
            if (userId == null) {
                _state.update { it.copy(loading = false, message = "Sesión no válida") }
                return@launch
            }

            val result: ImportResult = withContext(Dispatchers.IO) {
                container.libraryImportRepository.importFromTreeUri(uri)
            }

            if (!result.ok) {
                _state.update { it.copy(loading = false, message = result.error ?: "Error importando") }
                return@launch
            }

            withContext(Dispatchers.IO) {
                container.playlistManagementRepository.ensureSystemPlaylists(userId)
            }

            _state.update {
                it.copy(
                    loading = false,
                    lastImportedCount = result.importedCount,
                    message = "Importadas ${result.importedCount} canciones"
                )
            }
        }
    }

    fun autoImportIfPossible() {
        if (didAutoImport) return
        didAutoImport = true
        if (_state.value.folderUri != null) importNow()
    }

        fun clear() = scope.cancel()
}