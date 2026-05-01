package com.uniandes.vinilos.ui.collectors

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.database.VinilosDatabase
import com.uniandes.vinilos.model.Collector
import com.uniandes.vinilos.repository.CollectorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class CollectorViewModel(
    private val repository: CollectorRepository
) : ViewModel() {

    private val _collectors = MutableStateFlow<List<Collector>>(emptyList())
    val collectors: StateFlow<List<Collector>> = _collectors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadCollectors() }

    fun loadCollectors() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _collectors.value = repository.getCollectors()
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

    fun refreshCollectors() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                _collectors.value = repository.refreshCollectors()
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

    fun loadCollector(id: Int) {
        if (_collectors.value.any { it.id == id }) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val collector = repository.getCollector(id)
                if (collector != null) {
                    _collectors.value = _collectors.value + collector
                }
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

    fun findById(collectorId: Int): Collector? =
        _collectors.value.find { it.id == collectorId }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = VinilosDatabase.getDatabase(context).collectorDao()
                    val repository = CollectorRepository(dao)
                    @Suppress("UNCHECKED_CAST")
                    return CollectorViewModel(repository) as T
                }
            }
    }
}
