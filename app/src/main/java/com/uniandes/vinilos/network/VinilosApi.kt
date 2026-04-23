package com.uniandes.vinilos.network

import com.uniandes.vinilos.network.dto.AlbumDto
import retrofit2.http.GET

interface VinilosApi {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>
}
