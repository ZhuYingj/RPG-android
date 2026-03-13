package com.mobile_client.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.utils.Screen

@Composable
fun HomeButton(navController: NavController, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.85f),
            contentColor = Color(0xFFD291BC)
        ),
        border = BorderStroke(1.dp, Color(0xFFD291BC)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Retour à l'accueil")
    }
}
