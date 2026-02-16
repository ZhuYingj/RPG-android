package com.mobile_client.services

import android.icu.util.TimeZone
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.ChatMessage
import com.mobile_client.utils.MessageEvents
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SocketManager {
    private var socket: Socket? = null
    private val serverUrl = ENVIRONMENT

    fun connect(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit,
        onChatMessage: (ChatMessage) -> Unit,
    ) {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
//                reconnection = true
//                reconnectionDelay = 1000
//                reconnectionAttempts = 5
                auth = mapOf("token" to AccountRepository.getToken())

            }

            socket = IO.socket(serverUrl, options)

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
                        concernedUser = data.optString("avatar", ""),
                        timestamp = convertUTCToLocalTime(data.getString("time")),
                    )
                    onChatMessage(message)
                }
            }

            socket?.on(MessageEvents.GLOBAL_CHAT_MESSAGE) { args ->

                if(args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val message = ChatMessage(
                        username = data.getString("username"), //we dont have enum for now, lets have an empty username for now
                        message = data.getString("message"),
                        concernedUser = data.optString("avatar", ""),
                        timestamp = convertUTCToLocalTime(data.getString("time")),
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

    fun sendMessage(message: String, lobby: String="") {
        if(lobby == "")
            socket?.emit(MessageEvents.GLOBAL_CHAT_MESSAGE, message)
        else
            socket?.emit(MessageEvents.CHAT_MESSAGE, message)
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
    }

    fun convertUTCToLocalTime(utcString: String): String {
        val instant: Instant = Instant.parse(utcString)
        val localTime: LocalTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return localTime.format(formatter)
    }
}
