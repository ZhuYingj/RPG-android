package com.mobile_client.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.utils.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages : StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private val _connectionStatus = MutableStateFlow("Connecté")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

//    private val _unreadCount = MutableStateFlow(0)
//    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

//    init {
//        enableListeners()
//    }
    fun enableListeners() {
        viewModelScope.launch {
            SocketManager.connect(
                onConnected = {
                    _connectionStatus.value = "Connecté"
                },
                onDisconnected = {
                    _connectionStatus.value = "Déconnecté"
                },
                onChatMessage = { message ->
                    handleChatMessage(message)
                }
            )
        }
    }

    private fun handleChatMessage(message: ChatMessage) {
        _messages.value += message
//        _messages.value = _message.message
//        _unreadCount.value++
    }

    private fun getCurrentTime(): String {
        val now = java.util.Calendar.getInstance()
        val hours = now.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minutes = now.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')
        val seconds = now.get(java.util.Calendar.SECOND).toString().padStart(2, '0')
        return "$hours:$minutes:$seconds"
    }

    fun clear() {
        _messages.value = emptyList()
        _connectionStatus.value = "Disconnected"
    }
    fun sendMessage(messageContent: String) {
        viewModelScope.launch {
            val newMessage = ChatMessage(
                username = AccountRepository.getUsername(),
                message = messageContent,
                concernedUser = "",
                timestamp = getCurrentTime(),
            )
            SocketManager.sendMessage(newMessage)
        }
    }


}




