package com.mobile_client.pages

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object Account : Screen("account")
    object Games : Screen("games")
    object JoinGame : Screen("join_game")
}
