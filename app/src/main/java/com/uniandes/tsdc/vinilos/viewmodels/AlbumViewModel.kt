package com.uniandes.tsdc.vinilos.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.uniandes.tsdc.vinilos.models.Album
import com.uniandes.tsdc.vinilos.repositories.AlbumRepository

class AlbumViewModel(application: Application) : AndroidViewModel(application) {

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> get() = _albums

    private val _eventNetworkError = MutableLiveData(false)
    val eventNetworkError: LiveData<Boolean> get() = _eventNetworkError

    private val _isNetworkErrorShown = MutableLiveData(false)
    val isNetworkErrorShown: LiveData<Boolean> get() = _isNetworkErrorShown

    private var albumRepository: AlbumRepository = AlbumRepository(application)

    init {
        refreshDataFromNetwork()
    }

    fun refreshDataFromNetwork() {
        albumRepository.getAlbums(
            onSuccess = { albums ->
                _albums.postValue(albums)
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
