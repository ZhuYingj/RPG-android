package com.mobile_client.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.components.Header
import com.mobile_client.utils.Screen

@Composable
fun HomeScreen(navController: NavController){

    BackHandler() { }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(navController = navController, title = "Accueil", showBackButton = false, showLogoutButton = true)

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
