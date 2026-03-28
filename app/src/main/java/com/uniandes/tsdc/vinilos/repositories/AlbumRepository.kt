package com.uniandes.tsdc.vinilos.repositories

import android.app.Application
import com.uniandes.tsdc.vinilos.models.Album
import com.uniandes.tsdc.vinilos.network.NetworkServiceAdapter

class AlbumRepository(private val application: Application) {

    private var networkServiceAdapter: NetworkServiceAdapter = NetworkServiceAdapter.getInstance(application)

    fun getAlbums(onSuccess: (List<Album>) -> Unit, onError: (Exception) -> Unit) {
        networkServiceAdapter.getAlbums(onSuccess, onError)
    }
}
