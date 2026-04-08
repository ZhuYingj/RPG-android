package com.mobile_client.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.mobile_client.viewModels.ThemeViewModel
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.components.PressableButton
import com.mobile_client.utils.FontSize
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.showDismissible

private val BotBlue = Color(0xFF20B6E3)
private val LockOrange = Color(0xFFD88B06)
private val StartGreen = Color(0xFF109E1F)
private val DarkText = Color(0xFF1A1A1A)

@Composable
fun WaitingPageScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    gameLobbyViewModel: GameLobbyViewModel,
    themeViewModel: ThemeViewModel,
) {
    val assets = themeViewModel.assets
    val players = gameLobbyViewModel.players
    val currentPlayer = gameLobbyViewModel.currentPlayer.value
    val isLobbyLocked = gameLobbyViewModel.isLobbyLocked.value
    val lobbyCode = gameLobbyViewModel.lobbyCode.value
    val isHost = currentPlayer?.playerType == PlayerTypes.Host
    val entryFee = gameLobbyViewModel.entryFee.intValue
    val isDropIn = gameLobbyViewModel.isDropIn.value
    val isFriendOnly = gameLobbyViewModel.isFriendOnly.value

    val scope = rememberCoroutineScope()
    println("WaitingScreen ENTERED: lobbyCode=${gameLobbyViewModel.lobbyCode.value} isGameStarted=${gameLobbyViewModel.isGameStarted.value} isDropIn=${gameLobbyViewModel.isDropIn.value}")

    LaunchedEffect(Unit) {
        gameLobbyViewModel.errorMessage.collect { message ->
            snackbarHostState.showDismissible(scope, message)
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
                println("leave lobby called")
                gameLobbyViewModel.leaveLobby()
            }
        }
    }

    LaunchedEffect(gameLobbyViewModel.isGameStarted.value) {
        println("WaitingScreen isGameStarted changed: ${gameLobbyViewModel.isGameStarted.value}")

        if (gameLobbyViewModel.isGameStarted.value) {
            println("WaitingScreen navigating to Game")
            val controller = GameControllerService.instance
            controller.gameMap.value = gameLobbyViewModel.gameMap.value
            controller.gameTiles.value = gameLobbyViewModel.gameMap.value?.tiles?.toGameTiles() ?: emptyList()
            controller.players.value = gameLobbyViewModel.players.toList()
            controller.player.value = gameLobbyViewModel.currentPlayer.value
            controller.originalPlayers.value = gameLobbyViewModel.players.toList()
            controller.lastPlayer.value = false
            snackbarHostState.showDismissible(scope, "La partie commence")

            navController.navigate(Screen.Game.route) {
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(assets.backgroundWaitingPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (lobbyCode.isNotEmpty()) {
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
                        color = assets.mainPageTextColor,
                        fontSize = FontSize.MENU_BUTTON.sp,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Statut de la salle d'attente: ${if (isLobbyLocked) "Verrouillée" else "Déverrouillée"}",
                        fontSize = FontSize.SUBTITLE.sp,
                        color = assets.mainPageTextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Frais d'entrée: $entryFee$",
                        fontSize = FontSize.SUBTITLE.sp,
                        color = assets.mainPageTextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Défi : ${currentPlayer?.challenge?.type?.description}\t" +
                            "${currentPlayer?.challenge?.progress}/${currentPlayer?.challenge?.goal} -> ${currentPlayer?.challenge?.reward}$",
                        fontSize = FontSize.SUBTITLE.sp,
                        color = assets.mainPageTextColor
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
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(assets.userListBackgroundColor)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Text(
                                        "Liste des joueurs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = FontSize.BODY.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(players) { player ->
                                    PlayerCard(
                                        player,
                                        isHost,
                                        gameLobbyViewModel,
                                        textColor = Color.Black,
                                        bgColor = assets.userBackgroundColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // === RIGHT: Start game ===
                        if (isHost) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                PressableButton(
                                    shadowColor = BotBlue,
                                    cornerRadius = 6.dp,
                                    shadowTopInset = 4.dp
                                ) { interactionSource, pressModifier ->
                                    OutlinedButton(
                                        onClick = { gameLobbyViewModel.createBotPlayer() },
                                        modifier = pressModifier.width(160.dp),
                                        interactionSource = interactionSource,
                                        border = BorderStroke(2.dp, BotBlue),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = assets.textAccount),
                                        shape = RoundedCornerShape(6.dp),
                                    ) {
                                        Text("+ Ajouter un JV", color = BotBlue, fontWeight = FontWeight.Bold, fontSize = FontSize.SUBTITLE.sp)
                                    }
                                }

                                PressableButton(
                                    shadowColor = LockOrange,
                                    cornerRadius = 6.dp,
                                    shadowTopInset = 4.dp
                                ) { interactionSource, pressModifier ->
                                    OutlinedButton(
                                        onClick = { gameLobbyViewModel.toggleLobbyLock() },
                                        modifier = pressModifier.width(160.dp),
                                        interactionSource = interactionSource,
                                        border = BorderStroke(2.dp, LockOrange),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = assets.textAccount),
                                        shape = RoundedCornerShape(6.dp),
                                    ) {
                                        Text(
                                            if (isLobbyLocked) "Déverrouiller" else "Verrouiller",
                                            color = LockOrange,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = FontSize.SUBTITLE.sp,
                                        )
                                    }
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.width(175.dp)
                                ) {
                                    val toggleLabelWidth = 120.dp

                                    // Friends Only toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Friends only",
                                            color = assets.mainPageTextColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = FontSize.SUBTITLE.sp,
                                            modifier = Modifier.width(toggleLabelWidth),
                                            textAlign = TextAlign.Start
                                        )
                                        Switch(
                                            checked = isFriendOnly,
                                            onCheckedChange = { gameLobbyViewModel.toggleFriendOnly() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF4CAF50),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }

                                    // Drop-in toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Drop-in",
                                            color = assets.mainPageTextColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = FontSize.SUBTITLE.sp,
                                            modifier = Modifier.width(toggleLabelWidth),
                                            textAlign = TextAlign.Start
                                        )
                                        Switch(
                                            checked = isDropIn,
                                            onCheckedChange = { gameLobbyViewModel.toggleDropIn() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF4CAF50),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }

                                    // QR Code toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "QR Code",
                                            color = assets.mainPageTextColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = FontSize.SUBTITLE.sp,
                                            modifier = Modifier.width(toggleLabelWidth),
                                            textAlign = TextAlign.Start
                                        )
                                        Switch(
                                            checked = gameLobbyViewModel.showQrCode.value,
                                            onCheckedChange = { gameLobbyViewModel.toggleQrCode() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF4CAF50),
                                                uncheckedThumbColor = Color.White,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }
                                }

                                PressableButton(
                                    shadowColor = StartGreen,
                                    cornerRadius = 6.dp,
                                    shadowTopInset = 4.dp
                                ) { interactionSource, pressModifier ->
                                    OutlinedButton(
                                        onClick = { gameLobbyViewModel.startGame() },
                                        modifier = pressModifier,
                                        interactionSource = interactionSource,
                                        border = BorderStroke(2.dp, StartGreen),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = assets.textAccount),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "Commencer la partie",
                                            color = StartGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = FontSize.SUBTITLE.sp,
                                        )
                                    }
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
fun PlayerCard(player: Player, isHost: Boolean, gameLobbyViewModel: GameLobbyViewModel, textColor: Color = Color(0xFF1A1A1A), bgColor: Color = Color.White) {
    var botMenuExpanded by remember { mutableStateOf(false) }
    val profileBitmap = remember(player.profilePicture) {
        ImageUtils.base64ToBitmap(player.profilePicture)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = 0.7f))
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
            Column (modifier = Modifier.width(100.dp)){
                Text(
                    player.username,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (player.playerType == PlayerTypes.Host) {
                    Text("Hôte", fontSize = FontSize.BUTTON.sp, color = Color.Red)
                } else if (player.isBot()) {
                    Text(
                        if (player.playerType == PlayerTypes.BotAggressive) "Robot (Agressif)" else "Robot (Passif)",
                        fontSize = FontSize.BUTTON.sp,
                        color = Color.Blue
                    )
                }
            }
            if (!player.isBot()) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profileBitmap),
                            contentDescription = "Photo de profil",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Host controls for non-host players
        if (isHost && player.playerType != PlayerTypes.Host) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (player.isBot()) {
                    Box {
                        OutlinedButton(
                            onClick = { botMenuExpanded = true },
                            border = BorderStroke(1.dp, Color.Gray),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (player.playerType == PlayerTypes.BotAggressive) "Agressif" else "Passif",
                                color = DarkText,
                                fontSize = FontSize.SMALL.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Changer type",
                                tint = DarkText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = botMenuExpanded,
                            onDismissRequest = { botMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Agressif", fontSize = FontSize.SMALL.sp) },
                                onClick = {
                                    if (player.playerType != PlayerTypes.BotAggressive) {
                                        gameLobbyViewModel.toggleBotType(player)
                                    }
                                    botMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Passif", fontSize = FontSize.SMALL.sp) },
                                onClick = {
                                    if (player.playerType != PlayerTypes.BotPassive) {
                                        gameLobbyViewModel.toggleBotType(player)
                                    }
                                    botMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Trash icon button instead of "Expulser" text
                IconButton(
                    onClick = { gameLobbyViewModel.kickPlayer(player) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Red, RoundedCornerShape(6.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "Expulser",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
