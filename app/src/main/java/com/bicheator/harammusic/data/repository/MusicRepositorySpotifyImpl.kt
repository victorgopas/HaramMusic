package com.bicheator.harammusic.data.repository

import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.data.remote.spotify.api.SpotifyApiService
import com.bicheator.harammusic.domain.model.Album
import com.bicheator.harammusic.domain.model.Artist
import com.bicheator.harammusic.domain.model.Song
import com.bicheator.harammusic.domain.repository.MusicRepository

class MusicRepositorySpotifyImpl(
    private val api: SpotifyApiService
) : MusicRepository {

    override suspend fun getExplore(): AppResult<Pair<List<Song>, List<Artist>>> = try {
        val seed = listOf("bad bunny", "rosalia", "dua lipa", "drake").random()

        val tracksRes = api.searchTracks(query = seed, limit = 12)
        val artistsRes = api.searchArtists(query = seed, limit = 8)

        val songs = tracksRes.tracks.items.map { t ->
            Song(
                id = t.id,
                title = t.name,
                artistId = t.artists.firstOrNull()?.id ?: "",
                albumId = t.album.id,
                previewUrl = t.previewUrl,
                artistName = t.artists.firstOrNull()?.name,
                albumName = t.album.name,
                imageUrl = t.album.images.firstOrNull()?.url,
                spotifyUri = t.uri,
                durationMs = t.durationMs
            )
        }

        val artists = artistsRes.artists.items.map { a ->
            Artist(
                id = a.id,
                name = a.name,
                imageUrl = a.images.firstOrNull()?.url,
                spotifyUri = a.uri
            )
        }

        AppResult.Success(songs to artists)
    } catch (e: Exception) {
        AppResult.Error("Error cargando explorar", e)
    }

    override suspend fun searchSongs(query: String): AppResult<List<Song>> = try {
        val res = api.searchTracks(query = query)

        val songs = res.tracks.items.map { t ->
            Song(
                id = t.id,
                title = t.name,
                artistId = t.artists.firstOrNull()?.id ?: "",
                albumId = t.album.id,
                previewUrl = t.previewUrl,
                artistName = t.artists.firstOrNull()?.name,
                albumName = t.album.name,
                imageUrl = t.album.images.firstOrNull()?.url,
                spotifyUri = t.uri,
                durationMs = t.durationMs
            )
        }

        AppResult.Success(songs)
    } catch (e: Exception) {
        AppResult.Error("Error buscando en Spotify", e)
    }

    override suspend fun searchAlbums(query: String): AppResult<List<Album>> =
        AppResult.Success(emptyList())

    override suspend fun searchArtists(query: String): AppResult<List<Artist>> =
        AppResult.Success(emptyList())

    override suspend fun getSongDetail(id: String): AppResult<Song> =
        AppResult.Error("No implementado aún")

    override suspend fun getAlbumDetail(id: String): AppResult<Album> =
        AppResult.Error("No implementado aún")
}
