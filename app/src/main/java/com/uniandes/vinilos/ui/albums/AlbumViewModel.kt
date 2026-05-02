package com.uniandes.vinilos.ui.albums

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Album
import com.uniandes.vinilos.repository.AlbumRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

    private val pageSize = 2
    private val _visibleCount = MutableStateFlow(pageSize)

    // stateIn comparte una sola suscripción upstream entre todos los collectors y
    // mantiene el último valor cacheado en memoria, evitando que cada recomposition
    // re-active el operador combine. Started.Eagerly hace que el flow derivado
    // siempre tenga el valor correcto disponible vía .value, incluso antes de la
    // primera suscripción de la UI.
    val visibleAlbums: StateFlow<List<Album>> =
        combine(_uiState, _visibleCount) { state, count ->
            if (state is AlbumsUiState.Success) state.albums.take(count) else emptyList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val hasMore: StateFlow<Boolean> =
        combine(_uiState, _visibleCount) { state, count ->
            if (state is AlbumsUiState.Success) state.albums.size > count else false
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun loadMore() {
        _visibleCount.value += pageSize
    }

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
