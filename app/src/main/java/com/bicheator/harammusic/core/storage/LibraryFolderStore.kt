package com.bicheator.harammusic.core.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri

class LibraryFolderStore(context: Context) {
    private val prefs = context.getSharedPreferences("library_folder", Context.MODE_PRIVATE)

    fun saveTreeUri(uri: Uri) {
        prefs.edit() { putString("tree_uri", uri.toString()) }
    }

    fun getTreeUri(): Uri? {
        val s = prefs.getString("tree_uri", null) ?: return null
        return runCatching { s.toUri() }.getOrNull()
    }

    fun clear() = prefs.edit() { clear() }
}