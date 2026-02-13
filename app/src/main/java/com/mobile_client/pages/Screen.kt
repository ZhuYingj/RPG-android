package com.mobile_client.pages

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object Account : Screen("account")
    object GameCreation : Screen("game_creation")
    object JoinGame : Screen("join_game")

    object Lobby : Screen("lobby")

    object Game : Screen("game")
}
