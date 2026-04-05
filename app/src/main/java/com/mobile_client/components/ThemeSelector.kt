package com.mobile_client.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mobile_client.utils.AppTheme
import com.mobile_client.viewModels.ThemeViewModel

@Composable
fun ThemeSelector(themeViewModel: ThemeViewModel) {
    val themes = listOf(
        AppTheme.CARROTS to "🥕 Carottes",
        AppTheme.AUBERGINES to "🍆 Aubergines",
    )

    Row (
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        themes.forEach { (theme, label) ->
            val isSelected = themeViewModel.currentTheme.value == theme

            Button(
                onClick = { themeViewModel.setTheme(theme) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (themeViewModel.currentTheme.value == theme)
                        themeViewModel.assets.backButtonColor
                    else Color.Gray,
                    contentColor = if (isSelected)
                        Color.Black
                    else
                        Color.White
                )
            ) {
                Text(label)
            }
        }
    }
}
