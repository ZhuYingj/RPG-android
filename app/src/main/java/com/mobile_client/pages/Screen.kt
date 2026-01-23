package com.mobile_client.pages

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object SingUp : Screen("sign_up")
}
