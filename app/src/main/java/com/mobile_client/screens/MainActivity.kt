package com.mobile_client.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobile_client.screens.ui.theme.MobileclientTheme
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.ChatViewModel
import com.mobile_client.viewModels.CurrentGamesViewModel
import com.mobile_client.viewModels.GameListViewModel
import com.mobile_client.viewModels.GameLobbyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileclientTheme {
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val chatViewModel: ChatViewModel = viewModel()
                val gameListViewModel: GameListViewModel = viewModel()
                val currentGamesViewModel: CurrentGamesViewModel = viewModel()
                val gameLobbyViewModel: GameLobbyViewModel = viewModel()
                Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(navController = navController, snackbarHostState = snackbarHostState, chatViewModel = chatViewModel)}

                        composable(Screen.SignUp.route) { SignUpScreen(navController = navController)}
                        composable(Screen.Home.route) { HomeScreen(navController = navController, chatViewModel = chatViewModel)}
                        composable(Screen.GameCreation.route) { GamesCreationScreen(navController = navController, chatViewModel = chatViewModel, gameListViewModel = gameListViewModel, gameLobbyViewModel = gameLobbyViewModel)}
                        composable(Screen.JoinGame.route) { JoinGameScreen(navController = navController, snackbarHostState = snackbarHostState, chatViewModel = chatViewModel, gameListViewModel = currentGamesViewModel, gameLobbyViewModel = gameLobbyViewModel)}
                        composable(Screen.Account.route) { AccountScreen(navController = navController, chatViewModel = chatViewModel)}
                        composable(Screen.CharacterCreation.route) { CharacterCreationScreen(navController = navController, snackbarHostState = snackbarHostState, chatViewModel = chatViewModel, gameLobbyViewModel = gameLobbyViewModel)}
                        composable(Screen.WaitingPage.route) { WaitingPageScreen(navController = navController, chatViewModel = chatViewModel, snackbarHostState = snackbarHostState, gameLobbyViewModel = gameLobbyViewModel)}
                    }
                }
            }
        }
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val activity = LocalActivity.current
    DisposableEffect(orientation) {
        val activity = activity ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}
