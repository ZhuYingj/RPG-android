package com.mobile_client.viewModels

import androidx.lifecycle.viewModelScope
import com.mobile_client.services.CurrentGameListService
import com.mobile_client.utils.GameMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CurrentGamesViewModel: BaseGameListViewModel() {
    override val isLobbyMode: Boolean = true
    private val _lobbySelected = MutableSharedFlow<String>()
    val lobbySelected = _lobbySelected.asSharedFlow()

    override fun loadMaps(isVisible: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedCurrentGames = CurrentGameListService.instance.getCurrentGames()
                _maps.value = fetchedCurrentGames.map { it.map as GameMap }
                _currentGames.value = fetchedCurrentGames // keep full lobbies if needed later
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des parties en cours: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    override fun onClick(map: GameMap) {
        viewModelScope.launch {
            val lobby = _currentGames.value.find { (it.map as GameMap)._id == map._id }
            lobby?.code?.let { _lobbySelected.emit(it) }
        }
    }
}
