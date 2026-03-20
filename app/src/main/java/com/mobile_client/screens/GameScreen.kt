package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobile_client.components.BattleOverlay
import com.mobile_client.components.BetweenTurnOverlay
import com.mobile_client.components.GameBoard
import com.mobile_client.components.ItemChoiceOverlay
import com.mobile_client.components.ShakeListener
import com.mobile_client.services.GameControllerService
import com.mobile_client.services.SocketService
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerTypes
import com.mobile_client.utils.Position
import com.mobile_client.utils.Screen
import com.mobile_client.utils.TileConstants
import com.mobile_client.utils.isBot
import com.mobile_client.viewModels.GameLobbyViewModel
import kotlinx.coroutines.launch

private val DarkText = Color(0xFF1A1A1A)
private val ActiveGreen = Color(0xFF4CAF50)
private val ActionBlue = Color(0xFF2196F3)
private val AbandonRed = Color(0xFFE53935)

@Composable
fun GameScreen(navController: NavController, snackbarHostState: SnackbarHostState, gameLobbyViewModel: GameLobbyViewModel) {
    val controller = GameControllerService.instance
    val scope = rememberCoroutineScope()

    val gameMap = controller.gameMap.value
    val gameTiles = controller.gameTiles.value
    val players = controller.players.value
    val player = controller.player.value
    val currentPlayer = controller.currentPlayer.value
    val originalPlayers = controller.originalPlayers.value
    val isAction = controller.isAction.value
    val isBetweenTurn = controller.isBetweenTurn.value
    val isItemChoice = controller.isItemChoice.value
    val timerCounter = controller.timerCounter.intValue
    val isTimerStopped = controller.isTimerStopped.value
    val isDebug = controller.isDebug.value
    val isInCombat = controller.fightService.isFight.value
    val gameWinner = controller.gameWinner.value
    val lastPlayer = controller.lastPlayer.value
    val serverMessage = controller.serverMessage.value
    var shakeCount by remember { mutableIntStateOf(0) }


    var accessibleTiles by remember { mutableStateOf<List<Position>>(emptyList()) }
    var path by remember { mutableStateOf<List<Position>>(emptyList()) }
    var selectedTile by remember { mutableStateOf<Position?>(null) }


    // Recalculate accessible tiles when player or turn changes
    LaunchedEffect(player, currentPlayer, isDebug, isBetweenTurn, gameTiles) {
        accessibleTiles = controller.getAccessibleTiles()
        path = emptyList()
        selectedTile = null
    }

    // Navigate home if no map
    LaunchedEffect(gameMap) {
        if (gameMap == null || gameMap.name.isEmpty()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Configure listeners on init
    LaunchedEffect(Unit) {
//        println("INIT player movement: ${player?.movement}")
//        println("INIT player position: ${player?.position}")
//        controller.originalPlayers.value = players.toList()
//        controller.currentPlayer.value = player
//        controller.configureListeners()

        //TODO print for debug also its never closed so its stacks every game
        SocketService.instance.socket?.onAnyIncoming { args ->
            val eventName = if (args.isNotEmpty()) args[0].toString() else "unknown"
            println("INCOMING EVENT: $eventName")}

        controller.lastPlayer.value = false
        controller.fightService.isFight.value = false
        controller.isDebug.value = false
        controller.isTimerStopped.value = false
        controller.isAction.value = false
        controller.isItemChoice.value = false
    }

    LaunchedEffect(lastPlayer) {
        if(lastPlayer) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Vous êtes le seul joueur restant",
                    duration = SnackbarDuration.Long
                )
            }
            controller.clear()
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(serverMessage) {
        if (serverMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                serverMessage,
                duration = SnackbarDuration.Short
            )
            controller.serverMessage.value = ""
        }
    }

    // Navigate to end game when winner is set
    LaunchedEffect(gameWinner) {
        if (gameWinner.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Partie terminée et gagnant : $gameWinner",
                    duration = SnackbarDuration.Long
                )
            }
            //TODO: Navigate to end game (EndGameScreen, where there is stats) after delay
            navController.navigate(Screen.EndGame.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (controller.gameWinner.value.isEmpty()) {
                controller.abandon {}
            }
            SocketService.instance.closeGameListeners()
        }
    }

    if (gameMap == null || player == null || currentPlayer == null) return

    Row(modifier = Modifier.fillMaxSize()) {
        // === LEFT COLUMN: Game Info + Player Info + Inventory ===
        Column(
            modifier = Modifier
                .weight(0.2f)
                .fillMaxHeight()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Game info
            Text("Informations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Taille: ${gameMap.size}", fontSize = 12.sp)
            Text("Joueurs: ${players.size}/${originalPlayers.size}", fontSize = 12.sp)
            Text("Joueur actif:", fontSize = 12.sp)
            ImageResources.avatarToImage[currentPlayer.avatar]?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Active player",
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text( if (isDebug) "Mode Debug" else "", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            ShakeListener({},
                onHorizontalShake = {
                    shakeCount++
                    if (shakeCount == 3) {
                        controller.setDebug()
                        shakeCount = 0
                    }
                }
            )

            // Player stats
            Text("Mon joueur", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(player.username, fontSize = 12.sp)
            ImageResources.avatarToImage[player.avatar]?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Player",
                    modifier = Modifier.size(32.dp)
                )
            }
            Text("Vie: ${player.stats.life}", fontSize = 11.sp)
            Text("Rapidité: ${player.stats.speed}", fontSize = 11.sp)
            Text("Attaque: ${player.stats.attack} (D${player.attack.value})", fontSize = 11.sp)
            Text("Défense: ${player.stats.defense} (D${player.defense.value})", fontSize = 11.sp)
            Text("Actions: ${player.hasAction}", fontSize = 11.sp)
            Text("Mouvements: ${player.movement}", fontSize = 11.sp)

            Spacer(modifier = Modifier.height(4.dp))

            // Inventory
            Text("Inventaire", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                player.items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item != TileConstants.Items.None) {
                            ImageResources.itemToImage[item]?.let { resId ->
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = "Item",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // === CENTER COLUMN: Game Board ===
        Box(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (gameTiles.isNotEmpty()) {
                GameBoard(
                    tiles = gameTiles,
                    players = players,
                    player = player,
                    accessibleTiles = accessibleTiles,
                    path = path,
                    isDebug = isDebug,
                    onTileClick = { pos ->
                        if (isDebug && controller.isTeleport()) {
                            // Debug mode: teleport directly
                            println("TELEPORTING TO $pos")
                            controller.teleport(pos)
                        } else if (isAction) {
                            controller.action(pos)
                        }
                        else {
                            val isAccessible = accessibleTiles.any { it.x == pos.x && it.y == pos.y }
                            if (isAccessible) {
                                if (selectedTile != null && selectedTile!!.x == pos.x && selectedTile!!.y == pos.y) {
                                    // Second click on same tile: execute move
                                    controller.move(pos)
                                    path = emptyList()
                                    selectedTile = null
                                } else {
                                    // First click or different accessible tile: show path
                                    selectedTile = pos
                                    path = controller.getShortestPath(pos)
                                }
                            } else {
                                // Non-accessible tile: clear path
                                controller.serverMessage.value = "Veuillez choisir une autre tuile, vous avez choisi une tuile invalide"
                                path = emptyList()
                                selectedTile = null
                            }
                        }
                    }
                )
            }
        }

        // === RIGHT COLUMN: Players List + Actions ===
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Players list
            Text("Joueurs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(players) { p ->
                    PlayerList(
                        player = p,
                        isActive = currentPlayer.username == p.username,
                        isInGame = players.any { it.username == p.username },
                        isCTF = gameMap.isCaptureTheFlag,
                        hasFlag = controller.whoHasFlag()?.username == p.username
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column( modifier = Modifier.weight(0.75f)) {
                // Timer
                Text(
                    if (isTimerStopped) "Temps: --" else "Temps: $timerCounter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Action buttons
                Button(
                    onClick = { controller.nextTurn() },
                    enabled = player.username == currentPlayer.username && !isInCombat && !isBetweenTurn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen)
                ) {
                    Text("Terminer son tour", color = Color.White)
                }

                Button(
                    onClick = { controller.setAction() },
                    enabled = player.username == currentPlayer.username && (currentPlayer.hasAction > 0) && !isInCombat && !isBetweenTurn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAction) ActionBlue.copy(alpha = 0.7f) else ActionBlue
                    )
                ) {
                    Text(if (isAction) "Action sélectionnée" else "Exécuter une action", color = Color.White)
                }

                Button(
                    onClick = { controller.abandon {
                        gameLobbyViewModel.leaveLobby()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AbandonRed)
                ) {
                    Text("Abandonner", color = Color.White)
                }
            }

        }
    }

    // Overlays
    if (isInCombat) {
        BattleOverlay(controller.fightService, timerCounter)
    }
    if (isBetweenTurn) {
        BetweenTurnOverlay(currentPlayer)
    }
    if (isItemChoice) {
        ItemChoiceOverlay(controller)
        //TODO si timer run out reject default item?
    }
}

@Composable
fun PlayerList(
    player: Player,
    isActive: Boolean,
    isInGame: Boolean,
    isCTF: Boolean,
    hasFlag: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isInGame) Color.White else Color.Gray.copy(alpha = 0.3f))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isActive) {
            Text("▶", color = ActiveGreen, fontSize = 12.sp)
        }
        ImageResources.avatarToImage[player.avatar]?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = player.username,
                modifier = Modifier.size(24.dp)
            )
        }
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (player.playerType == PlayerTypes.Host) {
                    Text("Host", fontSize = 10.sp, color = Color.Gray)
                }
                Text(
                    player.username,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCTF && player.team == 1) Color.Red
                    else if (isCTF && player.team == 2) Color.Blue
                    else DarkText
                )
                if (player.isBot()) {
                    Text("🖥️", fontSize = 10.sp)
                }
                if (hasFlag) {
                    Text("🚩", fontSize = 10.sp)
                }
            }
            Text("${player.winNumber} victoires", fontSize = 10.sp, color = Color.Gray)
        }
    }
}
