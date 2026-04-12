package com.mobile_client.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.components.GameList
import com.mobile_client.components.PressableButton
import com.mobile_client.services.AccountService
import com.mobile_client.utils.FontSize
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Screen
import com.mobile_client.utils.launchQrScanner
import com.mobile_client.utils.showDismissible
import com.mobile_client.viewModels.CurrentGamesListViewModel
import com.mobile_client.viewModels.GameLobbyViewModel
import com.mobile_client.viewModels.ThemeViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun JoinGameScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    currentGameListViewModel: CurrentGamesListViewModel,
    gameLobbyViewModel: GameLobbyViewModel,
    themeViewModel: ThemeViewModel,
) {
    var codeDigits by remember { mutableStateOf(List(4) { "" }) }
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val scope: CoroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val assets = themeViewModel.assets
    val account = AccountService.instance.accountInfo
    val avatarBitmap = remember(account?.avatar) {
        ImageUtils.base64ToBitmap(account?.avatar)
    }
    val money = AccountService.instance.money.intValue

    fun joinWithCode(code: String) {
        gameLobbyViewModel.joinLobbyWithBlockCheck(
            code,
            { navController.navigate(Screen.CharacterCreation.route) },
            { message -> snackbarHostState.showDismissible(scope, message) }
        )
    }

    LaunchedEffect(gameLobbyViewModel.isGameStarted.value) {
        if (gameLobbyViewModel.isGameStarted.value) {
            navController.navigate(Screen.Game.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        currentGameListViewModel.lobbySelected.collect { lobbyCode ->
            joinWithCode(lobbyCode)
        }
    }

    LaunchedEffect(Unit) {
        gameLobbyViewModel.errorMessage.collect { message ->
            snackbarHostState.showDismissible(scope, message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(assets.backgroundMainPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp),
        ) {
            // Title
            Text(
                text = "Joindre une partie",
                fontSize = FontSize.TITLE.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF87CEEB),
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Subtitle
            Text(
                text = "Choisissez un jeu, Scanner un code QR ou Entrez le code de la partie",
                fontSize = FontSize.BODY.sp,
                fontWeight = FontWeight.Bold,
                color = assets.mainPageTextColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 12.dp)
            )

            // Code input row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            ) {
                // 4 themed code boxes
                for (i in 0..3) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(55.dp)
                    ) {
                        Image(
                            painter = painterResource(assets.backgroundBoxCode),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        BasicTextField(
                            value = codeDigits[i],
                            onValueChange = { newValue ->
                                val digit = newValue.filter { it.isDigit() }.takeLast(1)
                                codeDigits = codeDigits.toMutableList().also { it[i] = digit }
                                if (digit.isNotEmpty() && i < 3) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = FontSize.MENU_BUTTON.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 12.dp)
                                .focusRequester(focusRequesters[i])
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.key == Key.Backspace && codeDigits[i].isEmpty() && i > 0) {
                                        codeDigits = codeDigits.toMutableList().also { it[i - 1] = "" }
                                        focusRequesters[i - 1].requestFocus()
                                        true
                                    } else {
                                        false
                                    }
                                },
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.Center) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                // Join button
                val isJoinEnabled = codeDigits.all { it.isNotEmpty() }
                PressableButton(
                    shadowColor = Color.White,
                    enabled = isJoinEnabled,
                ) { interactionSource, pressModifier ->
                    OutlinedButton(
                        onClick = {
                            val code = codeDigits.joinToString("")
                            if (code.length == 4) joinWithCode(code)
                        },
                        modifier = pressModifier,
                        interactionSource = interactionSource,
                        enabled = isJoinEnabled,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = assets.joinButton,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f),
                        ),
                        border = BorderStroke(1.dp, if (isJoinEnabled) assets.joinButton else Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Rejoindre la partie", color = assets.joinButton, fontSize = FontSize.BODY.sp)
                    }
                }

                // Refresh button
                PressableButton(
                    shadowColor = Color.White,
                ) { interactionSource, pressModifier ->
                    OutlinedButton(
                        onClick = { currentGameListViewModel.loadMaps() },
                        modifier = pressModifier.size(40.dp),
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = assets.joinButton
                        ),
                        border = BorderStroke(1.dp, assets.joinButton),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("⟳", fontSize = FontSize.BODY.sp, color = assets.joinButton)
                    }
                }

                PressableButton(
                    shadowColor = assets.backButtonColor, shadowTopInset = 3.dp,
                ) { interactionSource, pressModifier ->
                    Button(
                        onClick = {
                            launchQrScanner(
                                context = context,
                                onResult = { code -> joinWithCode(code) },
                                onError = { message ->
                                    snackbarHostState.showDismissible(scope, message)
                                }
                            )
                        },
                        modifier = pressModifier,
                        interactionSource = interactionSource,
                        colors = ButtonDefaults.buttonColors(containerColor = assets.backButtonColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Scanner un code QR", color = Color.White, fontSize = FontSize.BODY.sp)
                    }
                }
            }

            // Game list container
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .background(
                        assets.gameListBackground,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                GameList(currentGameListViewModel, themeViewModel = themeViewModel)
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .background(assets.headerRightBackground, RoundedCornerShape(20.dp))
                .border(1.dp, assets.headerRightBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.shop_icon),
                contentDescription = "Shop",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { navController.navigate(Screen.Shop.route) { launchSingleTop = true } }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.inventory_icon),
                contentDescription = "Inventory",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { navController.navigate(Screen.Inventory.route) { launchSingleTop = true } }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .clickable { navController.navigate(Screen.Account.route) { launchSingleTop = true } },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$money$",
                    color = assets.mainPageTextColor,
                    fontSize = FontSize.BODY.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = account?.username ?: "",
                    color = assets.mainPageTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.BODY.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            painter = rememberAsyncImagePainter(avatarBitmap),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
