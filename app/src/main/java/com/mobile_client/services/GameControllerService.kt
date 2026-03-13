package com.mobile_client.services

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.GameEvents
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.GameStats
import com.mobile_client.utils.GameTile
import com.mobile_client.utils.ORTHOGONAL_DIRECTIONS
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerStat
import com.mobile_client.utils.Position
import com.mobile_client.utils.TileConstants
import org.json.JSONObject

class GameControllerService private constructor() {
    companion object {
        val instance: GameControllerService by lazy { GameControllerService() }
    }

    val gson = AppGson
    private val socketManager = SocketService.instance
    val movementService = GameMovementService()
    val fightService = GameFightService.instance

    var gameMap = mutableStateOf<GameMap?>(null)
    var gameTiles = mutableStateOf<List<List<GameTile>>>(emptyList())
    var players = mutableStateOf<List<Player>>(emptyList())
    var currentPlayer = mutableStateOf<Player?>(null)
    var player = mutableStateOf<Player?>(null)
    var isAction = mutableStateOf(false)
    var isBetweenTurn = mutableStateOf(false)
    var timerCounter = mutableStateOf(0)
    var isTimerStopped = mutableStateOf(false)
    var isDebug = mutableStateOf(false)
    var isItemChoice = mutableStateOf(false)
    var originalPlayers = mutableStateOf<List<Player>>(emptyList())
    var playerStats = mutableStateOf<List<PlayerStat>>(emptyList())
    var gameStats = mutableStateOf(GameStats())
    var gameWinner = mutableStateOf("")
    var lastPlayer = mutableStateOf(false)

    fun configureListeners() {
        socketManager.initializeGameListeners(this)
    }

    fun getAccessibleTiles(): List<Position> {
        val p = player.value ?: return emptyList()
        val cp = currentPlayer.value ?: return emptyList()
        if (cp.username != p.username) return emptyList()
        if (gameTiles.value.isEmpty()) return emptyList()
        movementService.tiles = gameTiles.value
        movementService.isDebug = isDebug.value
        return movementService.availableMovement(p, players.value)
    }

    fun move(position: Position) {
        val socket = socketManager.socket ?: return
        val p = player.value ?: return
        if (gameWinner.value.isNotEmpty()) return
        if (isSameTile(position, p.position)) return

        val accessible = getAccessibleTiles()
        if (!accessible.any { isSameTile(position, it) }) return

        socket.emit(GameEvents.MOVE, JSONObject(gson.toJson(position)))
    }

    fun nextTurn() {
        socketManager.socket?.emit(GameEvents.NEXT_TURN)
    }

    fun changeObject(itemIndex: Int) {
        socketManager.socket?.emit(GameEvents.ITEM_REJECT, itemIndex)
    }

    fun abandon(onAbandoned: () -> Unit) {
        val socket = socketManager.socket ?: return
        socket.emit(GameEvents.ABANDON)
        socketManager.closeGameListeners()
        GameLobbyService.instance.closeLobbyListeners()// close the listener
        socket.once(GameEvents.ABANDON) {
            Handler(Looper.getMainLooper()).post {
                onAbandoned()
            }
        }
        clear()
    }

    fun leaveEndGame() {
        GameLobbyService.instance.closeLobbyListeners()
        socketManager.closeGameListeners()
        socketManager.socket?.emit(GameEvents.END_GAME_LEAVE)
    }

    fun toggleTimer() {
        isTimerStopped.value = !isTimerStopped.value
    }

    fun action(position: Position) {
        val socket = socketManager.socket ?: return
        if (gameWinner.value.isNotEmpty()) return

        val tiles = gameTiles.value
        var parentTile = position
        for (dir in ORTHOGONAL_DIRECTIONS) {
            val adj = Position(position.x + dir.x, position.y + dir.y)
            if (!isInMap(adj)) continue
            if (tiles[adj.x][adj.y].cost < tiles[parentTile.x][parentTile.y].cost) {
                parentTile = adj
            }
        }

        val accessible = getAccessibleTiles()
        if (!accessible.any { isSameTile(parentTile, it) }) return

        socket.emit(GameEvents.ACTION, JSONObject(gson.toJson(position)))
    }

    fun setDebug() {
        socketManager.socket?.emit(GameEvents.DEBUG)
    }

    fun teleport(position: Position) {
        val socket = socketManager.socket ?: return
        val p = player.value ?: return
        if (isSameTile(position, p.position)) return
        socket.emit(GameEvents.TELEPORT, JSONObject(gson.toJson(position)))
    }

    fun resetTiles() {
        movementService.resetTiles()
    }

    fun getShortestPath(position: Position): List<Position> {
        val p = player.value ?: return emptyList()
        val tiles = gameTiles.value
        val moves = mutableListOf<Position>()
        var currentTile: Position? = position
        while (currentTile != null && !isSameTile(p.position, currentTile)) {
            moves.add(0, currentTile)
            currentTile = tiles[currentTile.x][currentTile.y].parentTile
        }
        return if (currentTile != null) moves else emptyList()
    }

    fun endGame(winner: String) {
        gameWinner.value = winner
    }

    fun setAction() {
        isAction.value = !isAction.value
    }

    fun isOwnSpawn(position: Position): Boolean {
        val p = player.value ?: return false
        return position.x == p.spawnPoint.x && position.y == p.spawnPoint.y
    }

    fun isTeleport(): Boolean {
        val p = player.value ?: return false
        val cp = currentPlayer.value ?: return false
        return isDebug.value && p.username == cp.username
    }

    fun whoHasFlag(): Player? {
        return players.value.find { player ->
            player.items.any { it == TileConstants.Items.Flag }
        }
    }

    private fun isSameTile(first: Position, second: Position): Boolean {
        return first.x == second.x && first.y == second.y
    }

    private fun isInMap(position: Position): Boolean {
        val tiles = gameTiles.value
        return position.x >= 0 && position.y >= 0 && position.x < tiles.size && position.y < tiles[0].size
    }

    fun clear() {
        gameMap.value = null
        gameTiles.value = emptyList()
        players.value = emptyList()
        currentPlayer.value = null
        player.value = null
        isAction.value = false
        isBetweenTurn.value = false
        timerCounter.value = 0
        isTimerStopped.value = false
        isDebug.value = false
        isItemChoice.value = false
        originalPlayers.value = emptyList()
        playerStats.value = emptyList()
        gameStats.value = GameStats()
        gameWinner.value = ""
        lastPlayer.value = false
    }
}
