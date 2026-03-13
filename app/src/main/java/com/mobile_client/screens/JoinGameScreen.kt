package com.mobile_client.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.components.GameList
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.CurrentGamesViewModel
import com.mobile_client.viewModels.GameLobbyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun JoinGameScreen(navController: NavController, snackbarHostState: SnackbarHostState, currentGameListViewModel: CurrentGamesViewModel, gameLobbyViewModel: GameLobbyViewModel) {
    var showCodeDialog by remember { mutableStateOf(false) }
    var lobbyCode by remember { mutableStateOf("") }
    val scope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        currentGameListViewModel.lobbySelected.collect { lobbyCode ->
            gameLobbyViewModel.joinLobby(
                lobbyCode,
                {navController.navigate(Screen.CharacterCreation.route)},
                { message -> scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) } }
            )
        }
    }

    LaunchedEffect(Unit) {
        gameLobbyViewModel.errorMessage.collect { message ->
            scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 56.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Scanner un code QR", color = Color.Black)
            }
            Button(
                onClick = { showCodeDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Entrer un code", color = Color.Black)
            }
            Button(
                onClick = {currentGameListViewModel.loadMaps()},
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Reload", color = Color.Black)
            }
        }
        Text(
            text = "Liste des salles",
            style = MaterialTheme.typography.headlineMedium
        )
        GameList(currentGameListViewModel)
    }

    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showCodeDialog = false
            },
            title = { Text("Entrer un code de lobby") },
            text = {
                OutlinedTextField(
                    value = lobbyCode,
                    onValueChange = { newValue ->
                        if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                            lobbyCode = newValue
                        }
                    },
                    label = { Text("Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCodeDialog = false
                        gameLobbyViewModel.joinLobby(
                            lobbyCode,
                            {navController.navigate(Screen.CharacterCreation.route)},
                            { message -> scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) } }
                        )
                    },
                    enabled = lobbyCode.isNotBlank()
                ) {
                    Text("Rejoindre")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCodeDialog = false
                    lobbyCode = ""
                }) {
                    Text("Annuler")
                }
            }
        )
    }
}
