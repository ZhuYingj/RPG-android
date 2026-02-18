package com.mobile_client.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.components.Header
import com.mobile_client.services.ChatViewModel

@Composable
fun AccountScreen(navController: NavController, chatViewModel: ChatViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Header(navController, "Compte", chatViewModel)
        Text(
            text = "Account screen",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
