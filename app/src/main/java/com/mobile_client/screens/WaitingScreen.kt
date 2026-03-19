package com.mobile_client.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobile_client.services.GameControllerService
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import com.mobile_client.utils.PlayerTypes
import com.mobile_client.utils.Screen
import com.mobile_client.utils.isBot
import com.mobile_client.utils.toGameTiles
import com.mobile_client.viewModels.GameLobbyViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap

private val BotBlue = Color(0xFF20B6E3)
private val LockOrange = Color(0xFFD88B06)
private val DropInCyan = Color(0xFF009688)
private val StartGreen = Color(0xFF109E1F)
private val DarkText = Color(0xFF1A1A1A)

@Composable
fun WaitingPageScreen(navController: NavController, snackbarHostState: SnackbarHostState, gameLobbyViewModel: GameLobbyViewModel) {
    val players = gameLobbyViewModel.players
    val currentPlayer = gameLobbyViewModel.currentPlayer.value
    val isLobbyLocked = gameLobbyViewModel.isLobbyLocked.value
    val lobbyCode = gameLobbyViewModel.lobbyCode.value
    val isHost = currentPlayer?.playerType == PlayerTypes.Host
    val entryFee = gameLobbyViewModel.entryFee.intValue
    val isDropIn = gameLobbyViewModel.isDropIn.value
    val isFriendOnly = gameLobbyViewModel.isFriendOnly.value

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        gameLobbyViewModel.errorMessage.collect { message ->
            scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
        }
    }

    // Reroute to Home if lobby code is cleared (e.g. kicked or lobby closed)
    LaunchedEffect(lobbyCode, gameLobbyViewModel.isGameStarted.value) {
        if (lobbyCode.isEmpty() && !gameLobbyViewModel.isGameStarted.value) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!gameLobbyViewModel.isGameStarted.value && gameLobbyViewModel.lobbyCode.value.isNotEmpty()) {
                gameLobbyViewModel.selectAvatar(
                    currentPlayer?.avatar ?: PlayerAvatars.None,
                    PlayerAvatars.None
                )
                gameLobbyViewModel.leaveLobby()
            }
        }
    }

    LaunchedEffect(gameLobbyViewModel.isGameStarted.value) {
        if (gameLobbyViewModel.isGameStarted.value) {
            val controller = GameControllerService.instance
            controller.gameMap.value = gameLobbyViewModel.gameMap.value
            controller.gameTiles.value = gameLobbyViewModel.gameMap.value?.tiles?.toGameTiles() ?: emptyList()
            controller.players.value = gameLobbyViewModel.players.toList()
            controller.player.value = gameLobbyViewModel.currentPlayer.value
            controller.originalPlayers.value = gameLobbyViewModel.players.toList()
            controller.lastPlayer.value = false

            navController.navigate(Screen.Game.route) {
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.grass_backround),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (lobbyCode.isNotEmpty()) { // Only render content if we are still in a lobby
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Salle d'attente $lobbyCode",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Statut de la salle d'attente: ${if (isLobbyLocked) "Verrouillée" else "Déverrouillée"}",
                        fontSize = 18.sp,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Frais d'entrée : $entryFee$",
                        fontSize = 18.sp,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main layout: Players on left, Chat on right
                    Row(
                        modifier = Modifier.width(800.dp).fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Player list
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.3f))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(players) { player ->
                                    PlayerCard(player, isHost, gameLobbyViewModel)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // === RIGHT: Start game ===
                        if (isHost) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                OutlinedButton(
                                    onClick = { gameLobbyViewModel.toggleQrCode() },
                                    border = BorderStroke(2.dp, DarkText),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (gameLobbyViewModel.showQrCode.value) "Masquer le code QR" else "Afficher le code QR",
                                        color = DarkText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { gameLobbyViewModel.toggleLobbyLock() },
                                    border = BorderStroke(2.dp, LockOrange),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (isLobbyLocked) "Déverrouiller" else "Verrouiller",
                                        color = LockOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        gameLobbyViewModel.toogleDropIn()
                                    },
                                    border = BorderStroke(2.dp, DropInCyan),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (isDropIn) "dropIn active" else "dropIn desactive",
                                        color = DropInCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        gameLobbyViewModel.toogleFriendOnly()
                                    },
                                    border = BorderStroke(2.dp, DropInCyan),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (isFriendOnly) "FriendOnly active" else "FriendOnly desactive",
                                        color = DropInCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { gameLobbyViewModel.createBotPlayer() },
                                    border = BorderStroke(2.dp, BotBlue),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("+ Ajouter un JV", color = BotBlue, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { gameLobbyViewModel.startGame() },
                                    border = BorderStroke(2.dp, StartGreen),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "Commencer la partie",
                                        color = StartGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                        }
                    }
                }
            }
            if (gameLobbyViewModel.showQrCode.value && gameLobbyViewModel.qrCodeDataUrl.value != null) {
                val dataUrl = gameLobbyViewModel.qrCodeDataUrl.value!!
                val base64 = dataUrl.substringAfter("base64,")
                val bitmap = remember(dataUrl) {
                    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerCard(player: Player, isHost: Boolean, gameLobbyViewModel: GameLobbyViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            ImageResources.avatarToImage[player.avatar]?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = player.avatar.name,
                    modifier = Modifier
                        .height(40.dp)
                        .width(40.dp)
                )
            }

            // Username + type
            Column {
                Text(
                    player.username,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                if (player.playerType == PlayerTypes.Host) {
                    Text("Hôte", fontSize = 12.sp, color = Color.Gray)
                } else if (player.isBot()) {
                    Text(
                        if (player.playerType == PlayerTypes.BotAggressive) "Robot (Agressif)" else "Robot (Passif)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Kick button (host can kick non-host players)
        if (isHost && player.playerType != PlayerTypes.Host) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (player.isBot()) {
                    Button(
                        onClick = { gameLobbyViewModel.toggleBotType(player) },
                        colors = ButtonDefaults.buttonColors(containerColor = BotBlue),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (player.playerType == PlayerTypes.BotAggressive) "Passif" else "Agressif",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = { gameLobbyViewModel.kickPlayer(player) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Expulser", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
