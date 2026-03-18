package com.mobile_client.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.GameLobbyService
import com.mobile_client.services.SocketService
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.LobbyEvents
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.mobile_client.utils.PlayerTypes

class GameLobbyViewModel : ViewModel() {
    var isLobbyLocked = mutableStateOf(false)
    var players = mutableStateListOf<Player>()
    var currentPlayer = mutableStateOf<Player?>(null)
    var availableAvatars = mutableStateListOf<PlayerAvatars>()
    var isGameStarted = mutableStateOf(false)
    var isSubmitted = mutableStateOf(false)
    var lobbyCode = mutableStateOf("")
    var isHost = mutableStateOf(false)
    var gameMap = mutableStateOf<GameMap?>(null)

    var entryFee = mutableStateOf(0)
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    var qrCodeDataUrl = mutableStateOf<String?>(null)
    var showQrCode = mutableStateOf(false)

    init {
        availableAvatars.addAll(PlayerAvatars.entries.filter { it != PlayerAvatars.None })
    }

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
        gameMap.value = null
        entryFee.value = 0
        qrCodeDataUrl.value = null
        showQrCode.value = false
        GameLobbyService.instance.closeLobbyListeners()
    }

    fun toggleBotType(player: Player) {
        val index = players.indexOf(player)
        if (index == -1) return

        val newType = if (player.playerType == PlayerTypes.BotAggressive)
            PlayerTypes.BotPassive else PlayerTypes.BotAggressive

        val updatedPlayer = player.copy(playerType = newType)
        players[index] = updatedPlayer

        GameLobbyService.instance.updateBotType(player.username, newType)
    }
    fun createLobby(map: GameMap, fee: Int = 0, onSuccess: () -> Unit, onError: (String) -> Unit) {
        GameLobbyService.instance.leaveLobby()
        clear()
        SocketService.instance.initializeLobbyListeners(this)
        isHost.value = true
        entryFee.value = fee
        GameLobbyService.instance.createLobby(
            map = map,
            fee = fee,
            onSuccess = { code, qr ->
                lobbyCode.value = code
                qrCodeDataUrl.value = qr
                onSuccess()
            },
            onError = onError
        )
    }

    fun joinLobbyWithBlockCheck(
        code: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        SocketService.instance.initializeLobbyListeners(this)
        GameLobbyService.instance.joinLobby(
            code = code,
            onSuccess = { fee, qr ->
                lobbyCode.value = code
                entryFee.value = fee
                qrCodeDataUrl.value = qr
                onSuccess()
            },
            onError = { message ->
                SocketService.instance.closeLobbyListeners()
                onError(message)
            }
        )
    }
    fun selectAvatar(previousAvatar: PlayerAvatars, avatar: PlayerAvatars) {
        GameLobbyService.instance.selectAvatar(previousAvatar, avatar)
    }

    fun addPlayer(player: Player, onJoined: () -> Unit) {
        GameLobbyService.instance.addPlayer(player) { joiningPlayer ->
            currentPlayer.value = joiningPlayer
            isSubmitted.value = true
            onJoined()
        }
    }

    fun leaveLobby() {
        GameLobbyService.instance.leaveLobby()
        clear()
    }

    fun toggleLobbyLock() {
        GameLobbyService.instance.toggleLobbyLock()
    }

    fun startGame() {
        val current = currentPlayer.value ?: return
        GameLobbyService.instance.startGame(current.playerType)
    }

    fun kickPlayer(player: Player) {
        GameLobbyService.instance.kickPlayer(player)
        players.remove(player)
    }

    fun handleAvatarSelection(usedAvatars: List<PlayerAvatars>) {
        val allAvatars = PlayerAvatars.entries.filter { it != PlayerAvatars.None }
        availableAvatars.clear()
        availableAvatars.addAll(allAvatars.filter { avatar -> usedAvatars.none { it == avatar } })
    }

    fun createBotPlayer() {
        GameLobbyService.instance.createBotPlayer(
            availableAvatars = availableAvatars.toList(),
            existingPlayerNames = players.map { it.username }
        ) {}
    }

    fun showMessage(message: String) {
        viewModelScope.launch { _errorMessage.emit(message) }
    }

    fun toggleQrCode() {
        SocketService.instance.socket?.emit(LobbyEvents.TOGGLE_QR_CODE)
    }
}
