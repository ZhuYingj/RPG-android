package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.AccountService
import com.mobile_client.services.CosmeticService
import com.mobile_client.utils.Cosmetic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {
    private val cosmeticService = CosmeticService.instance

    private val _shopItems = MutableStateFlow<List<Cosmetic>>(emptyList())
    val shopItems: StateFlow<List<Cosmetic>> = _shopItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _money = MutableStateFlow(0)
    val money: StateFlow<Int> = _money

    fun clearMessage() { _message.value = null }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            AccountService.instance.fetchAccount()
            _money.value = AccountService.instance.money.intValue
            _shopItems.value = cosmeticService.loadShop()
            _isLoading.value = false
        }
    }

    fun buyItem(item: Cosmetic) {
        viewModelScope.launch {
            val success = cosmeticService.buyItem(item._id)
            if (success) {
                AccountService.instance.fetchAccount()
                _money.value = AccountService.instance.money.intValue
                _message.value = "Achat Réussi"
            } else {
                _message.value = "Échec de l'achat"
            }
        }
    }
}
