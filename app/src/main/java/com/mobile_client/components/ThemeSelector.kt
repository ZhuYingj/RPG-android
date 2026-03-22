package com.mobile_client.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mobile_client.utils.AppTheme
import com.mobile_client.viewModels.ThemeViewModel

@Composable
fun ThemeSelector(themeViewModel: ThemeViewModel) {
    val themes = listOf(
        AppTheme.CARROTS to "🥕 Carottes",
        AppTheme.AUBERGINES to "🍆 Aubergines",
    )

    Row {
        themes.forEach { (theme, label) ->
            Button(
                onClick = { themeViewModel.setTheme(theme) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (themeViewModel.currentTheme.value == theme)
                        themeViewModel.assets.backButtonColor
                    else Color.Gray
                )
            ) {
                Text(label)
            }
        }
    }
}
