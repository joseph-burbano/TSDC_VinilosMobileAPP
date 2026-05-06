package com.uniandes.vinilos.ui.artists

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.ArtistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ArtistViewModel(
    private val repository: ArtistRepository
) : ViewModel() {

    private val _performers = MutableStateFlow<List<Performer>>(emptyList())
    val performers: StateFlow<List<Performer>> = _performers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Separado de isLoading — true solo durante refresh, no durante carga inicial
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val pageSize = 4
    private val _visibleCount = MutableStateFlow(pageSize)

    // stateIn cachea el último valor de la combinación y comparte una sola suscripción
    // upstream entre todos los collectors (lista + grid + contador), lo que evita que
    // cada `collectAsStateWithLifecycle` reactive el operador `combine` por separado.
    val visiblePerformers: StateFlow<List<Performer>> =
        combine(_performers, _visibleCount) { list, count -> list.take(count) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = emptyList()
            )

    val hasMore: StateFlow<Boolean> =
        combine(_performers, _visibleCount) { list, count -> list.size > count }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = false
            )

    fun loadMore() {
        _visibleCount.value += pageSize
    }

    init { loadPerformers() }

    fun loadPerformers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _performers.value = repository.getPerformers()
            } catch (e: IOException) {
                _error.value = "Sin conexión. Revisa tu red e inténtalo de nuevo."
            } catch (e: HttpException) {
                _error.value = "El servidor respondió con un error (${e.code()})."
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun findById(performerId: Int): Performer? =
        _performers.value.find { it.id == performerId }

    fun refreshPerformers() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                // Actualiza sin borrar la lista visible — la UI no parpadea
                _performers.value = repository.refreshPerformers()
            } catch (e: IOException) {
                _error.value = "Sin conexión. Revisa tu red e inténtalo de nuevo."
            } catch (e: HttpException) {
                _error.value = "El servidor respondió con un error (${e.code()})."
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = VinilosDatabase.getDatabase(context).performerDao()
                    val repository = ArtistRepository(dao)
                    @Suppress("UNCHECKED_CAST")
                    return ArtistViewModel(repository) as T
                }
            }
    }
}
