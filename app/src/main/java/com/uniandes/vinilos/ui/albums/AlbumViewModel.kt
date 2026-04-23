package com.uniandes.vinilos.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlbumsUiState {
    data object Loading : AlbumsUiState
    data class Success(val albums: List<Album>) : AlbumsUiState
    data class Error(val message: String) : AlbumsUiState
}

class AlbumViewModel(
    private val repository: AlbumRepository = AlbumRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Loading)
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = AlbumsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                AlbumsUiState.Success(repository.getAlbums())
            } catch (e: Exception) {
                AlbumsUiState.Error(e.message ?: "No se pudo cargar el catálogo")
            }
        }
    }

    fun findById(albumId: Int): Album? =
        (uiState.value as? AlbumsUiState.Success)?.albums?.find { it.id == albumId }
}
