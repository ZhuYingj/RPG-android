package com.mobile_client.utils
data class ChatMessage(
    val username: String,
    val message: String,
    val concernedUser: String,
    val timestamp: String,
)

object MessageEvents {
    const val CHAT_MESSAGE = "chatMessage"
    const val JOURNAL_MESSAGE = "journalMessage"
}
