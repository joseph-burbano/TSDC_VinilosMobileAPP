package com.uniandes.vinilos.database

import com.uniandes.vinilos.database.entities.AlbumEntity
import com.uniandes.vinilos.model.Album

fun AlbumEntity.toAlbum() = Album(
    id = id,
    name = name,
    cover = cover,
    releaseDate = releaseDate,
    description = description,
    genre = genre,
    recordLabel = recordLabel,
    tracks = tracks,
    performers = performers
)

fun Album.toEntity() = AlbumEntity(
    id = id,
    name = name,
    cover = cover,
    releaseDate = releaseDate,
    description = description,
    genre = genre,
    recordLabel = recordLabel,
    tracks = tracks,
    performers = performers
)
