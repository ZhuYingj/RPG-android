package com.mobile_client.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.StrokeJoin
import com.google.gson.annotations.SerializedName

enum class PlayerAvatars {
    @SerializedName("./assets/avatars/avatar1.png")
    Carrot,
    @SerializedName("./assets/avatars/avatar2.png")
    Cabbage,
    @SerializedName("./assets/avatars/avatar3.png")
    Pepper,
    @SerializedName("./assets/avatars/avatar4.png")
    Brocoli,
    @SerializedName("./assets/avatars/avatar5.png")
    Corn,
    @SerializedName("./assets/avatars/avatar6.png")
    Beet,
    @SerializedName("./assets/avatars/avatar7.png")
    Potato,
    @SerializedName("./assets/avatars/avatar8.png")
    Mushroom,
    @SerializedName("./assets/avatars/avatar9.png")
    Onion,
    @SerializedName("./assets/avatars/avatar10.png")
    Cauliflower,
    @SerializedName("./assets/avatars/avatar11.png")
    Celery,
    @SerializedName("./assets/avatars/avatar12.png")
    Tomato,
    @SerializedName("")
    None
}

enum class Dices(val value: Int) {
    D6(6),
    D4(4)
}

enum class PlayerTypes {
    Human,
    Host,
    BotAggressive,
    BotPassive
}

const val BASE_STAT_VALUE = 4

data class Stats(
    val life: Int = BASE_STAT_VALUE,
    val speed: Int = BASE_STAT_VALUE,
    val attack: Int = BASE_STAT_VALUE,
    val defense: Int = BASE_STAT_VALUE
)

data class Position(
    val x: Int = 0,
    val y: Int = 0
)

data class Player(
    val username: String,
    val avatar: PlayerAvatars,
    var playerType: PlayerTypes,
    val attack: Dices,
    val defense: Dices,
    val isBonusLife: Boolean,
    val stats: Stats,
    val position: Position = Position(),
    val items: List<TileConstants.Items> = listOf(TileConstants.Items.None, TileConstants.Items.None),
    val currentLife: Int = stats.life,
    val movement: Int = 0,
    var hasAction: Int = 0,
    val evasionTry: Int = 0,
    val winNumber: Int = 0,
    val spawnPoint: Position = Position(),
    val team: Int = 0,
    val isObserver: Boolean
)

fun Player.isBot(): Boolean {
    return playerType == PlayerTypes.BotAggressive || playerType == PlayerTypes.BotPassive
}

data class RejoiningPlayer(
    val map: GameMap,
    val players: List<Player>,
    val joiningPlayer: Player,
    val activePlayerIndex: Int
)
