package com.bicheator.harammusic.data.library

import android.content.Context
import java.io.File
import java.util.UUID

class CoverStorage(private val context: Context) {
    fun saveCoverBytes(bytes: ByteArray): String {
        val dir = File(context.filesDir, "covers").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.writeBytes(bytes)
        return file.absolutePath
    }
}