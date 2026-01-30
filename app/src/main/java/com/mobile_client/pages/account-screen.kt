package com.mobile_client.pages

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AccountScreen(navController: NavController) {
    Text(
        text = "Account screen",
        style = MaterialTheme.typography.headlineMedium
    )
}
