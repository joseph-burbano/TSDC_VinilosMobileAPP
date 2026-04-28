package com.uniandes.vinilos.model

data class Collector(
    val id: Int,
    val name: String,
    val telephone: String = "",
    val email: String = "",
    val birthDate: String? = null,
    val description: String = "",
    val image: String = "",
    val comments: List<Comment> = emptyList(),
    val favoritePerformers: List<Performer> = emptyList(),
    val collectorAlbums: List<CollectorAlbum> = emptyList()
)
