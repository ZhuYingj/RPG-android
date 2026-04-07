package com.mobile_client.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun SnackbarHostState.showDismissible(
    scope: CoroutineScope,
    message: String
) {
    scope.launch {
        showSnackbar(message, actionLabel = "Fermer", duration = SnackbarDuration.Short)
    }
}
