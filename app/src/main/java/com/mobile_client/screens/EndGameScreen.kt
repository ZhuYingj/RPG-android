package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobile_client.services.GameControllerService
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerStat
import com.mobile_client.utils.Screen
import com.mobile_client.utils.isBot
import com.mobile_client.viewModels.ThemeViewModel
import java.util.Locale

private val HeaderGreen = Color(0xFFBBF471)
private val EvenRowGray = Color(0xFFF9F9F9)
private val Team1Red = Color.Red
private val Team2Blue = Color.Blue

@Composable
fun EndGameScreen(navController: NavController, themeViewModel: ThemeViewModel) {
    val assets = themeViewModel.assets
    val controller = GameControllerService.instance
    val gameMap = controller.gameMap.value
//    val players = controller.players.value
//    val player = controller.player.value
    val originalPlayers = controller.originalPlayers.value
    val playerStats = controller.playerStats.value
    val gameStats = controller.gameStats.value
    val winner = controller.gameWinner.value
    val isCTF = gameMap?.isCaptureTheFlag ?: false

    var sortColumn by remember { mutableStateOf("name") }
    var isAscended by remember { mutableStateOf(false) }
    var sortedStats by remember { mutableStateOf(playerStats) }

    fun getSortableValue(stat: PlayerStat, column: String): Int {
        return when (column) {
            "fights" -> stat.fightNumber
            "evasions" -> stat.evasionNumber
            "wins" -> stat.winNumber
            "loses" -> stat.losesNumber
            "lifeLost" -> stat.lifeLost
            "damageDealt" -> stat.damageDealt
            "items" -> stat.itemPicked.size
            "tiles" -> stat.visitedTile.size
            else -> 0
        }
    }

    fun sortTable(column: String) {
        if (sortColumn == column) {
            isAscended = !isAscended
        } else {
            sortColumn = column
            isAscended = true
        }
        sortedStats = playerStats.sortedWith(Comparator { a, b ->
            if (column == "name") {
                if (isAscended) a.playerName.compareTo(b.playerName)
                else b.playerName.compareTo(a.playerName)
            } else {
                val aVal = getSortableValue(a, column)
                val bVal = getSortableValue(b, column)
                if (isAscended) aVal.compareTo(bVal) else bVal.compareTo(aVal)
            }
        })
    }

    fun showArrow(column: String): String {
        return if (sortColumn == column) {
            if (isAscended) " ▲" else " ▼"
        } else ""
    }

    fun isWinner(p: Player): Boolean {
        return if (isCTF) {
            if (winner == "équipe rouge") p.team == 1 else p.team == 2
        } else {
            p.username == winner
        }
    }

    fun getPlayerName(p: Player): String {
        var name = p.username
        if (p.isBot()) name += " 🖥️"
        if (isWinner(p)) name += " 👑"
        return name
    }

    fun sortedPlayers(): List<Player> {
        return sortedStats.mapNotNull { stat ->
            originalPlayers.find { it.username == stat.playerName }
        }
    }

    fun getStatFor(p: Player): PlayerStat? {
        return sortedStats.find { it.playerName == p.username }
    }

    fun getVisitedTilesPercent(p: Player): String {
        val stat = getStatFor(p) ?: return "0.00"
        return String.format(Locale.US, "%.2f", stat.visitedTileNumber)
    }

    fun getGameTilesVisited(): String {
        return String.format(Locale.US, "%.2f", gameStats.tilesVisitedNumber)
    }

    fun getGameToggledDoors(): String {
        return String.format(Locale.US,"%.2f", gameStats.doorNumber)
    }

    fun getGameTime(): String {
        val totalSeconds = (gameStats.gameTime / 1000).toInt()
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        return "${mins}:${secs.toString().padStart(2, '0')}"
    }

    // Init sort
    LaunchedEffect(playerStats) {
        sortedStats = playerStats
        sortTable("name")
    }

    // Navigate home if no winner
    LaunchedEffect(winner) {
        if (winner.isEmpty()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.leaveEndGame()
            controller.gameWinner.value = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(assets.backgroundCharacterPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Fin de la partie",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = assets.mainPageTextColor
                    )
                    Text(
                        "$winner a gagné",
                        fontSize = 24.sp,
                        color = assets.mainPageTextColor
                    )
                    val myUsername = controller.player.value?.username ?: ""
                    val myMoney = controller.moneyResults.value[myUsername]
                    if (myMoney != null) {
                        val isPlayerWinner = originalPlayers.any { it.username == myUsername && isWinner(it) }
                        val label = if (isPlayerWinner) "Victoire" else "Défaite"
                        val total = myMoney.baseMoney + myMoney.challengeReward + myMoney.entryFeeGain

                        Text(
                            "$label: ${myMoney.baseMoney}$",
                            fontSize = 20.sp,
                            color = assets.mainPageTextColor
                        )
                        if (myMoney.challengeReward > 0) {
                            Text(
                                "Défi accompli: +${myMoney.challengeReward}$",
                                fontSize = 20.sp,
                                color = assets.mainPageTextColor
                            )
                        }
                        if (myMoney.entryFeeGain > 0) {
                            Text(
                                "Lot remporté: +${myMoney.entryFeeGain}$",
                                fontSize = 20.sp,
                                color = assets.mainPageTextColor
                            )
                        }
                        Text(
                            "Total gagné: $total$",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Content: Tables + Chat
            Row(
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp)
            ) {
                // Tables column
                LazyColumn(
                    modifier = Modifier
                        .weight(0.65f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Player stats table
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        ) {
                            // Header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HeaderGreen)
                                    .padding(8.dp)
                            ) {
                                TableHeader("Nom du Joueur${showArrow("name")}", Modifier.weight(1.5f)) { sortTable("name") }
                                TableHeader("Combats${showArrow("fights")}", Modifier.weight(1f)) { sortTable("fights") }
                                TableHeader("Évasions${showArrow("evasions")}", Modifier.weight(1f)) { sortTable("evasions") }
                                TableHeader("Victoires${showArrow("wins")}", Modifier.weight(1f)) { sortTable("wins") }
                                TableHeader("Défaites${showArrow("loses")}", Modifier.weight(1f)) { sortTable("loses") }
                                TableHeader("PV Perdus${showArrow("lifeLost")}", Modifier.weight(1f)) { sortTable("lifeLost") }
                                TableHeader("PV Infligés${showArrow("damageDealt")}", Modifier.weight(1f)) { sortTable("damageDealt") }
                                TableHeader("Objets Ramassés${showArrow("items")}", Modifier.weight(1f)) { sortTable("items") }
                                TableHeader("Tuiles Visitées (%) ${showArrow("tiles")}", Modifier.weight(1f)) { sortTable("tiles") }
                            }

                            // Data rows
                            sortedPlayers().forEachIndexed { index, p ->
                                val stat = getStatFor(p)
                                val bgColor = if (index % 2 == 0) Color.White else EvenRowGray
                                val nameColor = if (isCTF && p.team == 1) Team1Red
                                else if (isCTF && p.team == 2) Team2Blue
                                else Color.Black

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor)
                                        .padding(8.dp)
                                ) {
                                    Text(getPlayerName(p), modifier = Modifier.weight(1.5f), fontSize = 14.sp, color = nameColor, fontWeight = FontWeight.Bold)
                                    TableCell("${stat?.fightNumber ?: 0}", Modifier.weight(1f))
                                    TableCell("${stat?.evasionNumber ?: 0}", Modifier.weight(1f))
                                    TableCell("${stat?.winNumber ?: 0}", Modifier.weight(1f))
                                    TableCell("${stat?.losesNumber ?: 0}", Modifier.weight(1f))
                                    TableCell("${stat?.lifeLost ?: 0}", Modifier.weight(1f))
                                    TableCell("${stat?.damageDealt ?: 0}", Modifier.weight(1f))
                                    TableCell("${stat?.itemPicked?.size ?: 0}", Modifier.weight(1f))
                                    TableCell("${getVisitedTilesPercent(p)} %", Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Game stats table
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HeaderGreen)
                                    .padding(8.dp)
                            ) {
                                TableHeader("Temps de la partie", Modifier.weight(1f)) {}
                                TableHeader("Tuiles Visitées par au moins un joueur (%)", Modifier.weight(1f)) {}
                                TableHeader("Portes ayant été activées au moins une fois (%)", Modifier.weight(1f)) {}
                                if (isCTF) {
                                    TableHeader("Nombre de joueur différent qui ont tenu le drapeau", Modifier.weight(1f)) {}
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                TableCell(getGameTime(), Modifier.weight(1f))
                                TableCell("${getGameTilesVisited()} %", Modifier.weight(1f))
                                TableCell("${getGameToggledDoors()} %", Modifier.weight(1f))
                                if (isCTF) {
                                    TableCell("${gameStats.flagTaken.size}", Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeader(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TableCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(4.dp),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}
