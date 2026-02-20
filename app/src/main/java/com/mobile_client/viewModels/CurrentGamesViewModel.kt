package com.mobile_client.viewModels

import androidx.lifecycle.viewModelScope
import com.mobile_client.services.GameListService
import com.mobile_client.utils.GameMap
import kotlinx.coroutines.launch

class CurrentGamesViewModel: BaseGameListViewModel() {
    override fun loadMaps(isVisible: Boolean) {
        //TODO: Charger les parties en cours
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedMaps = GameListService.instance.getAllMaps(isVisible)
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
    override fun onClick(map: GameMap) {
        //TODO: Joindre la partie
    }
}
