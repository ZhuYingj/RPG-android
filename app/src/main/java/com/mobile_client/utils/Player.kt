package com.mobile_client.utils

import com.google.gson.annotations.SerializedName

val premiumAvatarFilePaths = mapOf(
    PlayerAvatars.Pumpkin to "avatars/avatar13.png",
    PlayerAvatars.Asparagus to "avatars/avatar14.png",
    PlayerAvatars.Eggplant to "avatars/avatar15.png",
    PlayerAvatars.Avocado to "avatars/avatar16.png",
)
enum class PlayerAvatars {
    @SerializedName("./assets/avatars/avatar1.png")
    Carrot,
    @SerializedName("./assets/avatars/avatar2.png")
    Cabbage,
    @SerializedName("./assets/avatars/avatar3.png")
    Pepper,
    @SerializedName("./assets/avatars/avatar4.png")
    Broccoli,
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
    @SerializedName("./assets/avatars/avatar13.png")
    Pumpkin,
    @SerializedName("./assets/avatars/avatar14.png")
    Asparagus,
    @SerializedName("./assets/avatars/avatar15.png")
    Eggplant,
    @SerializedName("./assets/avatars/avatar16.png")
    Avocado,
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
    val profilePicture: String? = null,
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
    val isObserver: Boolean = false,
    val equippedItems: List<Cosmetic> = emptyList(),
    val challenge: Challenge = Challenge(ChallengeTypes.PickUpItem),
)

data class Challenge(
    val type: ChallengeTypes,
    val progress: Int = 0,
    val goal: Int = 0,
    val reward: Int = 0
)

enum class ChallengeTypes(val value: Int, val description: String) {
    Water(1, "Déplaces toi 8 fois sur une tuile d'eau"),
    ToggleDoor(2, "Ouvres 3 porte durant la partie"),
    InitiateFight(3, "Commences 2 combat durant la partie"),
    PickUpItem(4, "Ramasses 3 fois un item durant une partie"),
    Evade(5, "Réussis 2 évasion d'un combat durant la partie");
}

fun Player.isBot(): Boolean {
    return playerType == PlayerTypes.BotAggressive || playerType == PlayerTypes.BotPassive
}

data class RejoiningPlayer(
    val map: GameMap,
    val players: List<Player>,
    val joiningPlayer: Player,
    val activePlayerIndex: Int
)
