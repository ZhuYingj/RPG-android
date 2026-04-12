package com.mobile_client.viewModels

import androidx.lifecycle.viewModelScope
import com.mobile_client.services.GameListService
import com.mobile_client.utils.GameMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class GameListViewModel : BaseGameListViewModel() {

    private val _mapSelected = MutableSharedFlow<GameMap>()
    val mapSelected = _mapSelected.asSharedFlow()

    override fun loadMaps(isVisible: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedMaps = GameListService.instance.getAllMaps()
                _maps.value = fetchedMaps

            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Erreur lors du chargement des jeux: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onClick(map: GameMap) {
        viewModelScope.launch {
            _mapSelected.emit(map)
        }
    }
}
