package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.CosmeticService
import com.mobile_client.utils.Cosmetic
import com.mobile_client.utils.InventoryCosmetic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {
    private val cosmeticService = CosmeticService.instance

    private val _inventory = MutableStateFlow<List<InventoryCosmetic>>(emptyList())
    val inventory: StateFlow<List<InventoryCosmetic>> = _inventory

    private val _equipped = MutableStateFlow<List<Cosmetic>>(emptyList())
    val equipped: StateFlow<List<Cosmetic>> = _equipped

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() { _message.value = null }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            cosmeticService.loadShop()
            val dto = cosmeticService.loadInventory()
            _inventory.value = dto.inventory
            _equipped.value = dto.equipped
            _isLoading.value = false
        }
    }

    fun getCosmetic(cosmeticId: String): Cosmetic? {
        return cosmeticService.getCosmetic(cosmeticId)
    }

    fun isEquipped(item: InventoryCosmetic): Boolean {
        return _equipped.value.any { it._id == item.cosmeticId }
    }

    fun equipItem(item: InventoryCosmetic) {
        viewModelScope.launch {
            val dto = cosmeticService.equipItem(item.cosmeticId)
            if (dto != null) {
                _inventory.value = dto.inventory
                _equipped.value = dto.equipped
                _message.value = "Équipement réussi"
            } else {
                _message.value = "Échec de l'équipement"
            }
        }
    }

    fun unequipItem(item: InventoryCosmetic) {
        viewModelScope.launch {
            val dto = cosmeticService.unequipItem(item.cosmeticId)
            if (dto != null) {
                _inventory.value = dto.inventory
                _equipped.value = dto.equipped
                _message.value = "Déséquipement réussi"
            } else {
                _message.value = "Échec du déséquipement"
            }
        }
    }
}
