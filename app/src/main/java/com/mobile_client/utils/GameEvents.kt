package com.mobile_client.utils

object GameEvents {
    const val MOVE = "move"
    const val ACTION = "action"
    const val NEXT_TURN = "next"
    const val ABANDON = "abandon"
    const val LAST_PLAYER = "last-player"
    const val IS_MOVE_VALID = "is-move-valid"
    const val START_PLAYER_TURN = "start-player-turn"
    const val TIMER = "set-timer"
    const val TOGGLE_TIMER = "toggle-timer"
    const val TOGGLE_DOOR = "toggle-door"
    const val DEBUG = "set-debug"
    const val TELEPORT = "teleport"
    const val ITEM_PICKUP = "item-pickup"
    const val ITEM_CHOICE = "item-choice"
    const val ITEM_REJECT = "item-reject"
    const val ITEM_DROP = "item-drop"
    const val END_FIGHT = "end-fight"
    const val RESTART_TIMER = "restart-timer"
    const val REJOINING_PLAYER = "rejoining-player"
    const val END_GAME = "end-game"
    const val END_GAME_LEAVE = "end-game-leave"
}

object FightEvents {
    const val ATTACK = "attack"
    const val EVADE = "evade"
    const val ATTACK_RESULT = "attack-result"
    const val EVADE_RESULT = "evade-result"
    const val WIN_FIGHT = "win-fight"
    const val INITIATE_FIGHT = "init-fight"
    const val WATER_CAN_USED = "watercan-used"
}
