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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.services.AccountService
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeButton(val label: String, val action: () -> Unit)

@Composable
fun HomeScreen(navController: NavController) {
    BackHandler() { }

    val account = AccountService.instance.accountInfo
    val avatarBitmap = ImageUtils.base64ToBitmap(account?.avatar)
    val scope = rememberCoroutineScope()
    val money = AccountService.instance.money.value

    val buttons = listOf(
        HomeButton("Joindre une partie") { navController.navigate(Screen.JoinGame.route) },
        HomeButton("Créer une partie") { navController.navigate(Screen.GameCreation.route) },
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
        delay(1000)
        AccountService.instance.fetchAccount()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.main_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "LES CAROTTES",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                buttons.forEachIndexed { index, button ->
                    val icon = if (index % 2 == 0) R.drawable.carrot3 else R.drawable.carrot4

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
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .clickable { navController.navigate(Screen.Account.route) { launchSingleTop = true } },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$money $",
                color = Color.Black,
                fontSize = 14.sp
            )

            Text(
                text = account?.username ?: "",
                color = Color.Black,
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
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
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
