package com.mobile_client.utils

data class ActionReturnObject(
    val isValid: Boolean,
    val message: String? = null,
    val actionRemaining: Int? = null
)

data class AttackResultObject(
    val damage: Int,
    val heal: Int? = null,
    val attackDice: Int,
    val defenseDice: Int,
    val playerTurn: Player,
    val attacker: Player,
)

data class EvadeReturnObject(
    val isSuccess: Boolean,
    val evadingPlayer: Player,
    val playerTurn: Player
)

data class WinFightObject(
    val damage: Int,
    val players: List<Player>
)

data class InitFightObject(
    val players: List<Player>,
    val playerTurn: Player
)

data class ItemPickUpObject(
    val player: Player,
    val item: TileConstants.Items
)

data class MoneyResult(
    val baseMoney: Int = 0,
    val challengeReward: Int = 0,
    val entryFeeGain: Int = 0,
)

data class EndGameObject(
    val playerStats: List<PlayerStat>,
    val gameStats: GameStats,
    val winnerName: String,
    val moneyResults: Map<String, MoneyResult> = emptyMap()
)
data class GameStats(
    val gameTime: Long = 0,
    val doorActivated: List<Position> = emptyList(),
    val tilesVisited: List<Position> = emptyList(),
    val flagTaken: List<String> = emptyList(),
    val doorNumber: Double = 0.0,
    val tilesVisitedNumber: Double = 0.0
)

data class PlayerStat(
    val playerName: String = "",
    val fightNumber: Int = 0,
    val evasionNumber: Int = 0,
    val losesNumber: Int = 0,
    val winNumber: Int = 0,
    val lifeLost: Int = 0,
    val damageDealt: Int = 0,
    val itemPicked: List<TileConstants.Items> = emptyList(),
    val visitedTile: List<Position> = emptyList(),
    val visitedTileNumber: Double = 0.0
)

data class StartGameObject(
    val players: List<Player>,
    val map: GameMap
)

val ORTHOGONAL_DIRECTIONS = listOf(
    Position(0, -1),
    Position(0, 1),
    Position(-1, 0),
    Position(1, 0)
)

val TypeToCost = mapOf(
    TileConstants.Types.Ice to 0,
    TileConstants.Types.Normal to 1,
    TileConstants.Types.Water to 2,
    TileConstants.Types.Wall to 99,
    TileConstants.Types.OpenDoor to 1,
    TileConstants.Types.ClosedDoor to 99,
    TileConstants.Types.OpenAutoDoor to 1,
    TileConstants.Types.ClosedAutoDoor to 99,
    TileConstants.Types.Bush to 1,
    TileConstants.Types.Flower to 1,
    TileConstants.Types.OpenedBush to 1
)
