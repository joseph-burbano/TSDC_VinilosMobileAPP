package com.uniandes.vinilos.model

data class CollectorAlbum(
    val id: Int,
    val price: Int = 0,
    val status: String = "",
    val album: Album? = null
)
