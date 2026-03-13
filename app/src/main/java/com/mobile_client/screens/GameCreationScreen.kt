package com.mobile_client.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.components.GameList
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.GameListViewModel
import com.mobile_client.viewModels.GameLobbyViewModel
import kotlinx.coroutines.launch

@Composable
fun GamesCreationScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    gameListViewModel: GameListViewModel,
    gameLobbyViewModel: GameLobbyViewModel
) {
    var pendingMap by remember { mutableStateOf<GameMap?>(null) }
    var showFeeDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        gameListViewModel.mapSelected.collect { map ->
            pendingMap = map
            showFeeDialog = true
        }
    }

    LaunchedEffect(Unit) {
        gameLobbyViewModel.errorMessage.collect { message ->
            scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
        }
    }

    if (showFeeDialog && pendingMap != null) {
        var feeInput by remember { mutableStateOf("0") }
        AlertDialog(
            onDismissRequest = {
                showFeeDialog = false
                pendingMap = null
            },
            title = { Text("Frais d'entrée") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Définir un frais d'entrée pour rejoindre ce lobby")
                    OutlinedTextField(
                        value = feeInput,
                        onValueChange = { if (it.all(Char::isDigit)) feeInput = it },
                        label = { Text("Montant") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = feeInput != "",
                    onClick = {
                        showFeeDialog = false
                        gameLobbyViewModel.createLobby(
                            map = pendingMap!!,
                            fee = feeInput.toIntOrNull() ?: 0,
                            onSuccess = { navController.navigate(Screen.CharacterCreation.route) },
                            onError = { message -> gameLobbyViewModel.showMessage(message) }
                        )
                        pendingMap = null
                    }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFeeDialog = false
                    pendingMap = null
                }) { Text("Annuler") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 56.dp)) {
        Text(text = "Créer un jeu", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Liste des jeux disponibles", style = MaterialTheme.typography.headlineMedium)
        Box(modifier = Modifier.fillMaxSize()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GameList(gameListViewModel)
            }
        }
    }
}
