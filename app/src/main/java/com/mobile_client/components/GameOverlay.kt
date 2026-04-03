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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mobile_client.screens.R
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
            .fillMaxWidth(0.45f)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Combat", fontWeight = FontWeight.Bold, fontSize = 22.sp)


            Text(actionString, fontSize = 14.sp, color = Color.Green)


            if (player != null && opponent != null && activePlayer != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
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

                    Text(
                        "VS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )

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

            Spacer(modifier = Modifier.height(4.dp))

            // Action buttons
            Text("Actions de combat:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(145.dp)) {
                Button(
                    onClick = { fightService.attack() },
                    enabled = isMyTurn,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD740),
                        contentColor = Color.Black,
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp)
                ) {
                    Text("Attaquer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Button(
                    onClick = { fightService.evade() },
                    enabled = isMyTurn && (player?.evasionTry ?: 0) > 0,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD740),
                        contentColor = Color.Black,
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp)
                ) {
                    Text("Évasion", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Timer
            Row {
                Text(
                    "Temps restant: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    "$timerCounter sec",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFE53935)
                )
            }
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
    val isDefending = fighter.username == activePlayer.username
    val rawDice = if (isDefending) defenseDice else attackDice
    val roleName = if (isDefending) "défense" else "attaque"
    val totalResult = if (isDefending) {
        defenseDice + activePlayer.stats.defense
    } else {
        attackDice + otherPlayer.stats.attack
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(220.dp)
            .then(
                if (isActive) {
                    Modifier
                        .border(
                            width = 4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE53935),
                                    Color(0xFFFF9800),
                                    Color(0xFFFFD740)
                                )
                            ),
                            shape = RoundedCornerShape(1.dp)
                        )
                        .padding(14.dp)
                } else {
                    Modifier.padding(18.dp)
                }
            )
    ) {
        // Avatar with cosmetics
        Box {
            ImageResources.avatarToImage[fighter.avatar]?.let {
                Image(
                    painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    alpha = avatarAlpha
                )
            }
            for (cos in fighter.equippedItems) {
                ImageResources.cosmeticToImage[cos.filePath]?.let { resId ->
                    if (cos.type == 1) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "cosmétique",
                            modifier = Modifier
                                .size(64.dp)
                                .offset(y = (-20).dp)
                                .zIndex(2f)
                        )
                    } else if (cos.type == 2) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "cosmétique",
                            modifier = Modifier
                                .size(64.dp)
                                .offset(x = (20).dp)
                                .zIndex(2f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.life),
                contentDescription = "Vie",
                modifier = Modifier.size(36.dp)
            )

            Text(
                "${fighter.currentLife}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Tentatives d'évasion restantes: ${fighter.evasionTry}",
            fontSize = 14.sp,
            color = Color.Gray,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Résultat de dé: $rawDice", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text("Résultat de $roleName : ", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "$totalResult",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935)
            )
        }
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
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(24.dp),
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
