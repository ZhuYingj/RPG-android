package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import com.mobile_client.utils.GameMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseGameListViewModel : ViewModel() {

    val navigationEvent = MutableSharedFlow<String>()
    protected val _maps = MutableStateFlow<List<GameMap>>(emptyList())
    val maps: StateFlow<List<GameMap>> = _maps.asStateFlow()

    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    protected val _mapIndexStart = MutableStateFlow(0)
    val mapIndexStart: StateFlow<Int> = _mapIndexStart.asStateFlow()

    protected val _mapIndexEnd = MutableStateFlow(4)
    val mapIndexEnd: StateFlow<Int> = _mapIndexEnd.asStateFlow()

    abstract fun loadMaps(isVisible: Boolean = false)
    abstract fun onClick(map: GameMap)

}
