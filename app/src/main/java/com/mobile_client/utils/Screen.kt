package com.mobile_client.utils

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object Account : Screen("account")
    object GameCreation : Screen("game_creation")
    object JoinGame : Screen("join_game")

    object CharacterCreation : Screen("character_form")

    object WaitingPage : Screen("game_lobby")

    object Game : Screen("game")
    object EndGame : Screen("end_game")
    object Inventory : Screen("inventory")
    object Shop : Screen("shop")
}
