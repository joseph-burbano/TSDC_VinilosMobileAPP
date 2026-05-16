package com.uniandes.vinilos.network

import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.model.PerformerPrize
import com.uniandes.vinilos.model.Prize
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    @GET("prizes")
    suspend fun getPrizes(): List<Prize>

    @POST("prizes")
    suspend fun createPrize(@Body prize: PrizeCreateBody): Prize

    @POST("prizes/{prizeId}/musicians/{musicianId}")
    suspend fun associatePrizeToMusician(
        @Path("prizeId") prizeId: Int,
        @Path("musicianId") musicianId: Int,
        @Body body: PerformerPrizeBody
    ): PerformerPrize

    @POST("prizes/{prizeId}/bands/{bandId}")
    suspend fun associatePrizeToBand(
        @Path("prizeId") prizeId: Int,
        @Path("bandId") bandId: Int,
        @Body body: PerformerPrizeBody
    ): PerformerPrize
}

data class PrizeCreateBody(
    val name: String,
    val description: String,
    val organization: String
)

data class PerformerPrizeBody(
    val premiationDate: String
)
