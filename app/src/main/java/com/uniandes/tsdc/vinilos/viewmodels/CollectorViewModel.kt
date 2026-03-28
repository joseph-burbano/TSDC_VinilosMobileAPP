package com.uniandes.tsdc.vinilos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.uniandes.tsdc.vinilos.models.Collector
import com.uniandes.tsdc.vinilos.repositories.CollectorRepository

class CollectorViewModel(application: Application) : AndroidViewModel(application) {

    private val _collectors = MutableLiveData<List<Collector>>()
    val collectors: LiveData<List<Collector>> get() = _collectors

    private val _eventNetworkError = MutableLiveData(false)
    val eventNetworkError: LiveData<Boolean> get() = _eventNetworkError

    private val _isNetworkErrorShown = MutableLiveData(false)
    val isNetworkErrorShown: LiveData<Boolean> get() = _isNetworkErrorShown

    private var collectorRepository: CollectorRepository = CollectorRepository(application)

    init {
        refreshDataFromNetwork()
    }

    fun refreshDataFromNetwork() {
        collectorRepository.getCollectors(
            onSuccess = { collectors ->
                _collectors.postValue(collectors)
                _eventNetworkError.postValue(false)
                _isNetworkErrorShown.postValue(false)
            },
            onError = {
                _eventNetworkError.postValue(true)
            }
        )
    }

    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }
}
