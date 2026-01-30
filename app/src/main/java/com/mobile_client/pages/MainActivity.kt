package com.mobile_client.pages

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobile_client.pages.ui.theme.MobileclientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileclientTheme {
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) {
//                            if(navController.currentDestination?.route != Screen.Login.route && navController.currentDestination?.route != Screen.SignUp.route)
//                                ChatBox(viewModel<ChatViewModel>(), modifier = Modifier.zIndex(1f))
                            //HomeScreen(navController= navController)
                            LoginScreen(navController = navController, snackbarHostState = snackbarHostState)
                            //tanrin
                            //12345
                            }

                        composable(Screen.SignUp.route) { SignUpScreen(navController = navController)}

                        composable(Screen.Home.route) { HomeScreen(navController = navController)}
                        composable(Screen.Games.route) { GamesScreen(navController = navController)}
                        composable(Screen.JoinGame.route) { JoinGameScreen(navController = navController)}
                    }
                }
            }
        }
    }
}
/*
@Preview(showBackground = true, device="spec:width=2000px,height=1200px, orientation=landscape")
@Composable
fun GreetingPreview() {
    MobileclientTheme {
        LoginScreen(navController = rememberNavController())
    }
}*/

@Composable
fun LockScreenOrientation(orientation: Int) {
    val activity = LocalActivity.current
    DisposableEffect(orientation) {
        val activity = activity ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}
