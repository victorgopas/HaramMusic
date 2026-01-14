package com.bicheator.harammusic.data.library

data class ImportResult(
    val ok: Boolean,
    val importedCount: Int = 0,
    val error: String? = null
)