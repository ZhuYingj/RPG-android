package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.GameListService
import com.mobile_client.utils.GameMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameListViewModel : ViewModel() {  // ← Changed from 'object' to 'class'
    private val _maps = MutableStateFlow<List<GameMap>>(emptyList())
    val maps: StateFlow<List<GameMap>> = _maps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _mapIndexStart = MutableStateFlow(0)
    val mapIndexStart: StateFlow<Int> = _mapIndexStart.asStateFlow()

    private val _mapIndexEnd = MutableStateFlow(4)
    val mapIndexEnd: StateFlow<Int> = _mapIndexEnd.asStateFlow()

    init {
        // This runs when the ViewModel is created
        loadMaps()
    }

    fun loadMaps(isVisible: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedMaps = GameListService.getAllMaps(isVisible)
                _maps.value = fetchedMaps

                println("Loaded ${fetchedMaps.size} maps")
                fetchedMaps.forEach { map ->
                    println("Map: ${map.name}, ID: ${map._id}")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Erreur lors du chargement des jeux: ${e.message}"
                println("Error loading maps: ${e.message}")
                println("Stack trace: ${e.stackTraceToString()}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onClick(map: GameMap) {
        viewModelScope.launch {
            try {
                println("Starting game with map: ${map.name}, ID: ${map._id}")
                // TODO: Implement play game logic
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Erreur lors du démarrage du jeu: ${e.message}"
            }
        }
    }
}
