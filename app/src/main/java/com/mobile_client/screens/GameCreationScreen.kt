package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobile_client.components.GameList
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.GameListViewModel
import com.mobile_client.viewModels.GameLobbyViewModel
import com.mobile_client.viewModels.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun GamesCreationScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    gameListViewModel: GameListViewModel,
    gameLobbyViewModel: GameLobbyViewModel,
    themeViewModel: ThemeViewModel,
) {
    var pendingMap by remember { mutableStateOf<GameMap?>(null) }
    var showFeeDialog by remember { mutableStateOf(false) }
    var isRapid by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val assets = themeViewModel.assets

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
                        onValueChange = { if (it.all(Char::isDigit) && it.length <= 5) feeInput = it },
                        label = { Text("Montant") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isRapid,
                            onCheckedChange = { isRapid = it }
                        )
                        Text("Élimination Rapide")
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = feeInput != "",
                    onClick = {
                        val fee = feeInput.toIntOrNull() ?: 0
                        showFeeDialog = false
                        gameLobbyViewModel.createLobby(
                            map = pendingMap!!,
                            fee = fee,
                            isRapid = isRapid,
                            onSuccess = { navController.navigate(Screen.CharacterCreation.route) },
                            onError = { message ->
                                gameLobbyViewModel.showMessage(message)
                                pendingMap = null
                            }
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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(assets.backgroundCreationPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp),
        ) {
            Text(
                text = "Création d'une partie",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = assets.mainPageTextColor,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Liste des jeux",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = assets.mainPageTextColor,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                ),
                modifier = Modifier
                    .padding(start = 24.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                GameList(gameListViewModel, themeViewModel = themeViewModel)
            }
        }
    }
}
