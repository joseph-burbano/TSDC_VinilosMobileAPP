package com.uniandes.vinilos.ui.albums

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface AlbumsUiState {
    data object Loading : AlbumsUiState
    data class Success(val albums: List<Album>) : AlbumsUiState
    data class Error(val message: String) : AlbumsUiState
}

class AlbumViewModel(
    private val repository: AlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Loading)
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (_uiState.value !is AlbumsUiState.Success) {
            _uiState.value = AlbumsUiState.Loading
        }
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                _uiState.value = AlbumsUiState.Success(repository.getAlbums())
            } catch (e: Exception) {
                _uiState.value = AlbumsUiState.Error(e.toUserMessage())
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        if (_uiState.value !is AlbumsUiState.Success) {
            _uiState.value = AlbumsUiState.Loading
        }
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                _uiState.value = AlbumsUiState.Success(repository.refreshAlbums())
            } catch (e: Exception) {
                _uiState.value = AlbumsUiState.Error(e.toUserMessage())
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun findById(albumId: Int): Album? =
        (uiState.value as? AlbumsUiState.Success)?.albums?.find { it.id == albumId }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = VinilosDatabase.getDatabase(context).albumDao()
                    @Suppress("UNCHECKED_CAST")
                    return AlbumViewModel(AlbumRepository(dao)) as T
                }
            }
    }
}

private fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Sin conexión. Revisa tu red e inténtalo de nuevo."
    is HttpException -> "El servidor respondió con un error (${code()})."
    else -> message ?: "Ocurrió un error inesperado."
}
