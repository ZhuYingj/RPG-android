package com.mobile_client.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.ThemeViewModel

@Composable
fun HomeButton(navController: NavController, modifier: Modifier = Modifier, themeViewModel: ThemeViewModel) {
    val assets = themeViewModel.assets

    PressableButton(modifier = modifier, shadowColor = assets.backButtonColor, shadowTopInset = 6.dp) { interactionSource, pressModifier ->
        OutlinedButton(
            onClick = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
            modifier = pressModifier,
            interactionSource = interactionSource,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = assets.textAccount,
                contentColor = assets.backButtonColor
            ),
            border = BorderStroke(1.dp, assets.backButtonColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Retour à l'accueil")
        }
    }
}
