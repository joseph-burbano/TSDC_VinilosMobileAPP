package com.uniandes.vinilos.repository

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Artist
import com.uniandes.vinilos.model.Track
import com.uniandes.vinilos.network.NetworkServiceAdapter
import com.uniandes.vinilos.network.VinilosApi
import com.uniandes.vinilos.network.dto.AlbumDto

class AlbumRepository(
    private val api: VinilosApi = NetworkServiceAdapter.api
) {
    suspend fun getAlbums(): List<Album> = api.getAlbums().map { it.toModel() }
}

private fun AlbumDto.toModel(): Album = Album(
    id = id,
    name = name,
    cover = cover.orEmpty(),
    // Backend returns ISO timestamps (e.g. "1969-07-30T00:00:00.000Z"); UI only shows the year.
    releaseDate = releaseDate?.take(4).orEmpty(),
    description = description.orEmpty(),
    genre = genre.orEmpty(),
    recordLabel = recordLabel.orEmpty(),
    tracks = tracks.map { Track(it.id, it.name, it.duration) },
    // `performers` in the backend maps to `artists` in the mobile model.
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
