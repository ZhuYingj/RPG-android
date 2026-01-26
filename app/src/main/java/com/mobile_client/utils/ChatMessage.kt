package com.mobile_client.utils
data class ChatMessage(
    val username: String,
    val message: String,
    val concernedUser: String,
    val timestamp: String,
)

object MessageEvents {
    const val CHAT_MESSAGE = "chat-message"
    const val JOURNAL_MESSAGE = "journal-message"
    const val GLOBAL_CHAT_MESSAGE = "global-chat-message"
}
