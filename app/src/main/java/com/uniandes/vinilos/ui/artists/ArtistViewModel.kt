package com.uniandes.vinilos.ui.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandes.vinilos.model.Performer
import com.uniandes.vinilos.repository.ArtistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtistViewModel : ViewModel() {
    private val repository = ArtistRepository()

    private val _performers = MutableStateFlow<List<Performer>>(emptyList())
    val performers: StateFlow<List<Performer>> = _performers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadPerformers()
    }

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
}
