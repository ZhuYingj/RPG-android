package com.mobile_client.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.components.GameList
import com.mobile_client.components.Header
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.ChatViewModel
import com.mobile_client.viewModels.GameListViewModel
import com.mobile_client.viewModels.GameLobbyViewModel

@Composable
fun GamesCreationScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
    gameListViewModel: GameListViewModel,
    gameLobbyViewModel: GameLobbyViewModel
) {

    LaunchedEffect(Unit) {
        gameListViewModel.mapSelected.collect { map ->
            gameLobbyViewModel.createLobby(
                map = map,
                onSuccess = {
                    navController.navigate(Screen.CharacterCreation.route)
                },
                onError = { message ->
                    println("Erreur lors du démarrage du jeu: $message")
                }
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Header(navController, "Création de partie", chatViewModel)
        Text(
            text = "Créer un jeu",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Liste des jeux disponibles",
            style = MaterialTheme.typography.headlineMedium
        )
        GameList(gameListViewModel, modifier = Modifier.fillMaxSize())
    }
}
