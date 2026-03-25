package com.mobile_client.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.services.AccountService
import com.mobile_client.services.SocketService
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.GameLobbyViewModel
import com.mobile_client.viewModels.ThemeViewModel
import kotlinx.coroutines.launch

data class HomeButton(val label: String, val action: () -> Unit)

@Composable
fun HomeScreen(navController: NavController, gameLobbyViewModel: GameLobbyViewModel, themeViewModel: ThemeViewModel) {
    BackHandler() { }
    val navBackStackEntry = navController.currentBackStackEntryAsState().value

    LaunchedEffect(navBackStackEntry) {
        println("cleaned sockets")
        SocketService.instance.closeLobbyListeners()
        SocketService.instance.closeGameListeners()
        gameLobbyViewModel.isGameStarted.value = false
        gameLobbyViewModel.isSubmitted.value = false
    }

    val account = AccountService.instance.accountInfo
    val avatarBitmap = ImageUtils.base64ToBitmap(account?.avatar)
    val scope = rememberCoroutineScope()
    val money = AccountService.instance.money.value
    val assets = themeViewModel.assets

    val buttons = listOf(
        HomeButton("Joindre une partie") { navController.navigate(Screen.JoinGame.route) },
        HomeButton("Créer une partie") { navController.navigate(Screen.GameCreation.route) },
        HomeButton("Classement") { navController.navigate(Screen.LeaderBoard.route) },
        HomeButton("Se déconnecter") {
            scope.launch {
                if (AccountService.instance.logout()) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        },
    )

    LaunchedEffect(Unit) {
        AccountService.instance.fetchAccount()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(assets.backgroundMainPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = assets.title.uppercase(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF87CEEB),
                    letterSpacing = 5.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 100.dp)
                )

                buttons.forEachIndexed { index, button ->
                    val icon = if (index % 2 == 0) assets.button3 else assets.button4

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clickable { button.action() }
                    ) {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier
                                .width(300.dp)
                                .height(65.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(
                            text = button.label,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(assets.footerColor.copy(alpha = 0.75f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Équipe 102",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = assets.mainPageTextColor
                    )
                    Text(
                        text = "Antoine Beaunoyer, Alexie Lalonde-Legault, Tan Tommy Rin, Jeremy Vong, Song Ning Lan, Sheng He Ge",
                        fontSize = 12.sp,
                        color = assets.mainPageTextColor
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .border(1.dp, Color.White, CircleShape)
                    .clickable { navController.navigate(Screen.Inventory.route) { launchSingleTop = true } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INV",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .border(1.dp, Color.White, CircleShape)
                    .clickable { navController.navigate(Screen.Shop.route) { launchSingleTop = true } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Shop",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clickable { navController.navigate(Screen.Account.route) { launchSingleTop = true } },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$money $",
                    color = assets.mainPageTextColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = account?.username ?: "",
                    color = assets.mainPageTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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
                    } else {
                        Text(
                            text = account?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
