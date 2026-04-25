package com.uniandes.vinilos.repository

import com.uniandes.vinilos.database.dao.AlbumDao
import com.uniandes.vinilos.database.toAlbum
import com.uniandes.vinilos.database.toEntity
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Artist
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.network.NetworkServiceAdapter
import com.uniandes.vinilos.network.VinilosApi
import com.uniandes.vinilos.network.dto.AlbumDto

class AlbumRepository(
    private val dao: AlbumDao,
    private val api: VinilosApi = NetworkServiceAdapter.api
) {

    suspend fun getAlbums(): List<Album> {
        val cached = dao.getAll()
        if (cached.isNotEmpty()) {
            return cached.map { it.toAlbum() }
        }
        return fetchAndCache()
    }

    suspend fun refreshAlbums(): List<Album> {
        dao.deleteAll()
        return fetchAndCache()
    }

    private suspend fun fetchAndCache(): List<Album> {
        val albums = api.getAlbums().map { it.toModel() }
        dao.insertAll(albums.map { it.toEntity() })
        return albums
    }
}

private fun AlbumDto.toModel(): Album = Album(
    id = id,
    name = name,
    cover = cover.orEmpty(),
    releaseDate = releaseDate?.take(4).orEmpty(),
    description = description.orEmpty(),
    genre = genre.orEmpty(),
    recordLabel = recordLabel.orEmpty(),
    tracks = tracks.map { Track(it.id, it.name, it.duration) },
    artists = performers.map {
        Artist(
            id = it.id,
            name = it.name,
            image = it.image.orEmpty(),
            description = it.description.orEmpty(),
            birthDate = ""
        )
    }
)
