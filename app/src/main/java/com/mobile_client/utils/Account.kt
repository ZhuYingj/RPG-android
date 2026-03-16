package com.mobile_client.utils

data class Account(
    val userId: String?,
    val username: String,
    val email: String,
    val money: Int,
    val password: String? = null,
    val avatar: String? = null,
    val stats: AccountStats = AccountStats()
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
