package com.mobile_client.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mobile_client.utils.AppTheme
import com.mobile_client.utils.ThemeAssets
import com.mobile_client.utils.themeAssetsMap

class ThemeViewModel : ViewModel() {
    var currentTheme = mutableStateOf(AppTheme.CARROTS)
        private set

    val assets: ThemeAssets
        get() = themeAssetsMap[currentTheme.value]!!

    fun setTheme(theme: AppTheme) {
        currentTheme.value = theme
    }

    fun resetTheme() {
        currentTheme.value = AppTheme.CARROTS
    }
}
