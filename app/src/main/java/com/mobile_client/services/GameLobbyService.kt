package com.mobile_client.services

import android.os.Handler
import android.os.Looper
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
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.GameEvents

class GameLobbyService private constructor() {
    companion object {
        val instance: GameLobbyService by lazy { GameLobbyService() }
    }
    val gson = AppGson
    private val socketManager = SocketService.instance

    fun joinLobby(code: String, onSuccess: (Int, String?) -> Unit, onError: (String) -> Unit) {
        val socket = socketManager.socket ?: return

        socket.emit(LobbyEvents.JOIN_LOBBY, code)
        socket.once(LobbyEvents.LOBBY_JOINED) { args ->
            Handler(Looper.getMainLooper()).post {
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val success = data.getBoolean("success")
                    if (success) {
                        val fee = data.optInt("entryFee", 0)
                        val qr = data.optString("qrDataUrl", "")
                        onSuccess(fee, qr)
                    } else {
                        val message = data.optString("message", "Impossible de rejoindre")
                        onError(message)
                    }
                }
            }
        }
    }

    fun createLobby(map: GameMap, fee: Int = 0, onSuccess: (String, String?) -> Unit, onError: (String) -> Unit) {
        val socket = socketManager.socket ?: return
        socket.off(LobbyEvents.LOBBY_CREATED)

        val data = JSONObject().apply {
            put("map", JSONObject(gson.toJson(map)))
            put("entryFee", fee)
        }

        socket.emit(LobbyEvents.CREATE_LOBBY, data)
        socket.once(LobbyEvents.LOBBY_CREATED) { args ->
            Handler(Looper.getMainLooper()).post {
                if (args.isNotEmpty()) {
                    val response = args[0] as JSONObject
                    val success = response.getBoolean("success")
                    if (success) {
                        val qr = response.optString("qrDataUrl", "")
                        onSuccess(response.getString("lobbyCode"), qr)
                    } else {
                        onError(response.optString("message", "Impossible de créer le lobby"))
                    }
                } else {
                    onError("Impossible de créer le lobby")
                }
            }
        }
    }

    fun selectAvatar(previousAvatar: PlayerAvatars, avatar: PlayerAvatars) {
        val socket = socketManager.socket ?: return
        val data = JSONObject().apply {
            put("avatar", gson.toJson(avatar).trim('"'))
            put("previousAvatar", gson.toJson(previousAvatar).trim('"'))
        }
        socket.emit(LobbyEvents.AVATAR_SELECTED, data)
    }

    fun addPlayer(player: Player, onJoined: (Player) -> Unit) {
        val socket = socketManager.socket ?: return
        val playerJson = JSONObject(gson.toJson(player))
        socket.emit(LobbyEvents.PLAYER_JOINED, playerJson)
        socket.once(LobbyEvents.JOINING) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val joiningPlayer: Player = gson.fromJson(data.toString(), Player::class.java)
                onJoined(joiningPlayer)
            }
        }
    }

    fun leaveLobby() {
        val socket = socketManager.socket ?: return
        socket.emit(LobbyEvents.LEAVE_LOBBY)
    }

    fun rejoiningPlayer(player: Player) {
        val socket = socketManager.socket ?: return
        val playerJson = JSONObject(gson.toJson(player))
        socket.emit(GameEvents.REJOINING_PLAYER, playerJson)
    }

    fun toggleFriendOnly() {
        val socket = socketManager.socket ?: return
        socket.emit(LobbyEvents.FRIEND_ONLY)
    }

    fun toggleDropIn() {
        val socket = socketManager.socket ?: return
        socket.emit(LobbyEvents.TOGGLE_DROP_IN)
    }

    fun toggleLobbyLock() {
        val socket = socketManager.socket ?: return
        socket.emit(LobbyEvents.TOGGLE_LOCK)
    }

    fun startGame(playerType: PlayerTypes) {
        val socket = socketManager.socket ?: return
        if (playerType == PlayerTypes.Host) {
            socket.emit(LobbyEvents.START_GAME)
        }
    }

    fun kickPlayer(player: Player) {
        val socket = socketManager.socket ?: return
        val playerJson = JSONObject(gson.toJson(player))
        socket.emit(LobbyEvents.KICK_PLAYER, playerJson)
    }

    fun closeLobbyListeners() {
        socketManager.closeLobbyListeners()
    }

    fun updateBotType(username: String, botType: PlayerTypes) {
        val socket = socketManager.socket ?: return
        val data = JSONObject().apply {
            put("username", username)
            put("type", botType.ordinal)
        }
        socket.emit(LobbyEvents.TOGGLE_BOT, data)
    }

    fun createBotPlayer(
        availableAvatars: List<PlayerAvatars>,
        existingPlayerNames: List<String>,
        onJoined: (Player) -> Unit
    ) {
        val statsRandom = Math.random() < 0.5
        val diceRandom = Math.random() < 0.5
        if (availableAvatars.isEmpty()) return

        val bot = Player(
            username = findAvailableBotName(existingPlayerNames),
            playerType = PlayerTypes.BotAggressive,
            defense = if (diceRandom) Dices.D4 else Dices.D6,
            attack = if (diceRandom) Dices.D6 else Dices.D4,
            avatar = availableAvatars[(Math.random() * availableAvatars.size).toInt()],
            isBonusLife = statsRandom,
            stats = Stats(
                life = if (statsRandom) BASE_STAT_VALUE + 2 else BASE_STAT_VALUE,
                speed = if (statsRandom) BASE_STAT_VALUE else BASE_STAT_VALUE + 2,
                attack = BASE_STAT_VALUE,
                defense = BASE_STAT_VALUE
            )
        )
        addPlayer(bot, onJoined)
    }

    private fun findAvailableBotName(existingPlayerNames: List<String>): String {
        var name: String
        do {
            name = BOT_NAMES[(Math.random() * BOT_NAMES.size).toInt()]
        } while (existingPlayerNames.any { it.equals(name, ignoreCase = true) })
        return name
    }
}
