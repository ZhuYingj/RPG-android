package com.mobile_client.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mobile_client.services.GameLobbyService
import com.mobile_client.services.SocketService
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars

class GameLobbyViewModel : ViewModel() {
    var isLobbyLocked = mutableStateOf(false)
    var players = mutableStateListOf<Player>()
    var currentPlayer = mutableStateOf<Player?>(null)
    var availableAvatars = mutableStateListOf<PlayerAvatars>()
    var isGameStarted = mutableStateOf(false)
    var isSubmitted = mutableStateOf(false)
    var lobbyCode = mutableStateOf("")
    var isHost = mutableStateOf(false)

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
        GameLobbyService.instance.closeLobbyListeners()
    }

    fun joinLobby(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        SocketService.instance.initializeLobbyListeners(this)
        GameLobbyService.instance.joinLobby(
            code = code,
            onSuccess = {
                lobbyCode.value = code
                onSuccess()
            },
            onError = onError
        )
    }

    fun createLobby(map: com.mobile_client.utils.GameMap, onSuccess: () -> Unit, onError: (String) -> Unit) {
        clear()
        SocketService.instance.initializeLobbyListeners(this)
        isHost.value = true
        GameLobbyService.instance.createLobby(
            map = map,
            onSuccess = { code ->
                lobbyCode.value = code
                onSuccess()
            },
            onError = onError
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
}
