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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.mobile_client.components.PressableButton
import com.mobile_client.services.AccountService
import com.mobile_client.services.CosmeticService
import com.mobile_client.utils.BASE_STAT_VALUE
import com.mobile_client.utils.Dices
import com.mobile_client.utils.FontSize
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Player
import com.mobile_client.utils.PlayerAvatars
import com.mobile_client.utils.PlayerTypes
import com.mobile_client.utils.Screen
import com.mobile_client.utils.Stats
import com.mobile_client.utils.premiumAvatarFilePaths
import com.mobile_client.utils.showDismissible
import com.mobile_client.viewModels.GameLobbyViewModel
import com.mobile_client.viewModels.ThemeViewModel
import kotlinx.coroutines.CoroutineScope

private val IconDark = Color(0xFF3E2723)

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

    // Track which premium avatar filePaths the player owns
    var ownedPremiumAvatarPaths by remember { mutableStateOf<Set<String>>(emptySet()) }

    val hp = if (isBonusLife == true) BASE_STAT_VALUE + 2 else BASE_STAT_VALUE
    val speed = if (isBonusLife == false) BASE_STAT_VALUE + 2 else BASE_STAT_VALUE

    val scope: CoroutineScope = rememberCoroutineScope()

    // Load inventory to determine which premium avatars are owned
    LaunchedEffect(Unit) {
        val cosmeticService = CosmeticService.instance
        val inventory = cosmeticService.loadInventory()
        val shop = cosmeticService.loadShop()

        // Build a map from cosmeticId -> filePath using shop + equipped data
        val cosmeticIdToFilePath = mutableMapOf<String, String>()
        shop.forEach { cosmeticIdToFilePath[it._id] = it.filePath }
        inventory.equipped.forEach { cosmeticIdToFilePath[it._id] = it.filePath }

        // Collect filePaths of all cosmetics the player owns (inventory + equipped)
        val ownedPaths = mutableSetOf<String>()
        inventory.inventory.forEach { item ->
            cosmeticIdToFilePath[item.cosmeticId]?.let { ownedPaths.add(it) }
        }
        inventory.equipped.forEach { ownedPaths.add(it.filePath) }
        ownedPremiumAvatarPaths = ownedPaths

        selectedAvatar = gameLobbyViewModel.autoSelectAvatar(ownedPremiumAvatarPaths)
        if (selectedAvatar == PlayerAvatars.None)
            snackbarHostState.showDismissible(scope, "Aucun avatar disponible pour l'instant")
    }

    LaunchedEffect(Unit) {
        gameLobbyViewModel.errorMessage.collect { message ->
            snackbarHostState.showDismissible(scope, message)
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
                    .padding(top = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "CRÉATION DU PERSONNAGE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.TITLE.sp,
                    color = assets.mainPageTextColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Username displayed in red without white background (matching web version)
                Text(
                    text = AccountService.instance.username,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.SMALLER_TITLE.sp,
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
                            fontSize = FontSize.SMALLER_TITLE.sp,
                            color = assets.mainPageTextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(485.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.vector_cartoon),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxSize()
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
                                    userScrollEnabled = false,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                ) {
                                    items(allAvatars.size) { index ->
                                        val avatar = allAvatars[index]
                                        val isSelected = selectedAvatar == avatar
                                        val isNotPickedByOthers = availableAvatars.contains(avatar)

                                        // Premium avatars (13-16) require ownership
                                        val isPremium = premiumAvatarFilePaths.containsKey(avatar)
                                        val isOwned = if (isPremium) {
                                            val filePath = premiumAvatarFilePaths[avatar]
                                            filePath != null && ownedPremiumAvatarPaths.contains(filePath)
                                        } else {
                                            true
                                        }

                                        val isAvailable = isNotPickedByOthers && isOwned

                                        Box(
                                            modifier = Modifier
                                                .sizeIn(maxHeight = 105.dp, maxWidth = 105.dp)
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
                                                    .size(94.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(
                                                        if (isSelected) 2.dp else 0.dp,
                                                        if (isSelected) assets.characterFormOutline else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ImageResources.avatarToImage[avatar]?.let {
                                                    Image(
                                                        painter = painterResource(id = it),
                                                        contentDescription = avatar.name,
                                                        modifier = Modifier.size(90.dp),
                                                        colorFilter = if (isPremium && !isOwned) {
                                                            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                                                        } else null
                                                    )
                                                }
                                                // Show lock icon on premium avatars the player doesn't own
                                                if (isPremium && !isOwned) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.lock),
                                                        contentDescription = "Locked",
                                                        modifier = Modifier.size(30.dp)
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(45.dp))
                        Text(
                            "Choisissez un bonus et assignez vos dés",
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.SMALLER_TITLE.sp,
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
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column {
                                    Text("Attributs :", fontWeight = FontWeight.Bold, color = assets.mainPageTextColor, fontSize = FontSize.MENU_BUTTON.sp,)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        StatIcon(R.drawable.heart, hp, themeViewModel)
                                        StatIcon(R.drawable.bolt, speed, themeViewModel)
                                        StatIcon(R.drawable.swords, BASE_STAT_VALUE, themeViewModel)
                                        StatIcon(R.drawable.shield, BASE_STAT_VALUE, themeViewModel)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Choisissez un bonus +2 :", fontWeight = FontWeight.Bold, color = assets.mainPageTextColor, fontSize = FontSize.MENU_BUTTON.sp,)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
                                            SelectableStatIcon(R.drawable.heart, isBonusLife == true, themeViewModel = themeViewModel) { isBonusLife = true }
                                            SelectableStatIcon(R.drawable.bolt, isBonusLife == false, themeViewModel = themeViewModel) { isBonusLife = false }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("D6 assigné à :", fontWeight = FontWeight.Bold, color = assets.mainPageTextColor, fontSize = FontSize.MENU_BUTTON.sp,)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
                                            SelectableStatIcon(R.drawable.swords, attackDice == Dices.D6, themeViewModel = themeViewModel) { attackDice = Dices.D6 }
                                            SelectableStatIcon(R.drawable.shield, attackDice == Dices.D4, themeViewModel = themeViewModel) { attackDice = Dices.D4 }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("D4 est assigné à :", fontWeight = FontWeight.Bold, fontSize = FontSize.MENU_BUTTON.sp, color = assets.mainPageTextColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
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
                                }
                                val isEnabled = selectedAvatar != PlayerAvatars.None
                                PressableButton(
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    shadowColor = assets.characterCreateButton,
                                    enabled = isEnabled,
                                    shadowTopInset = 6.dp,
                                ) { interactionSource, pressModifier ->
                                    Button(
                                        modifier = pressModifier,
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
                                                isObserver = false,
                                                equippedItems = listOf()
                                            )

                                            gameLobbyViewModel.addPlayer(player) {
                                                Handler(Looper.getMainLooper()).post {
                                                    navController.navigate(Screen.WaitingPage.route) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        },
                                        interactionSource = interactionSource,
                                        enabled = selectedAvatar != PlayerAvatars.None
                                            && isBonusLife != null
                                            && attackDice != null,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = assets.textAccount,
                                            disabledContainerColor = Color.White.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, assets.characterCreateButton)
                                    ) {
                                        Text("CRÉER", color = assets.characterCreateButton, fontWeight = FontWeight.Bold, fontSize = FontSize.MENU_BUTTON.sp)
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
fun StatIcon(drawableRes: Int, value: Int, themeViewModel: ThemeViewModel) {
    val assets = themeViewModel.assets
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            tint = assets.mainPageTextColor,
            modifier = Modifier.size(36.dp)
        )
        Text(" : $value", color = assets.mainPageTextColor, fontWeight = FontWeight.Bold, fontSize = FontSize.MENU_BUTTON.sp,)
    }
}

@Composable
fun SelectableStatIcon(drawableRes: Int, isSelected: Boolean, themeViewModel: ThemeViewModel, onClick: () -> Unit) {
    val assets = themeViewModel.assets
    Box(
        modifier = Modifier
            .size(55.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) assets.statsCharacterOutline else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            tint = assets.mainPageTextColor,
            modifier = Modifier.size(36.dp)
        )
    }
}
