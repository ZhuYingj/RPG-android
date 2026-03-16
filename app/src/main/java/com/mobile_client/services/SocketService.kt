package com.mobile_client.services

import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.ActionReturnObject
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.AttackResultObject
import com.mobile_client.utils.ChatMessage
import com.mobile_client.utils.EndGameObject
import com.mobile_client.utils.EvadeReturnObject
import com.mobile_client.utils.FightEvents
import com.mobile_client.utils.GameEvents
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.InitFightObject
import com.mobile_client.utils.ItemPickUpObject
import com.mobile_client.utils.LobbyEvents
import com.mobile_client.utils.MessageEvents
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import com.mobile_client.utils.TileConstants
import com.mobile_client.utils.WinFightObject
import com.mobile_client.utils.toGameTiles
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
                auth = mapOf("token" to AccountService.instance.token)
            }
            socket = IO.socket(serverUrl, options)
            socket?.on(Socket.EVENT_CONNECT) { onConnected() }
            socket?.on(Socket.EVENT_DISCONNECT) { onDisconnected() }
            socket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    // ===================== CHAT LISTENERS =====================

    fun initializeChatListeners(onChatMessage: (ChatMessage) -> Unit) {
        socket?.on(MessageEvents.CHAT_MESSAGE) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val message = ChatMessage(
                    username = data.getString("username"),
                    message = data.getString("message"),
                    avatar = data.getString("avatar"),
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
                    avatar = data.getString("avatar"),
                    timestamp = convertUTCToLocalTime(data.getString("time")),
                )
                onChatMessage(message)
            }
        }
    }

    // ===================== LOBBY LISTENERS =====================

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
            lobbyViewModel.showMessage(message)
            lobbyViewModel.clear()
        }

        socket.on(LobbyEvents.KICKED) {
            lobbyViewModel.showMessage("Vous avez été enlevé de la partie")
            lobbyViewModel.clear()
        }

        socket.on(LobbyEvents.ERROR) { args ->
            if (args.isNotEmpty()) {
                lobbyViewModel.showMessage(args[0].toString())
            }
        }

        socket.on(LobbyEvents.START_GAME) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val type = object : TypeToken<List<Player>>() {}.type
                val gamePlayers: List<Player> = gson.fromJson(data.getJSONArray("players").toString(), type)
                val map: GameMap = gson.fromJson(data.getJSONObject("map").toString(), GameMap::class.java)

                gamePlayers.forEach { println("START_GAME player: ${it.username} movement=${it.movement}") }
                this.initializeGameListeners(GameControllerService.instance)

                lobbyViewModel.players.clear()
                lobbyViewModel.players.addAll(gamePlayers)
                lobbyViewModel.gameMap.value = map

                // Update currentPlayer with server version
                val current = gamePlayers.find { it.username == lobbyViewModel.currentPlayer.value?.username }
                if (current != null) lobbyViewModel.currentPlayer.value = current

                //initializeGameListeners()
                lobbyViewModel.isGameStarted.value = true
            }
        }

        socket.on(LobbyEvents.AVATAR_SELECTED) { args ->
            if (args.isNotEmpty() && args[0] != null) {
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

    // ===================== GAME LISTENERS =====================

    fun initializeGameListeners(controller: GameControllerService) {
        val socket = socket ?: return

        socket.on(GameEvents.ABANDON) { args ->
            if (args.isNotEmpty()) {
                val type = object : TypeToken<List<Player>>() {}.type
                val players: List<Player> = gson.fromJson((args[0] as JSONArray).toString(), type)
                controller.players.value = players
            }
        }

        socket.on(GameEvents.TOGGLE_DOOR) { args ->
            if (args.isNotEmpty()) {
                val map: GameMap = gson.fromJson((args[0] as JSONObject).toString(), GameMap::class.java)
                controller.gameMap.value = map
                controller.gameTiles.value = map.tiles.toGameTiles()
            }
        }

        socket.on(GameEvents.MOVE) { args ->
            if (args.isNotEmpty()) {
                val player: Player = gson.fromJson((args[0] as JSONObject).toString(), Player::class.java)
                handleMove(controller, player)
            }
        }

        socket.on(GameEvents.IS_MOVE_VALID) { args ->
            if (args.isNotEmpty()) {
                val isValid = args[0] as Boolean
                if (!isValid)
                    controller.serverMessage.value = "Veuillez choisir une autre tuile, vous avez choisi une tuile invalide"
            }
        }

        socket.on(GameEvents.ACTION) { args ->
            //TODO: has actionremaining, and message, pourrait faire un snackbar pour le message
            if (args.isNotEmpty()) {
                val res = gson.fromJson(args[0].toString(), ActionReturnObject::class.java)
                if (res.isValid) {
                    controller.isAction.value = false
                }
                if (!res.message.isNullOrEmpty()) {
                    controller.serverMessage.value = res.message
                }
            }
        }

        socket.on(GameEvents.DEBUG) { args ->
            println("DEBUG fired")
            if (args.isNotEmpty()) {
                controller.isDebug.value = args[0] as Boolean
            }
        }

        initializeTurnListeners(controller)
        initializeItemListeners(controller)
        initializeGameFightListeners(controller)
    }

    private fun initializeTurnListeners(controller: GameControllerService) {
        val socket = socket ?: return

        socket.on(GameEvents.NEXT_TURN) { args ->
            if (args.isNotEmpty()) {
                val player: Player = gson.fromJson((args[0] as JSONObject).toString(), Player::class.java)
                println("Next turn: ${player.username}")
                handleNextTurn(controller, player)
            }
        }

        socket.on(GameEvents.START_PLAYER_TURN) {
            println("START_PLAYER_TURN fired")
            controller.isBetweenTurn.value = false
            val p = controller.player.value
            if (p != null) {
                controller.player.value = p.copy(movement = p.stats.speed)
            }
        }

        socket.on(GameEvents.TIMER) { args ->
            if (args.isNotEmpty()) {
                controller.timerCounter.value = (args[0] as Number).toInt()
            }
        }

        socket.on(GameEvents.TOGGLE_TIMER) {
            controller.toggleTimer()
        }
    }

    private fun initializeItemListeners(controller: GameControllerService) {
        val socket = socket ?: return

        socket.on(GameEvents.ITEM_PICKUP) { args ->
            if (args.isNotEmpty()) {
                val data: ItemPickUpObject = gson.fromJson((args[0] as JSONObject).toString(), ItemPickUpObject::class.java)
                handlePickUpItem(controller, data)
            }
        }

        socket.on(GameEvents.ITEM_CHOICE) {
            controller.isItemChoice.value = true
        }

        socket.on(GameEvents.ITEM_DROP) { args ->
            if (args.isNotEmpty()) {
                val map: GameMap = gson.fromJson((args[0] as JSONObject).toString(), GameMap::class.java)
                controller.gameMap.value = map
                controller.gameTiles.value = map.tiles.toGameTiles()
            }
        }
    }

    private fun initializeGameFightListeners(controller: GameControllerService) {
        val socket = socket ?: return
        val fightService = controller.fightService

        socket.on(FightEvents.INITIATE_FIGHT) { args ->
            if (args.isNotEmpty()) {
                val data: InitFightObject = gson.fromJson((args[0] as JSONObject).toString(), InitFightObject::class.java)
                initializeFightListeners(controller)
                //TODO: print socket and socket id
                println("socket: $socket")
                val p = controller.player.value ?: return@on
                if (p.username == data.players[0].username) {
                    fightService.initFight(data.players[0], data.players[1], data.playerTurn)
                } else {
                    fightService.initFight(data.players[1], data.players[0], data.playerTurn)
                }
            }
        }

        socket.on(GameEvents.END_FIGHT) { args ->
            if (args.isNotEmpty()) {
                val type = object : TypeToken<List<Player>>() {}.type
                val players: List<Player> = gson.fromJson((args[0] as JSONArray).toString(), type)
                handleEndFight(controller, players)
            }
        }

        socket.on(GameEvents.LAST_PLAYER) {
            println("LAST_PLAYER fired")
            closeGameListeners()
            closeLobbyListeners()
            controller.lastPlayer.value = true
        }

        socket.on(GameEvents.END_GAME) { args ->
            println("END_GAME fired")
            if (args.isNotEmpty()) {
                val data: EndGameObject = gson.fromJson((args[0] as JSONObject).toString(), EndGameObject::class.java)
                controller.gameStats.value = data.gameStats
                controller.playerStats.value = data.playerStats
                val map = controller.gameMap.value
                if (map != null && map.isCaptureTheFlag) {
                    val winner = controller.players.value.find { it.username == data.winnerName }
                    if (winner != null) {
                        val team = if (winner.team == 1) "rouge" else "bleu"
                        controller.endGame("équipe $team")
                    }
                } else {
                    controller.endGame(data.winnerName)
                }
            }
        }
    }

    private fun initializeFightListeners(controller: GameControllerService) {
        val socket = socket ?: return
        val fightService = controller.fightService

        socket.on(FightEvents.ATTACK_RESULT) { args ->
            if (args.isNotEmpty()) {
                val data: AttackResultObject = gson.fromJson((args[0] as JSONObject).toString(), AttackResultObject::class.java)
                fightService.activePlayer.value = data.playerTurn
                if (data.playerTurn.username == fightService.player.value?.username) {
                    fightService.player.value = data.playerTurn
                } else {
                    fightService.opposingPlayer.value = data.playerTurn
                }
                fightService.displayAttackResult(data.damage, data.attackDice, data.defenseDice, data.playerTurn)
            }
        }

        socket.on(FightEvents.WIN_FIGHT) { args ->
            if (args.isNotEmpty()) {
                val data: WinFightObject = gson.fromJson((args[0] as JSONObject).toString(), WinFightObject::class.java)
                val p = controller.player.value ?: return@on
                fightService.endFight(p.username == data.players[0].username)
            }
        }

        socket.on(FightEvents.EVADE_RESULT) { args ->
            if (args.isNotEmpty()) {
                val data: EvadeReturnObject = gson.fromJson((args[0] as JSONObject).toString(), EvadeReturnObject::class.java)
                fightService.displayEvade(data.isSuccess)
                fightService.activePlayer.value = data.playerTurn
                if (data.playerTurn.username == fightService.player.value?.username) {
                    fightService.player.value = data.playerTurn
                    fightService.opposingPlayer.value = data.evadingPlayer
                } else {
                    fightService.player.value = data.evadingPlayer
                    fightService.opposingPlayer.value = data.playerTurn
                }
            }
        }

        socket.on(FightEvents.WATER_CAN_USED) {
            fightService.waterCanUsed.value = true
        }
    }

    // ===================== GAME HANDLER HELPERS =====================

    private fun handleMove(controller: GameControllerService, player: Player) {
        val x = player.position.x
        val y = player.position.y

        // Clear item from tile player moved to
        val tiles = controller.gameTiles.value
        if (tiles.isNotEmpty() && x < tiles.size && y < tiles[0].size) {
            val tileItem = tiles[x][y].item
            if (tileItem != null && tileItem != TileConstants.Items.None && tileItem != TileConstants.Items.Spawn) {
                controller.gameTiles.value = tiles.mapIndexed { rowIdx, row ->
                    if (rowIdx == x) {
                        row.mapIndexed { colIdx, tile ->
                            if (colIdx == y) tile.copy(item = TileConstants.Items.None)
                            else tile
                        }
                    } else row
                }
            }
        }

        val players = controller.players.value.toMutableList()
        val index = players.indexOfFirst { it.username == player.username }
        if (index >= 0) {
            players[index] = player
            controller.players.value = players
        }
        if (player.username == controller.player.value?.username) {
            controller.player.value = player
        }
    }

    private fun handleNextTurn(controller: GameControllerService, player: Player) {
        controller.currentPlayer.value = player
        if (player.username == controller.player.value?.username) {
            controller.player.value = player
        }
        val players = controller.players.value.toMutableList()
        val index = players.indexOfFirst { it.username == player.username }
        if (index >= 0) {
            players[index] = player
            controller.players.value = players
        }
        controller.isBetweenTurn.value = true
        controller.resetTiles()
    }

    private fun handlePickUpItem(controller: GameControllerService, data: ItemPickUpObject) {
        val x = data.player.position.x
        val y = data.player.position.y

        // Always clear the tile — data.item is what was picked up, not what remains
        controller.gameTiles.value = controller.gameTiles.value.mapIndexed { rowIdx, row ->
            if (rowIdx == x) {
                row.mapIndexed { colIdx, tile ->
                    if (colIdx == y) tile.copy(item = data.item) //tile.copy(item = TileConstants.Items.None)
                    else tile
                }
            } else row
        }

        val players = controller.players.value.toMutableList()
        val index = players.indexOfFirst { it.username == data.player.username }
        if (index >= 0) {
            players[index] = data.player
            controller.players.value = players
        }
        if (data.player.username == controller.player.value?.username) {
            controller.player.value = data.player
        }
    }

    private fun handleEndFight(controller: GameControllerService, players: List<Player>) {
        controller.fightService.waterCanUsed.value = false
        val currentPlayers = controller.players.value.toMutableList()
        val originalPlayers = controller.originalPlayers.value.toMutableList()
        for (player in players) {
            val index = currentPlayers.indexOfFirst { it.username == player.username }
            if (index >= 0) currentPlayers[index] = player
            val origIndex = originalPlayers.indexOfFirst { it.username == player.username }
            if (origIndex >= 0) originalPlayers[origIndex] = player
            if (player.username == controller.player.value?.username) {
                controller.player.value = player
            }
            if (player.username == controller.currentPlayer.value?.username) {
                controller.currentPlayer.value = player
            }
        }
        controller.players.value = currentPlayers
        controller.originalPlayers.value = originalPlayers
    }

    // ===================== UTILITY =====================

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

    fun closeGameListeners() {
        socket?.off(GameEvents.ABANDON)
        socket?.off(GameEvents.LAST_PLAYER)
        socket?.off(GameEvents.TOGGLE_DOOR)
        socket?.off(GameEvents.MOVE)
        socket?.off(GameEvents.IS_MOVE_VALID)
        socket?.off(GameEvents.ACTION)
        socket?.off(GameEvents.DEBUG)
        socket?.off(GameEvents.NEXT_TURN)
        socket?.off(GameEvents.START_PLAYER_TURN)
        socket?.off(GameEvents.TIMER)
        socket?.off(GameEvents.TOGGLE_TIMER)
        socket?.off(GameEvents.ITEM_PICKUP)
        socket?.off(GameEvents.ITEM_CHOICE)
        socket?.off(GameEvents.ITEM_DROP)
        socket?.off(FightEvents.INITIATE_FIGHT)
        socket?.off(GameEvents.END_FIGHT)
        socket?.off(GameEvents.END_GAME)
        socket?.off(FightEvents.ATTACK_RESULT)
        socket?.off(FightEvents.WIN_FIGHT)
        socket?.off(FightEvents.EVADE_RESULT)
        socket?.off(FightEvents.WATER_CAN_USED)
    }

    fun convertUTCToLocalTime(utcString: String): String {
        val instant: Instant = Instant.parse(utcString)
        val localTime: LocalTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return localTime.format(formatter)
    }
}
