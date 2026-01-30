package com.mobile_client.pages

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun GamesScreen(navController: NavController) {
    Text(
        text = "Créer un jeu",
        style = MaterialTheme.typography.headlineMedium
    )
}
