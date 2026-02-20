package com.mobile_client.viewModels

import androidx.lifecycle.viewModelScope
import com.mobile_client.utils.Screen
import com.mobile_client.services.GameListService
import com.mobile_client.services.GameLobbyService
import com.mobile_client.utils.GameMap
import kotlinx.coroutines.launch

class GameListViewModel : BaseGameListViewModel() {  // ← Changed from 'object' to 'class'

    override fun loadMaps(isVisible: Boolean) {
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
        viewModelScope.launch {
            try {
                GameLobbyService.map.value = map
                GameLobbyService.createLobby(
                    onSuccess = {
                        viewModelScope.launch {
                            navigationEvent.emit(Screen.CharacterCreation.route)
                        }
                    },
                    onError = { message ->
                        _error.value = message
                    }
                )
                println("Starting game with map: ${map.name}, ID: ${map._id}")
                // TODO: Implement play game logic
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Erreur lors du démarrage du jeu: ${e.message}"
            }
        }
    }
}
