package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.mobile_client.viewModels.ThemeViewModel
import kotlinx.coroutines.launch

private val DarkText = Color(0xFF1A1A1A)
private val ActiveGreen = Color(0xFF4CAF50)
private val ActionBlue = Color(0xFF2196F3)
private val AbandonRed = Color(0xFFE53935)

@Composable
fun GameScreen(navController: NavController, snackbarHostState: SnackbarHostState, gameLobbyViewModel: GameLobbyViewModel, themeViewModel: ThemeViewModel) {
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
    var visibleBushTiles by remember { mutableStateOf<List<Position>>(emptyList()) }
    var path by remember { mutableStateOf<List<Position>>(emptyList()) }
    var selectedTile by remember { mutableStateOf<Position?>(null) }

    val assets = themeViewModel.assets
    // Recalculate accessible tiles when player or turn changes
    LaunchedEffect(player, currentPlayer, isDebug, isBetweenTurn, gameTiles) {
        visibleBushTiles = controller.getVisibleBushTiles()
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
                    "Partie terminée et gagnant : $gameWinner",
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
                .background(assets.gameViewBackground)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = assets.gameViewBackgroundComponent)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Informations de la partie", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = assets.mainPageTextColor)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val mapSizeText = when (gameMap.size) {
                            TileConstants.MapSize.Small -> "10x10"
                            TileConstants.MapSize.Medium -> "15x15"
                            TileConstants.MapSize.Large -> "20x20"
                        }
                        Text("Taille: $mapSizeText", fontSize = 16.sp, color = assets.mainPageTextColor)
                        Text("Joueurs: ${players.size}/${originalPlayers.size}", fontSize = 16.sp, color = assets.mainPageTextColor)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Joueur actif :", fontSize = 16.sp, color = assets.mainPageTextColor)
                    Spacer(modifier = Modifier.height(10.dp))
                    ImageResources.avatarToImage[currentPlayer.avatar]?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = "Active player",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    if (isDebug) {
                        Text("Mode Debug", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            ShakeListener({},
                onHorizontalShake = {
                    shakeCount++
                    if (shakeCount == 3) {
                        controller.setDebug()
                        shakeCount = 0
                    }
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = assets.gameViewBackgroundComponent)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Informations du joueur", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = assets.mainPageTextColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ImageResources.avatarToImage[player.avatar]?.let {
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = "Player",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Text(player.username, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = assets.mainPageTextColor)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard("Vie", "${player.stats.life}", Modifier.weight(1f), color = assets.mainPageTextColor)
                            StatCard("Rapidité", "${player.stats.speed}", Modifier.weight(1f), color = assets.mainPageTextColor)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard("Attaque", "${player.stats.attack} + D${player.attack.value}", Modifier.weight(1f), color = assets.mainPageTextColor)
                            StatCard("Défense", "${player.stats.defense} + D${player.defense.value}", Modifier.weight(1f), color = assets.mainPageTextColor)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard("Mouvements restants", "${player.movement}", Modifier.weight(1f), color = assets.mainPageTextColor)
                            StatCard("Actions restantes", "${player.hasAction}", Modifier.weight(1f), color = assets.mainPageTextColor)
                        }
                    }
                }
            }

            // Inventory
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = assets.gameViewBackgroundComponent)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Inventaire", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = assets.mainPageTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(assets.gameViewBackgroundComponent)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        player.items.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(assets.inventoryBox)
                                    .drawBehind {
                                        val stroke = Stroke(
                                            width = 2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(10f, 6f), 0f
                                            )
                                        )
                                        drawRoundRect(
                                            color = Color(0xFFBDBDBD),
                                            style = stroke,
                                            cornerRadius = CornerRadius(12.dp.toPx())
                                        )
                                    },
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
                    visibleBushTiles = visibleBushTiles,
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
                                    controller.move(pos, path)
                                    path = emptyList()
                                    selectedTile = null
                                } else {
                                    // First click or different accessible tile: show path
                                    selectedTile = pos
                                    path = controller.getShortestPath(pos)
                                    println("SHOWING PATH TO $pos = $path")
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

        // === RIGHT COLUMN: Players List + Défi + Actions ===
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .background(assets.gameViewBackground)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Players list card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = assets.gameViewBackgroundComponent)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Liste des joueurs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = assets.mainPageTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.height(228.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(players) { p ->
                            PlayerList(
                                player = p,
                                isActive = currentPlayer.username == p.username,
                                isInGame = players.any { it.username == p.username },
                                isCTF = gameMap.isCaptureTheFlag,
                                hasFlag = controller.whoHasFlag()?.username == p.username,
                                playerItemActiveColor = assets.playerItemActive,
                                playerStatColor = assets.playerStats,
                                playerItemBackground = assets.playerItem,
                                arrowActiveColor = assets.joinButton
                            )
                        }
                    }
                }
            }

            // Défi card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = assets.gameViewBackgroundComponent)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Défi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = assets.mainPageTextColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        player.challenge.type.description,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = assets.mainPageTextColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${player.challenge.progress}/${player.challenge.goal} -> ${player.challenge.reward}$",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = assets.mainPageTextColor
                    )
                }
            }

            // Timer + Actions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = assets.gameViewBackgroundComponent)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (isTimerStopped) "Temps Restant: --" else "Temps Restant: $timerCounter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = assets.mainPageTextColor
                    )

                    Button(
                        onClick = { controller.nextTurn() },
                        enabled = player.username == currentPlayer.username && !isInCombat && !isBetweenTurn,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = assets.endTurn)
                    ) {
                        Text("Terminer son tour", color = assets.textActions)
                    }

                    Button(
                        onClick = { controller.setAction() },
                        enabled = player.username == currentPlayer.username && (currentPlayer.hasAction > 0) && !isInCombat && !isBetweenTurn,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAction) assets.action.copy(alpha = 0.7f) else assets.action
                        )
                    ) {
                        Text(if (isAction) "Action sélectionnée" else "Exécuter une action", color = assets.textActions)
                    }

                    Button(
                        onClick = {
                            controller.abandon {
                                gameLobbyViewModel.leaveLobby()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = assets.abandon)
                    ) {
                        Text("Abandonner la partie", color = assets.textActions)
                    }
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
    hasFlag: Boolean,
    playerItemActiveColor: Color,
    playerStatColor: Color,
    playerItemBackground: Color,
    arrowActiveColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isActive) playerItemActiveColor
                else if (isInGame) playerItemBackground
                else playerItemBackground
            )
            .padding(6.dp)
            .then(
                if (player.isObserver) {
                    Modifier.drawWithContent {
                        drawContent()
                        drawLine(
                            color = Color.Black,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                            strokeWidth = 3f
                        )
                    }
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Text("▶", color = arrowActiveColor, fontSize = 12.sp)
            }
        }

        ImageResources.avatarToImage[player.avatar]?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = player.username,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.size(4.dp))

        Row(
            modifier = Modifier.width(120.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (player.playerType == PlayerTypes.Host) {
                Text("Host -", fontSize = 16.sp, color = Color.Gray)
            }
            Text(
                player.username,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCTF && player.team == 1) Color.Red
                else if (isCTF && player.team == 2) Color.Blue
                else DarkText
            )
            if (player.isBot()) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = "Computer",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box(
            modifier = Modifier.width(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasFlag) {
                Text("🚩", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text("${player.winNumber} victoires", fontSize = 10.sp, color = playerStatColor)
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Column(
        modifier = modifier
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 12.sp, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
