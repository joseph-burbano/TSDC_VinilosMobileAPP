package com.uniandes.vinilos.network

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import retrofit2.http.GET
import retrofit2.http.Path

interface VinilosApi {
    @GET("musicians")
    suspend fun getMusicians(): List<Performer>

    @GET("bands")
    suspend fun getBands(): List<Performer>

    @GET("albums")
    suspend fun getAlbums(): List<Album>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: Int): Album

    @GET("collectors")
    suspend fun getCollectors(): List<Collector>

    @GET("collectors/{id}")
    suspend fun getCollector(@Path("id") id: Int): Collector
}
