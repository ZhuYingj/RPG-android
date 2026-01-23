package com.mobile_client.services

import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.ChatMessage
import com.mobile_client.utils.MessageEvents
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager {
    private var socket: Socket? = null
    private val SERVER_URL = ENVIRONMENT

    fun connect(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit,
        onChatMessage: (ChatMessage) -> Unit,
    ) {
        try {
            val options = IO.Options().apply {
                reconnection = true
                reconnectionDelay = 1000
                reconnectionAttempts = 5
            }

            socket = IO.socket(SERVER_URL, options)

            socket?.on(Socket.EVENT_CONNECT) {
                onConnected()
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                onDisconnected()
            }

            // Listen for chat messages
            socket?.on(MessageEvents.CHAT_MESSAGE) { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val message = ChatMessage(
                        username = data.getString("username"),
                        message = data.getString("message"),
                        concernedUser = data.optString("concernedUser", ""),
                        timestamp = data.getString("time"),
                    )
                    onChatMessage(message)
                }
            }

//            // Listen for journal messages
//            socket?.on(MessageEvents.JOURNAL_MESSAGE) { args ->
//                if (args.isNotEmpty()) {
//                    val data = args[0] as JSONObject
//                    val message = ChatMessage(
//                        username = data.getString("username"),
//                        message = data.getString("message"),
//                        concernedUser = data.optString("concernedUser", ""),
//                        timestamp = data.getString("time"),
//                    )
//                    onJournalMessage(message)
//                }
//            }

            socket?.connect()

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun sendMessage(message: ChatMessage) {
        val messageData = JSONObject().apply {
            put("username", message.username)
            put("message", message.message)
            put("concernedUser", message.concernedUser)
            put("time", message.timestamp)
        }
        socket?.emit(MessageEvents.CHAT_MESSAGE, messageData)
    }


    fun disconnect() {
        socket?.disconnect()
        socket?.off()
    }

}
