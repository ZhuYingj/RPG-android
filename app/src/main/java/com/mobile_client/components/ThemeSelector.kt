package com.mobile_client.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themes.forEach { (theme, label) ->
            val isSelected = themeViewModel.currentTheme.value == theme
            val buttonColor = if (isSelected) themeViewModel.assets.backButtonColor else Color.Gray

            PressableButton(
                shadowColor = buttonColor,
                cornerRadius = 6.dp,
            ) { interactionSource, pressModifier ->
                Button(
                    onClick = { themeViewModel.setTheme(theme) },
                    modifier = pressModifier,
                    interactionSource = interactionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = if (isSelected) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(label)
                }
            }
        }
    }
}
