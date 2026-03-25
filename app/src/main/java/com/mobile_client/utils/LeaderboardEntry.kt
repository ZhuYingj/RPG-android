package com.mobile_client.utils

data class LeaderboardEntry(
    val username: String,
    val avatar: String?,
    val money: Int,
    val stats: LeaderboardStats?,
)

data class LeaderboardStats(
    val gamesWon: Int,
    val classicGamesPlayed: Int,
    val CTFGamesPlayed: Int,
    val averageGameTime: Double,
)

enum class LeaderboardSortType(val label: String) {
    WINS("Victoires"),
    MONEY("Argent"),
    PLAYTIME("Temps de jeu"),
}

enum class LeaderboardFilterType(val label: String) {
    GLOBAL("Global"),
    FRIENDS("Amis"),
}
