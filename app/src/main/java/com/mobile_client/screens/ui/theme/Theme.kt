package com.mobile_client.screens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MobileclientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            val scheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            scheme.copy(
                inversePrimary = Color.Yellow
            )
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun DefaultFontWrapper(content: @Composable () -> Unit) {
    val defaultTypography = Typography(
        displayLarge = TextStyle(fontFamily = FontFamily.Default),
        displayMedium = TextStyle(fontFamily = FontFamily.Default),
        displaySmall = TextStyle(fontFamily = FontFamily.Default),
        headlineLarge = TextStyle(fontFamily = FontFamily.Default),
        headlineMedium = TextStyle(fontFamily = FontFamily.Default),
        headlineSmall = TextStyle(fontFamily = FontFamily.Default),
        titleLarge = TextStyle(fontFamily = FontFamily.Default),
        titleMedium = TextStyle(fontFamily = FontFamily.Default),
        titleSmall = TextStyle(fontFamily = FontFamily.Default),
        bodyLarge = TextStyle(fontFamily = FontFamily.Default),
        bodyMedium = TextStyle(fontFamily = FontFamily.Default),
        bodySmall = TextStyle(fontFamily = FontFamily.Default),
        labelLarge = TextStyle(fontFamily = FontFamily.Default),
        labelMedium = TextStyle(fontFamily = FontFamily.Default),
        labelSmall = TextStyle(fontFamily = FontFamily.Default),
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = defaultTypography,
        content = content
    )
}
