package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
            modifier = Modifier.width(350.dp),
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            shape = RoundedCornerShape(4.dp),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Définir un frais d'entrée pour rejoindre ce lobby")
                    OutlinedTextField(
                        value = feeInput,
                        onValueChange = { if (it.all(Char::isDigit) && it.length <= 5) feeInput = it },
                        label = { Text("Frais d'entrée") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF6650A4),
                            focusedBorderColor = Color(0xFF6650A4),
                            unfocusedLabelColor = Color(0xFF6650A4),
                            focusedLabelColor = Color(0xFF6650A4),
                            cursorColor = Color(0xFF6650A4)
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isRapid,
                            onCheckedChange = { isRapid = it },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                checkedColor = Color(0xFF6650A4)
                            )
                        )
                        Text("Élimination Rapide")
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        showFeeDialog = false
                        pendingMap = null
                    }) { Text("Annuler", color = Color.Black) }

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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6650A4)),
                        shape = RoundedCornerShape(4.dp)
                    ) { Text("Confirmer", color = Color.White) }
                }
            },
            dismissButton = {}
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
                .fillMaxWidth(0.75f)
                .fillMaxSize()
                .align(Alignment.Center)
                .background(Color(0x5C302F2F), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Création d'une partie",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = assets.mainPageTextColor,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp).align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Liste des jeux",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = assets.mainPageTextColor,
                modifier = Modifier.padding(start = 15.dp, bottom = 8.dp)
            )

            GameList(gameListViewModel, themeViewModel = themeViewModel)
        }
    }
}
