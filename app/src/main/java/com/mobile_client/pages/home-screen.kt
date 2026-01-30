package com.mobile_client.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mobile_client.components.ChatBox
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.pages.ui.theme.MobileclientTheme
import com.mobile_client.pages.ui.theme.Pink80
import com.mobile_client.services.AccountRepository
import com.mobile_client.services.HttpService
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController){

    val scope = rememberCoroutineScope()
    //val httpService = HttpService()
    fun logout() {
        scope.launch {
            try {
                val body = mapOf(
                    "token" to AccountRepository.getToken()
                )
                val response: HttpResponse = HttpService.post("$ENVIRONMENT/api/auth/logout", body)
                if (response.status == HttpStatusCode.OK) {
                    AccountRepository.clear()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true } // Clear all previous screens, remove destination and avoid duplicate login screen
                        launchSingleTop = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .padding(bottom = 10.dp)
                .fillMaxWidth()
                .background(color = Pink80),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { logout() }, modifier = Modifier) {
                    Text("Logout")
                }
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineLarge)
                Button(onClick= { navController.navigate(Screen.Account.route) }, modifier = Modifier) {
                    Text("Account")
                }
            }

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
                    Text("Join Game", color = Color.Black)
                }
                Button(onClick = {navController.navigate(Screen.Games.route)},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier.padding(bottom=10.dp)) {
                    Text("Games", color = Color.Black)
                }
            }
        }
        ChatBox(
            modifier = Modifier
                .heightIn(max=400.dp)
                .align(Alignment.BottomEnd)
                .widthIn(max = 400.dp)
                .padding(16.dp)
                .zIndex(1f))
    }
}

@Preview(showBackground = true, device="spec:width=2000px,height=1200px, orientation=landscape")
@Composable
fun HomeScreenPreview() {
    MobileclientTheme {
        //LoginScreen(onNavigateToSignUp = {}, onNavigateToHome = {})
        HomeScreen(navController = rememberNavController())
        //SignUpScreen(navController = rememberNavController())
    }
}
