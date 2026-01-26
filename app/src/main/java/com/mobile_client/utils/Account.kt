package com.mobile_client.utils

data class Account(
    val userId: String,
    val username: String,
    val email: String,
    val password: String? = null,
    val avatar: String? = null
)

data class LoginResponse(
    val account: Account,
    val token: String
)
