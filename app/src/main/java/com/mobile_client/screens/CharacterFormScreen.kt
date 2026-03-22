package com.mobile_client.screens

import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobile_client.services.AccountService
import com.mobile_client.utils.BASE_STAT_VALUE
import com.mobile_client.utils.Dices
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import com.mobile_client.utils.PlayerTypes
import com.mobile_client.utils.Screen
import com.mobile_client.utils.Stats
import com.mobile_client.viewModels.GameLobbyViewModel
import com.mobile_client.viewModels.ThemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val IconDark = Color(0xFF3E2723)
private val SelectedGreen = Color(0xFF4CAF50)

@Composable
fun CharacterCreationScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    gameLobbyViewModel: GameLobbyViewModel,
    themeViewModel: ThemeViewModel,
) {
    val availableAvatars = gameLobbyViewModel.availableAvatars
    val allAvatars = PlayerAvatars.entries.filter { it != PlayerAvatars.None }
    val assets = themeViewModel.assets

    var selectedAvatar by remember { mutableStateOf(PlayerAvatars.None) }
    var previousAvatar by remember { mutableStateOf(PlayerAvatars.None) }
    var isBonusLife by remember { mutableStateOf<Boolean?>(false) }
    var attackDice by remember { mutableStateOf<Dices?>(Dices.D6) }

    val hp = if (isBonusLife == true) BASE_STAT_VALUE + 2 else BASE_STAT_VALUE
    val speed = if (isBonusLife == false) BASE_STAT_VALUE + 2 else BASE_STAT_VALUE

    val scope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        gameLobbyViewModel.errorMessage.collect { message ->
            scope.launch { snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short) }
        }
    }

    LaunchedEffect(gameLobbyViewModel.isGameStarted.value) {
        if (gameLobbyViewModel.isGameStarted.value) {
            navController.navigate(Screen.Game.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!gameLobbyViewModel.isSubmitted.value) {
                gameLobbyViewModel.selectAvatar(selectedAvatar, PlayerAvatars.None)
                gameLobbyViewModel.leaveLobby()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(assets.backgroundCharacterPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 80.dp, vertical = 4.dp)
                    .padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "CRÉATION DU PERSONNAGE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = assets.mainPageTextColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = AccountService.instance.username,
                    onValueChange = {},
                    singleLine = true,
                    enabled = false,
                    modifier = Modifier
                        .width(300.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left column - Avatar selection
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Choisissez un avatar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = assets.mainPageTextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(R.drawable.vector_cartoon),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.brown_soil),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                ) {
                                    items(allAvatars.size) { index ->
                                        val avatar = allAvatars[index]
                                        val isSelected = selectedAvatar == avatar
                                        val isAvailable = availableAvatars.contains(avatar)
                                        Box(
                                            modifier = Modifier
                                                .sizeIn(maxHeight = 110.dp, maxWidth = 110.dp)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .alpha(if (isAvailable) 1f else 0.3f)
                                                .clickable(enabled = isAvailable) {
                                                    previousAvatar = selectedAvatar
                                                    selectedAvatar = avatar
                                                    gameLobbyViewModel.selectAvatar(
                                                        previousAvatar,
                                                        avatar
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(
                                                        if (isSelected) 2.dp else 0.dp,
                                                        if (isSelected) SelectedGreen else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ImageResources.avatarToImage[avatar]?.let {
                                                    Image(
                                                        painter = painterResource(id = it),
                                                        contentDescription = avatar.name,
                                                        modifier = Modifier.size(90.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right column - Stats
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Choisissez un bonus et assignez vos dés",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = assets.mainPageTextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(R.drawable.brown_soil),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Attributs :", fontWeight = FontWeight.Bold, color = assets.mainPageTextColor, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                        StatIcon(R.drawable.heart, hp)
                                        StatIcon(R.drawable.bolt, speed)
                                        StatIcon(R.drawable.swords, BASE_STAT_VALUE)
                                        StatIcon(R.drawable.shield, BASE_STAT_VALUE)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Choisissez un bonus +2 :", fontWeight = FontWeight.Bold, color = assets.mainPageTextColor, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SelectableStatIcon(R.drawable.heart, isBonusLife == true) { isBonusLife = true }
                                        SelectableStatIcon(R.drawable.bolt, isBonusLife == false) { isBonusLife = false }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("D6 assigné à :", fontWeight = FontWeight.Bold, color = assets.mainPageTextColor, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SelectableStatIcon(R.drawable.swords, attackDice == Dices.D6) { attackDice = Dices.D6 }
                                        SelectableStatIcon(R.drawable.shield, attackDice == Dices.D4) { attackDice = Dices.D4 }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("D4 est assigné à :", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = assets.mainPageTextColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (attackDice != null) {
                                        Icon(
                                            painter = painterResource(
                                                if (attackDice == Dices.D6) R.drawable.shield else R.drawable.swords
                                            ),
                                            contentDescription = null,
                                            tint = IconDark,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            val stats = Stats(
                                                life = hp,
                                                speed = speed,
                                                attack = BASE_STAT_VALUE,
                                                defense = BASE_STAT_VALUE
                                            )
                                            val player = Player(
                                                username = AccountService.instance.username,
                                                avatar = selectedAvatar,
                                                playerType = if (gameLobbyViewModel.isHost.value) PlayerTypes.Host else PlayerTypes.Human,
                                                attack = attackDice ?: Dices.D6,
                                                defense = if (attackDice == Dices.D6) Dices.D4 else Dices.D6,
                                                isBonusLife = isBonusLife == true,
                                                stats = stats,
                                                hasAction = 0,
                                                isObserver = false
                                            )

                                            gameLobbyViewModel.addPlayer(player) {
                                                Handler(Looper.getMainLooper()).post {
                                                    navController.navigate(Screen.WaitingPage.route) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        },
                                        enabled = selectedAvatar != PlayerAvatars.None
                                            && isBonusLife != null
                                            && attackDice != null,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            disabledContainerColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, SelectedGreen)
                                    ) {
                                        Text("CRÉER", color = SelectedGreen, fontWeight = FontWeight.Bold)
                                    }
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
fun StatIcon(drawableRes: Int, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            tint = IconDark,
            modifier = Modifier.size(24.dp)
        )
        Text(" : $value", color = IconDark, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SelectableStatIcon(drawableRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) SelectedGreen else Color.Gray,
                RoundedCornerShape(6.dp)
            )
            .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            tint = IconDark,
            modifier = Modifier.size(28.dp)
        )
    }
}
