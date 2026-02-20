package com.mobile_client.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.components.GameList
import com.mobile_client.components.Header
import com.mobile_client.viewModels.BaseGameListViewModel
import com.mobile_client.viewModels.ChatViewModel

@Composable
fun GamesCreationScreen(navController: NavController, chatViewModel: ChatViewModel, gameListViewModel: BaseGameListViewModel) {

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
