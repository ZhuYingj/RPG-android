package com.mobile_client.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.mobile_client.components.ChatBox
import com.mobile_client.components.Header
import com.mobile_client.viewModels.ChatViewModel
@Composable
fun HomeScreen(navController: NavController, chatViewModel: ChatViewModel){

    BackHandler() { }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(navController = navController, title = "Accueil", chatViewModel, showBackButton = false, showLogoutButton = true)

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "LES CAROTTES",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                Button(onClick = {navController.navigate(Screen.JoinGame.route)},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier.padding(bottom=10.dp)) {
                    Text("Joindre une partie", color = Color.Black)
                }
                Button(onClick = {navController.navigate(Screen.GameCreation.route)},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier.padding(bottom=10.dp)) {
                    Text("Créer une partie", color = Color.Black)
                }
            }
        }
        ChatBox(
            modifier = Modifier
                .heightIn(max=400.dp)
                .align(Alignment.BottomEnd)
                .widthIn(max = 400.dp)
                .padding(16.dp)
                .zIndex(1f), chatViewModel)
    }
}

//@Preview(showBackground = true, device="spec:width=2000px,height=1200px, orientation=landscape")
//@Composable
//fun HomeScreenPreview() {
//    MobileclientTheme {
//        //LoginScreen(onNavigateToSignUp = {}, onNavigateToHome = {})
//        HomeScreen(navController = rememberNavController())
//        //SignUpScreen(navController = rememberNavController())
//    }
//}
