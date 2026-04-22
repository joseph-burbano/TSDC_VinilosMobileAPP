package com.uniandes.vinilos.repository

import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.network.NetworkServiceAdapter

class ArtistRepository {
    private val api = NetworkServiceAdapter.api

    suspend fun getMusicians(): List<Performer> {
        return api.getMusicians()
    }

    suspend fun getBands(): List<Performer> {
        return api.getBands()
    }

    suspend fun getPerformers(): List<Performer> {
        return getMusicians() + getBands()
    }
}
