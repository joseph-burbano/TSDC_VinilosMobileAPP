package com.uniandes.vinilos.network.dto

data class AlbumDto(
    val id: Int,
    val name: String,
    val cover: String?,
    val releaseDate: String?,
    val description: String?,
    val genre: String?,
    val recordLabel: String?,
    val performers: List<PerformerDto> = emptyList(),
    val tracks: List<TrackDto> = emptyList()
)

data class PerformerDto(
    val id: Int,
    val name: String,
    val image: String? = null,
    val description: String? = null
)

data class TrackDto(
    val id: Int,
    val name: String,
    val duration: String
)
