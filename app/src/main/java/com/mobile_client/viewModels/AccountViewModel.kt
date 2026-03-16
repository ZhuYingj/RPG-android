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

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val accountService = AccountService.instance

    init {
        _account.value = accountService.accountInfo
    }

    fun updateAccount(name: String, email: String) {
        viewModelScope.launch {
            val current = accountService.accountInfo ?: return@launch
            val usernameChanged = current.username != name
            val emailChanged = current.email != email

            if (!usernameChanged && !emailChanged) return@launch

            try {
                if (usernameChanged) accountService.updateUsername(name)
                if (emailChanged) accountService.updateEmail(email)
                _account.value = accountService.accountInfo
                _message.value = "Informations mises à jour avec succès"
            } catch (e: Exception) {
                _message.value = "${e.message}"
            }
        }
    }

    fun updateAccountAvatar(base64Avatar: String) {
        viewModelScope.launch {
            try {
                accountService.updateAvatar(base64Avatar)
                _account.value = accountService.accountInfo
                _message.value = "Avatar mis à jour avec succès"
            } catch (e: Exception) {
                _message.value = "${e.message}"
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val deleted = accountService.deleteAccount()
                if (deleted) {
                    accountService.logout()
                    onSuccess()
                } else {
                    _message.value = "Échec de la suppression du compte"
                }
            } catch (e: Exception) {
                _message.value = "${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
