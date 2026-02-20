package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.SocketService
import com.mobile_client.utils.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages : StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private val _connectionStatus = MutableStateFlow("Connecté")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

//    private val _unreadCount = MutableStateFlow(0)
//    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

//    init {
//        enableListeners()
//    }

    override fun onCleared() {
        super.onCleared()
        _messages.value = emptyList()
        _connectionStatus.value = "Disconnected"
    }
    fun enableListeners() {
        viewModelScope.launch {
            SocketService.instance.connect(
                onConnected = {
                    _connectionStatus.value = "Connecté"
                },
                onDisconnected = {
                    _connectionStatus.value = "Déconnecté"
                },
            )
        }
    }

    fun handleChatMessage(message: ChatMessage) {
        _messages.value += message
    }

    fun clearList() {
        _messages.value = emptyList()
    }
    fun sendMessage(messageContent: String) {
        viewModelScope.launch { SocketService.instance.sendMessage(messageContent) }
    }

}
