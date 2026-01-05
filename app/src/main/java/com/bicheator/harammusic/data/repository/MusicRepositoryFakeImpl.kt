package com.bicheator.harammusic.data.repository

import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.domain.model.*
import com.bicheator.harammusic.domain.repository.MusicRepository
import kotlinx.coroutines.delay

class MusicRepositoryFakeImpl : MusicRepository {

    private val artists = listOf(
        Artist("a1", "Bad Bunny", listOf("reggaeton")),
        Artist("a2", "Rosalía", listOf("pop", "flamenco")),
        Artist("a3", "Dua Lipa", listOf("pop"))
    )

    private val albums = listOf(
        Album("al1", "Un Verano Sin Ti", "a1", "2022-05-06"),
        Album("al2", "Motomami", "a2", "2022-03-18"),
        Album("al3", "Future Nostalgia", "a3", "2020-03-27")
    )

    private val songs = listOf(
        Song("s1", "Tití Me Preguntó", "a1", "al1"),
        Song("s2", "SAOKO", "a2", "al2"),
        Song("s3", "Levitating", "a3", "al3")
    )

    override suspend fun searchSongs(query: String): AppResult<List<Song>> {
        delay(200)
        val q = query.trim().lowercase()
        return AppResult.Success(songs.filter { it.title.lowercase().contains(q) })
    }

    override suspend fun searchAlbums(query: String): AppResult<List<Album>> {
        delay(200)
        val q = query.trim().lowercase()
        return AppResult.Success(albums.filter { it.title.lowercase().contains(q) })
    }

    override suspend fun searchArtists(query: String): AppResult<List<Artist>> {
        delay(200)
        val q = query.trim().lowercase()
        return AppResult.Success(artists.filter { it.name.lowercase().contains(q) })
    }

    override suspend fun getSongDetail(id: String): AppResult<Song> {
        delay(100)
        return songs.find { it.id == id }?.let { AppResult.Success(it) }
            ?: AppResult.Error("Canción no encontrada")
    }

    override suspend fun getAlbumDetail(id: String): AppResult<Album> {
        delay(100)
        return albums.find { it.id == id }?.let { AppResult.Success(it) }
            ?: AppResult.Error("Álbum no encontrado")
    }
}
