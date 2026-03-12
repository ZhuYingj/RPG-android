package com.mobile_client.services

import androidx.compose.runtime.mutableStateOf
import com.mobile_client.utils.Player
import com.mobile_client.utils.TileConstants

class GameFightService private constructor() {
    companion object {
        val instance: GameFightService by lazy { GameFightService() }
    }

    var activePlayer = mutableStateOf<Player?>(null)
    var player = mutableStateOf<Player?>(null)
    var opposingPlayer = mutableStateOf<Player?>(null)
    var attackDice = mutableStateOf(0)
    var defenseDice = mutableStateOf(0)
    var isFight = mutableStateOf(false)
    var waterCanUsed = mutableStateOf(false)
    var actionString = mutableStateOf("")

    fun initFight(playerVal: Player, opposingPlayerVal: Player, activePlayerVal: Player) {
        player.value = playerVal
        opposingPlayer.value = opposingPlayerVal
        activePlayer.value = activePlayerVal
        attackDice.value = 0
        defenseDice.value = 0
        isFight.value = true
    }

    fun attack() {
        SocketService.instance.socket?.emit(com.mobile_client.utils.FightEvents.ATTACK)
    }

    fun evade() {
        val p = player.value ?: return
        if (p.evasionTry <= 0) return
        SocketService.instance.socket?.emit(com.mobile_client.utils.FightEvents.EVADE)
    }

    fun displayEvade(isSuccess: Boolean) {
        val active = activePlayer.value ?: return
        if (isSuccess) {
            isFight.value = false
            return
        }
        actionString.value = "Échec de la fuite de ${active.username}"
    }

    fun displayAttackResult(damage: Int, attackDiceVal: Int, defenseDiceVal: Int, defender: Player) {
        attackDice.value = attackDiceVal
        defenseDice.value = defenseDiceVal

        val attacker = if (defender.username == player.value?.username) {
            opposingPlayer.value
        } else {
            player.value
        }

        actionString.value = if (damage > 0) {
            "${attacker?.username} a infligé $damage points de dommage à ${defender.username ?: ""}"
        } else {
            "${attacker?.username} a raté son attaque"
        }
    }

    fun endFight(hasWin: Boolean) {
        isFight.value = false
    }
}
