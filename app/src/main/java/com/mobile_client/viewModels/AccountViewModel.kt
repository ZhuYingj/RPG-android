package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.AccountService
import com.mobile_client.utils.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account

    init {
        _account.value = AccountService.instance.accountInfo
    }

    fun updateAccount(name: String, email: String) {
        viewModelScope.launch {
            val current = _account.value ?: return@launch
            val updated = current.copy(username = name, email = email)
            _account.value = updated
            AccountService.instance.setAccount(updated, AccountService.instance.token ?: "")
        }
    }
}
