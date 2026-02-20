package com.mobile_client.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.Screen
import com.mobile_client.screens.ui.theme.Pink80
import com.mobile_client.services.AccountRepository
import com.mobile_client.viewModels.ChatViewModel
import com.mobile_client.services.HttpService
import com.mobile_client.services.SocketManager
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch

@Composable
fun Header(
    navController: NavController,
    title: String,
    chatViewModel: ChatViewModel,
    showLogoutButton: Boolean = false,
    showBackButton: Boolean = true
) {
    val scope = rememberCoroutineScope()

    fun logout() {
        scope.launch {
            try {
                val body = emptyMap<String, String>()
                val response: HttpResponse = HttpService.post(
                    "$ENVIRONMENT/api/auth/logout",
                    body
                )
                if (response.status == HttpStatusCode.OK) {
                    SocketManager.disconnect()
                    AccountRepository.clear()
                    chatViewModel.clearList()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } catch (e: Exception) {
                println("Logout exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Pink80)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            HeaderButton("Retour à l'accueil") {
                navController.navigate(Screen.Home.route) { launchSingleTop = true }
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderButton("Compte") {
                navController.navigate(Screen.Account.route) { launchSingleTop = true }
            }
            if (showLogoutButton) {
                HeaderButton("Déconnexion") { logout() }
            }

        }
    }
}

@Composable
fun HeaderButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.85f),
            contentColor = Color(0xFFD291BC)
        ),
        border = BorderStroke(1.dp, Color(0xFFD291BC)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text)
    }
}
