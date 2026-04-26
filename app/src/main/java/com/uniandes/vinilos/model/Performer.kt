package com.uniandes.vinilos.model

data class Performer(
    val id: Int,
    val name: String,
    val image: String,
    val description: String,
    val birthDate: String? = null,
    val creationDate: String? = null,
    val albums: List<Album> = emptyList()
)
