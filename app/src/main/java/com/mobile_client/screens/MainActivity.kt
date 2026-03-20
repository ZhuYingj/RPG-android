package com.mobile_client.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile_client.components.ChatBox
import com.mobile_client.components.FriendsPanel
import com.mobile_client.components.HomeButton
import com.mobile_client.screens.ui.theme.MobileclientTheme
import com.mobile_client.utils.Screen
import com.mobile_client.viewModels.ChatViewModel
import com.mobile_client.viewModels.CurrentGamesViewModel
import com.mobile_client.viewModels.FriendsViewModel
import com.mobile_client.viewModels.GameListViewModel
import com.mobile_client.viewModels.GameLobbyViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileclientTheme {
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val gameListViewModel: GameListViewModel = viewModel()
                val currentGamesViewModel: CurrentGamesViewModel = viewModel()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                val showChat = currentRoute != null && currentRoute !in listOf(Screen.Login.route, Screen.SignUp.route)
                val gameLobbyViewModel: GameLobbyViewModel = viewModel()
                val friendsViewModel: FriendsViewModel = viewModel()
                val showLobbyTab = currentRoute in listOf(Screen.WaitingPage.route, Screen.Game.route, Screen.EndGame.route)
                val hideBackButton = currentRoute in listOf(
                    Screen.Login.route,
                    Screen.SignUp.route,
                    Screen.Home.route,
                    Screen.Game.route
                )
                Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.navigationBarsPadding()) }, contentWindowInsets = WindowInsets(0)) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding).navigationBarsPadding().statusBarsPadding()) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Login.route,
                            modifier = Modifier.fillMaxSize().zIndex(0f)
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    navController = navController,
                                    snackbarHostState = snackbarHostState,
                                )
                            }
                            composable(Screen.SignUp.route) {
                                SignUpScreen(navController = navController)
                            }
                            composable(Screen.Home.route) {
                                HomeScreen(navController = navController, gameLobbyViewModel = gameLobbyViewModel)
                            }
                            composable(Screen.GameCreation.route) {
                                GamesCreationScreen(
                                    navController = navController,
                                    snackbarHostState = snackbarHostState,
                                    gameListViewModel = gameListViewModel,
                                    gameLobbyViewModel = gameLobbyViewModel
                                )
                            }
                            composable(Screen.JoinGame.route) {
                                JoinGameScreen(
                                    navController = navController,
                                    snackbarHostState = snackbarHostState,
                                    currentGameListViewModel = currentGamesViewModel,
                                    gameLobbyViewModel = gameLobbyViewModel
                                )
                            }
                            composable(Screen.Account.route) {
                                AccountScreen(
                                    navController = navController,
                                    snackbarHostState = snackbarHostState,
                                )
                            }
                            composable(Screen.CharacterCreation.route) {
                                CharacterCreationScreen(
                                    navController = navController,
                                    snackbarHostState = snackbarHostState,
                                    gameLobbyViewModel = gameLobbyViewModel
                                )
                            }
                            composable(Screen.WaitingPage.route) {
                                WaitingPageScreen(
                                    navController = navController,
                                    snackbarHostState = snackbarHostState,
                                    gameLobbyViewModel = gameLobbyViewModel
                                )
                            }
                            composable(Screen.Game.route) {
                                GameScreen(
                                    navController = navController,
                                    snackbarHostState = snackbarHostState,
                                    gameLobbyViewModel = gameLobbyViewModel
                                )
                            }
                            composable(Screen.EndGame.route) {
                                EndGameScreen(navController = navController)
                            }
                        }
                        if (!hideBackButton) {
                            HomeButton(
                                navController = navController,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .zIndex(1f)
                            )
                        }

                        val globalChatViewModel: ChatViewModel = viewModel()
                        val lobbyChatViewModel: ChatViewModel = viewModel(key = "lobbyChat")

                        LaunchedEffect(currentRoute) {
                            if (currentRoute == Screen.Login.route || currentRoute == Screen.SignUp.route) {
                                globalChatViewModel.clearList()
                                lobbyChatViewModel.clearList()
                            }
                        }
                        if (showChat) {
                            val lobbyCode = gameLobbyViewModel.lobbyCode.value


                            DisposableEffect(Unit) {
                                globalChatViewModel.enableListeners()
                                onDispose {
                                    //globalChatViewModel.disableListeners() // nettoyage du socket
                                }
                            }
                            LaunchedEffect(lobbyCode) {
                                if (lobbyCode == "") {
                                    lobbyChatViewModel.clearList()
                                }
                            }
                            ChatBox(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 0.dp
                                    )
                                    .zIndex(1f),
                                chatViewModel = globalChatViewModel,
                                lobbyChatViewModel = if (showLobbyTab) lobbyChatViewModel else null,
                                lobbyCode = lobbyCode,
                                friendsViewModel = friendsViewModel
                            )
                            FriendsPanel(
                                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                                friendsViewModel = friendsViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(99f)
                        )
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
