package com.mobile_client.utils

import java.util.Date

data class Account(
    val userId: String?,
    val username: String,
    val email: String,
    val money: Int,
    val password: String? = null,
    val avatar: String? = null,
    val stats: AccountStats = AccountStats(),
    val actionHistory: List<AccountLog> = emptyList(),
    val matchHistory: List<Match> = emptyList(),
)

data class LoginResponse(
    val account: Account,
    val token: String
)

data class AccountStats(
    val classicGamesPlayed: Int = 0,
    val CTFGamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val averageGameTime: Double = 0.0
)

data class AccountLog(
    val action: UserAction,
    val timestamp: Date
)

data class Match(
    val gameType: GameTypes,
    val gameWon: Boolean,
    val hasLeft: Boolean,
    val startTime: Date
)

enum class UserAction(val label: String) {
    Connection("Connexion"),
    Disconnection("Déconnexion")
}

enum class GameTypes(val label: String) {
    Classic("Classique"),
    CTF("CTF"),
}
