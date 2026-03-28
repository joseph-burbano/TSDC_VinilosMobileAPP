package com.uniandes.tsdc.vinilos.repositories

import android.app.Application
import com.uniandes.tsdc.vinilos.models.Collector
import com.uniandes.tsdc.vinilos.network.NetworkServiceAdapter

class CollectorRepository(val application: Application) {

    private var networkServiceAdapter: NetworkServiceAdapter = NetworkServiceAdapter.getInstance(application)

    fun getCollectors(onSuccess: (List<Collector>) -> Unit, onError: (Exception) -> Unit) {
        networkServiceAdapter.getCollectors(onSuccess, onError)
    }
}
