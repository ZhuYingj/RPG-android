package com.mobile_client.services

import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.ChatMessage
import com.mobile_client.utils.LobbyEvents
import com.mobile_client.utils.MessageEvents
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import com.mobile_client.viewModels.GameLobbyViewModel
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SocketService private constructor() {
    companion object {
        val instance: SocketService by lazy { SocketService() }
    }
    var socket: Socket? = null
    private val serverUrl = ENVIRONMENT
    private val gson = AppGson

    fun connect(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit,
    ) {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                auth = mapOf("token" to AccountService.instance.getToken())
            }

            socket = IO.socket(serverUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                onConnected()
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                onDisconnected()
            }

            socket?.connect()

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun initializeChatListeners(onChatMessage: (ChatMessage) -> Unit) {
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
    }

    fun initializeLobbyListeners(lobbyViewModel: GameLobbyViewModel) {
        val socket = socket ?: return

        socket.on(LobbyEvents.PLAYERS) { args ->
            if (args.isNotEmpty()) {
                val jsonArray = args[0] as JSONArray
                val type = object : TypeToken<List<Player>>() {}.type
                val playerList: List<Player> = gson.fromJson(jsonArray.toString(), type)
                lobbyViewModel.players.clear()
                lobbyViewModel.players.addAll(playerList)
            }
        }

        socket.on(LobbyEvents.TOGGLE_LOCK) { args ->
            if (args.isNotEmpty()) {
                lobbyViewModel.isLobbyLocked.value = args[0] as Boolean
            }
        }

        socket.on(LobbyEvents.LOBBY_CLOSED) { args ->
            val message = if (args.isNotEmpty()) args[0] as String else "Lobby fermé"
            println("Lobby closed: $message")
            lobbyViewModel.clear()
        }

        socket.on(LobbyEvents.KICKED) {
            println("Kicked from lobby")
            lobbyViewModel.clear()
        }

        socket.on(LobbyEvents.ERROR) { args ->
            if (args.isNotEmpty()) {
                println("Lobby error: ${args[0]}")
            }
        }

        socket.on(LobbyEvents.START_GAME) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val type = object : TypeToken<List<Player>>() {}.type
                val gamePlayers: List<Player> = gson.fromJson(data.getJSONArray("players").toString(), type)
                lobbyViewModel.players.clear()
                lobbyViewModel.players.addAll(gamePlayers)
                lobbyViewModel.isGameStarted.value = true
            }
        }

        socket.on(LobbyEvents.AVATAR_SELECTED) { args ->
            if (args.isNotEmpty()) {
                val jsonArray = args[0] as JSONArray
                val type = object : TypeToken<List<PlayerAvatars>>() {}.type
                val usedAvatars: List<PlayerAvatars> = gson.fromJson(jsonArray.toString(), type)
                lobbyViewModel.handleAvatarSelection(usedAvatars)
            }
        }
    }

    fun closeLobbyListeners() {
        socket?.off(LobbyEvents.PLAYERS)
        socket?.off(LobbyEvents.TOGGLE_LOCK)
        socket?.off(LobbyEvents.LOBBY_CLOSED)
        socket?.off(LobbyEvents.KICKED)
        socket?.off(LobbyEvents.ERROR)
        socket?.off(LobbyEvents.START_GAME)
        socket?.off(LobbyEvents.AVATAR_SELECTED)
    }

    fun sendMessage(message: String, lobby: String = "") {
        if (lobby == "")
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
