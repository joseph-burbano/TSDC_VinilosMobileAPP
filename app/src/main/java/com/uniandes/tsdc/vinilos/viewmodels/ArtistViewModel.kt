package com.uniandes.tsdc.vinilos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.uniandes.tsdc.vinilos.models.Artist
import com.uniandes.tsdc.vinilos.repositories.ArtistRepository

class ArtistViewModel(application: Application) : AndroidViewModel(application) {

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = _artists

    private val _eventNetworkError = MutableLiveData(false)
    val eventNetworkError: LiveData<Boolean> get() = _eventNetworkError

    private val _isNetworkErrorShown = MutableLiveData(false)
    val isNetworkErrorShown: LiveData<Boolean> get() = _isNetworkErrorShown

    private var artistRepository: ArtistRepository = ArtistRepository(application)

    init {
        refreshDataFromNetwork()
    }

    fun refreshDataFromNetwork() {
        artistRepository.getArtists(
            onSuccess = { artists ->
                _artists.postValue(artists)
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
