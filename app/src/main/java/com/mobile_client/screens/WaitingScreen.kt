package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.mobile_client.components.ChatBox
import com.mobile_client.components.Header
import com.mobile_client.viewModels.ChatViewModel
import com.mobile_client.services.GameLobbyService
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import com.mobile_client.utils.PlayerTypes
import com.mobile_client.utils.Screen
import com.mobile_client.utils.isBot

private val BotBlue = Color(0xFF20B6E3)
private val LockOrange = Color(0xFFD88B06)
private val StartGreen = Color(0xFF109E1F)
private val DarkText = Color(0xFF1A1A1A)

@Composable
fun WaitingPageScreen(navController: NavController, chatViewModel: ChatViewModel) {
    val players = GameLobbyService.players
    val currentPlayer = GameLobbyService.currentPlayer.value
    val isLobbyLocked = GameLobbyService.isLobbyLocked.value
    val lobbyCode = GameLobbyService.lobbyCode.value
    val isHost = currentPlayer?.playerType == PlayerTypes.Host

    LaunchedEffect(GameLobbyService.lobbyCode.value, players.size) {
        if (GameLobbyService.lobbyCode.value.isEmpty() && !GameLobbyService.isGameStarted.value) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!GameLobbyService.isGameStarted.value) {
                GameLobbyService.selectAvatar(
                    currentPlayer?.avatar ?: PlayerAvatars.None,
                    PlayerAvatars.None
                )
                GameLobbyService.leaveLobby()
            }
        }
    }

    LaunchedEffect(GameLobbyService.isGameStarted.value) {
        if (GameLobbyService.isGameStarted.value) {
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

        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                navController = navController,
                title = "Salle d'attente",
                chatViewModel,
                showBackButton = true
            )

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

                // Main layout: Players on left, Chat on right
                Row(
                    modifier = Modifier.fillMaxSize(),
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
                                PlayerCard(player, isHost)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Host controls
                        if (isHost) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { GameLobbyService.createBotPlayer() },
                                    border = BorderStroke(2.dp, BotBlue),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("+ Ajouter un JV", color = BotBlue, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { GameLobbyService.toggleLobbyLock() },
                                    border = BorderStroke(2.dp, LockOrange),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (isLobbyLocked) "Déverrouiller" else "Verrouiller",
                                        color = LockOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // === RIGHT: Chat + Start game ===
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Chat wrapper
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFCAC6C6))
                                .padding(10.dp)
                        ) {
                            Text(
                                "Clavardage de ${currentPlayer?.username ?: ""}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ChatBox(
                                chatViewModel = chatViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Start game button (host only)
                        if (isHost) {
                            OutlinedButton(
                                onClick = { GameLobbyService.startGame() },
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
    }
}

@Composable
fun PlayerCard(player: Player, isHost: Boolean) {
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
                    Text("Robot", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Kick button (host can kick non-host players)
        if (isHost && player.playerType != PlayerTypes.Host) {
            Button(
                onClick = { GameLobbyService.kickPlayer(player) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Expulser", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
