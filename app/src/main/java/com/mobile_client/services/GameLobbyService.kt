package com.mobile_client.services

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.mobile_client.utils.BASE_STAT_VALUE
import com.mobile_client.utils.Dices
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.LobbyConstants.BOT_NAMES
import com.mobile_client.utils.LobbyEvents
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import com.mobile_client.utils.PlayerTypes
import com.mobile_client.utils.Stats
import org.json.JSONObject

object GameLobbyService {
    var map = mutableStateOf<GameMap?>(null)
    var isLobbyLocked = mutableStateOf(false)
    var players = mutableStateListOf<Player>()
    var currentPlayer = mutableStateOf<Player?>(null)
    var availableAvatars = mutableStateListOf<PlayerAvatars>()
    var isGameStarted = mutableStateOf(false)
    var isSubmitted = mutableStateOf(false)
    var lobbyCode = mutableStateOf("")
    var isHost = mutableStateOf(false)

    val gson = Gson()

    fun clear() {
        isGameStarted.value = false
        isLobbyLocked.value = false
        players.clear()
        currentPlayer.value = null
        lobbyCode.value = ""
        availableAvatars.clear()
        availableAvatars.addAll(PlayerAvatars.entries.filter { it != PlayerAvatars.None })
        isSubmitted.value = false
        isHost.value = false
        SocketManager.closeLobbyListeners()
    }

    fun joinLobby(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val socket = SocketManager.socket ?: return
        SocketManager.initializeLobbyListeners(this)

        socket.emit(LobbyEvents.JOIN_LOBBY, code)
        socket.once(LobbyEvents.LOBBY_JOINED) { args ->
            Handler(Looper.getMainLooper()).post {
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val success = data.getBoolean("success")
                    if (success) {
                        lobbyCode.value = code
                        onSuccess()
                    } else {
                        val message = data.optString("message", "Impossible de rejoindre")
                        onError(message)
                    }
                }
            }
        }
    }

    fun createLobby(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val socket = SocketManager.socket ?: return
        clear()
        SocketManager.initializeLobbyListeners(this)
        isHost.value = true
        socket.emit(LobbyEvents.CREATE_LOBBY, gson.toJson(map.value))
        socket.once(LobbyEvents.LOBBY_CREATED) { args ->
            Handler(Looper.getMainLooper()).post {
                if (args.isNotEmpty()) {
                    lobbyCode.value = args[0].toString()
                    onSuccess()
                } else {
                    onError("Impossible de créer le lobby")
                }
            }
        }
    }

    fun selectAvatar(previousAvatar: PlayerAvatars, avatar: PlayerAvatars) {
        val socket = SocketManager.socket ?: return
        val data = JSONObject().apply {
            put("avatar", gson.toJson(avatar).trim('"'))
            put("previousAvatar", gson.toJson(previousAvatar).trim('"'))
        }
        socket.emit(LobbyEvents.AVATAR_SELECTED, data)
    }

    fun addPlayer(player: Player, onJoined: () -> Unit) {
        val socket = SocketManager.socket ?: return
        val playerJson = JSONObject(gson.toJson(player))
        socket.emit(LobbyEvents.PLAYER_JOINED, playerJson)
        socket.once(LobbyEvents.JOINING) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val joiningPlayer: Player = gson.fromJson(data.toString(), Player::class.java)
                currentPlayer.value = joiningPlayer
                isSubmitted.value = true
                onJoined()
            }
        }
    }

    fun leaveLobby() {
        val socket = SocketManager.socket ?: return
        socket.emit(LobbyEvents.LEAVE_LOBBY)
        clear()
    }

    fun toggleLobbyLock() {
        val socket = SocketManager.socket ?: return
        socket.emit(LobbyEvents.TOGGLE_LOCK)
    }

    fun startGame() {
        val socket = SocketManager.socket ?: return
        val current = currentPlayer.value ?: return
        if (current.playerType == PlayerTypes.Host) {
            socket.emit(LobbyEvents.START_GAME)
        }
    }

    fun kickPlayer(player: Player) {
        val socket = SocketManager.socket ?: return
        val playerJson = JSONObject(gson.toJson(player))
        socket.emit(LobbyEvents.KICK_PLAYER, playerJson)
        players.remove(player)
    }

    fun handleAvatarSelection(usedAvatars: List<PlayerAvatars>) {
        val allAvatars = PlayerAvatars.entries.filter { it != PlayerAvatars.None }
        availableAvatars.clear()
        availableAvatars.addAll(allAvatars.filter { avatar -> usedAvatars.none { it == avatar } })
    }

    fun createBotPlayer() {
        val statsRandom = Math.random() < 0.5
        val diceRandom = Math.random() < 0.5
        val avatars = availableAvatars.toList()
        if (avatars.isEmpty()) return

        val bot = Player(
            username = findAvailableBotName(),
            playerType = PlayerTypes.BotAggressive,
            defense = if (diceRandom) Dices.D4 else Dices.D6,
            attack = if (diceRandom) Dices.D6 else Dices.D4,
            avatar = avatars[(Math.random() * avatars.size).toInt()],
            isBonusLife = statsRandom,
            stats = Stats(
                life = if (statsRandom) BASE_STAT_VALUE + 2 else BASE_STAT_VALUE,
                speed = if (statsRandom) BASE_STAT_VALUE else BASE_STAT_VALUE + 2,
                attack = BASE_STAT_VALUE,
                defense = BASE_STAT_VALUE
            )
        )
        addPlayer(bot) {}
    }

    private fun findAvailableBotName(): String {
        var name: String
        do {
            name = BOT_NAMES[(Math.random() * BOT_NAMES.size).toInt()]
        } while (players.any { it.username.equals(name, ignoreCase = true) })
        return name
    }
}
