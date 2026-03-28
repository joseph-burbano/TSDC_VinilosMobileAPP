package com.uniandes.tsdc.vinilos.repositories

import android.app.Application
import com.uniandes.tsdc.vinilos.models.Artist
import com.uniandes.tsdc.vinilos.network.NetworkServiceAdapter

class ArtistRepository(private val application: Application) {

    private var networkServiceAdapter: NetworkServiceAdapter = NetworkServiceAdapter.getInstance(application)

    fun getArtists(onSuccess: (List<Artist>) -> Unit, onError: (Exception) -> Unit) {
        networkServiceAdapter.getArtists(onSuccess, onError)
    }
}
