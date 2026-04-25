package com.uniandes.vinilos.ui.artists

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.ArtistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ArtistViewModel(
    private val repository: ArtistRepository
) : ViewModel() {

    private val _performers = MutableStateFlow<List<Performer>>(emptyList())
    val performers: StateFlow<List<Performer>> = _performers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val pageSize = 4
    private val _visibleCount = MutableStateFlow(pageSize)

    val visiblePerformers = combine(_performers, _visibleCount) { list, count ->
        list.take(count)
    }

    val hasMore = combine(_performers, _visibleCount) { list, count ->
        list.size > count
    }

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
            } catch (e: Exception) {
                _error.value = "Error al cargar artistas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun findById(performerId: Int): Performer? =
        _performers.value.find { it.id == performerId }

    fun refreshPerformers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _performers.value = repository.refreshPerformers()
            } catch (e: Exception) {
                _error.value = "Error al actualizar artistas: ${e.message}"
            } finally {
                _isLoading.value = false
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