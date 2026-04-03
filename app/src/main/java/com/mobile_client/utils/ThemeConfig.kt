package com.mobile_client.utils

import androidx.compose.ui.graphics.Color
import com.mobile_client.screens.R

enum class AppTheme {
    CARROTS,
    AUBERGINES
}

data class ThemeAssets(
    val title: String,
    val button3: Int,
    val button4: Int,
    val backgroundLeaderboardPage: Int,
    val backgroundMainPage: Int,
    val backgroundCreationPage: Int,
    val backgroundWaitingPage: Int,
    val backgroundCharacterPage: Int,
    val backgroundBoxCode: Int,
    val backgroundInventory: Int,
    val footerColor: Color,
    val userListBackgroundColor: Color,
    val userBackgroundColor: Color,
    val backButtonColor: Color,
    val backButtonColorHover: Color,
    val mainPageTextColor: Color,
    val tabIndicatorColor: Color,
)

val themeAssetsMap = mapOf(
    AppTheme.CARROTS to ThemeAssets(
        title = "Les Carottes",
        button3 = R.drawable.carrot3,
        button4 = R.drawable.carrot4,
        backgroundLeaderboardPage = R.drawable.leaderboard_background,
        backgroundMainPage = R.drawable.main_page,
        backgroundCreationPage = R.drawable.farm,
        backgroundWaitingPage = R.drawable.main_page,
        backgroundCharacterPage = R.drawable.grass_backround,
        backgroundBoxCode = R.drawable.tomato,
        backgroundInventory = R.drawable.inventory,
        footerColor = Color(243, 169, 193),
        userListBackgroundColor = Color(54, 206, 100),
        userBackgroundColor = Color(23, 143, 63),
        backButtonColor = Color(224, 140, 210),
        backButtonColorHover = Color(0xFFD48AB8),
        mainPageTextColor = Color.Black,
        tabIndicatorColor = Color(0xFFFF8C2E)
    ),
    AppTheme.AUBERGINES to ThemeAssets(
        title = "Les Aubergines",
        button3 = R.drawable.eggplant3,
        button4 = R.drawable.eggplant4,
        backgroundLeaderboardPage = R.drawable.second_leaderboard_background,
        backgroundMainPage = R.drawable.second_main_page,
        backgroundCreationPage = R.drawable.second_creation_page,
        backgroundWaitingPage = R.drawable.second_main_page,
        backgroundCharacterPage = R.drawable.second_grass_background,
        backgroundBoxCode = R.drawable.mushroom,
        backgroundInventory = R.drawable.second_inventory,
        footerColor = Color(127, 154, 198),
        userListBackgroundColor = Color(255, 255, 255, 90),
        userBackgroundColor = Color.White,
        backButtonColor = Color(113, 181, 207),
        backButtonColorHover = Color(84, 172, 207),
        mainPageTextColor = Color.White,
        tabIndicatorColor = Color(0xFF7B2FBE)
    ),
)
