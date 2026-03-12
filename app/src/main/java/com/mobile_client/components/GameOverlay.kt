package com.mobile_client.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile_client.services.GameControllerService
import com.mobile_client.services.GameFightService
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Player
import com.mobile_client.utils.TileConstants

@Composable
fun BattleOverlay(fightService: GameFightService, timerCounter: Int) {
    val player = fightService.player.value
    val opponent = fightService.opposingPlayer.value
    val activePlayer = fightService.activePlayer.value
    val actionString = fightService.actionString.value
    val attackDice = fightService.attackDice.value
    val defenseDice = fightService.defenseDice.value

    val isMyTurn = player != null && activePlayer != null && player.username == activePlayer.username

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Combat!", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Text(actionString, fontSize = 14.sp, color = Color.Gray)

            if (player != null && opponent != null && activePlayer != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // --- Player (you) ---
                    FighterColumn(
                        fighter = player,
                        label = "${player.username} (toi)",
                        isActive = player.username == activePlayer.username,
                        attackDice = attackDice,
                        defenseDice = defenseDice,
                        activePlayer = activePlayer,
                        otherPlayer = opponent
                    )

                    Text("VS", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        modifier = Modifier.padding(top = 32.dp))

                    // --- Opponent ---
                    FighterColumn(
                        fighter = opponent,
                        label = opponent.username,
                        isActive = opponent.username == activePlayer.username,
                        attackDice = attackDice,
                        defenseDice = defenseDice,
                        activePlayer = activePlayer,
                        otherPlayer = player
                    )
                }
            }

            // Action buttons
            Text("Actions de combat:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { fightService.attack() },
                    enabled = isMyTurn
                ) {
                    Text("Attaquer")
                }
                Button(
                    onClick = { fightService.evade() },
                    enabled = isMyTurn && (player?.evasionTry ?: 0) > 0
                ) {
                    Text("Évasion")
                }
            }

            // Timer
            Text(
                "Temps restant: $timerCounter sec",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun FighterColumn(
    fighter: Player,
    label: String,
    isActive: Boolean,
    attackDice: Int,
    defenseDice: Int,
    activePlayer: Player,
    otherPlayer: Player
) {
    val avatarAlpha = if (isActive) 1f else 0.4f
    // If this fighter is the active player, they defend; otherwise they attack
    val isDefending = fighter.username == activePlayer.username
    val rawDice = if (isDefending) defenseDice else attackDice
    val roleName = if (isDefending) "de défense" else "d'attaque"
    val totalResult = if (isDefending) {
        defenseDice + activePlayer.stats.defense
    } else {
        attackDice + otherPlayer.stats.attack
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp)
    ) {
        ImageResources.avatarToImage[fighter.avatar]?.let {
            Image(
                painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                alpha = avatarAlpha
            )
        }
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text("Vie: ${fighter.currentLife}", fontSize = 12.sp)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Évasions restantes: ${fighter.evasionTry}",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text("Résultat de dé: $rawDice", fontSize = 11.sp)
        Text(
            "Résultat $roleName: $totalResult",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BetweenTurnOverlay(activePlayer: Player) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tour de:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ImageResources.avatarToImage[activePlayer.avatar]?.let {
                Image(painterResource(id = it), contentDescription = null, modifier = Modifier.size(64.dp))
            }
            Text(activePlayer.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ItemChoiceOverlay(controllerService: GameControllerService) {
    val player = controllerService.player.value ?: return
    val items = player.items
    var selectedIndex by remember { mutableStateOf(2) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Choisissez l'item que vous voulez rejeter:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .then(
                                if (isSelected) Modifier.border(2.dp, Color.Red, RoundedCornerShape(4.dp))
                                else Modifier.border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            )
                            .clickable { selectedIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (item != TileConstants.Items.None) {
                            ImageResources.itemToImage[item]?.let { resId ->
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = "Item",
                                    modifier = Modifier.fillMaxSize().padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    controllerService.changeObject(selectedIndex)
                    controllerService.isItemChoice.value = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFC92B2B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Confirmer votre choix",
                    color = Color(0xFFC92B2B)
                )
            }
        }
    }
}
