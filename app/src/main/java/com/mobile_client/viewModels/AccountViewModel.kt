package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.AccountService
import com.mobile_client.utils.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import com.mobile_client.services.CosmeticService
import com.mobile_client.utils.Cosmetic

class AccountViewModel : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val accountService = AccountService.instance

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap

    private val _showQrCode = MutableStateFlow(false)
    val showQrCode: StateFlow<Boolean> = _showQrCode

    private val cosmeticService = CosmeticService.instance
    private val _ownedAvatarCosmetics = MutableStateFlow<List<Cosmetic>>(emptyList())
    val ownedAvatarCosmetics: StateFlow<List<Cosmetic>> = _ownedAvatarCosmetics

    init {
        _account.value = accountService.accountInfo
        generateQrCode()
        loadOwnedAvatarCosmetics()
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

    private fun generateQrCode() {
        val username = accountService.accountInfo?.username ?: return
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(username, com.google.zxing.BarcodeFormat.QR_CODE, 300, 300)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        _qrBitmap.value = bmp
    }

    fun toggleQrCode() {
        _showQrCode.value = !_showQrCode.value
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun loadOwnedAvatarCosmetics() {
        viewModelScope.launch {
            try {
                val shop = cosmeticService.loadShop()
                val inventoryDTO = cosmeticService.loadInventory()
                val ownedIds = inventoryDTO.inventory.map { it.cosmeticId }.toSet()
                _ownedAvatarCosmetics.value = shop.filter { cosmetic ->
                    cosmetic.filePath.startsWith("avatars/") &&
                        ownedIds.contains(cosmetic._id)
                }
            } catch (e: Exception) {
                println("loadOwnedAvatarCosmetics error: ${e.message}")
            }
        }
    }
}
