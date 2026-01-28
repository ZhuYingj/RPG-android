package com.mobile_client.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.services.HttpService
import io.ktor.http.*
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.mobile_client.services.AccountRepository
import com.mobile_client.utils.LoginResponse
import io.ktor.client.call.body

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val httpService = HttpService()

    fun loginValidate() {
        scope.launch {
            errorMessage = null
            try {
                val body = mapOf(
                    "username" to username,
                    "password" to password
                )

                val response = httpService.post("$ENVIRONMENT/api/auth/login", body)
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val loginData: LoginResponse = response.body()
                        AccountRepository.setAccount(loginData.account, loginData.token)
                        navController.navigate(Screen.Home.route)
                    }
                    HttpStatusCode.BadRequest -> {
                        errorMessage = "Invalid credentials"
                    }
                    else -> {
                        errorMessage = "Login failed. Please try again."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.padding(bottom = 16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.padding(bottom = 16.dp),
            singleLine = true
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { loginValidate() },
            enabled = username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.height(40.dp).width(180.dp)
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate(Screen.SignUp.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("Sign up")
        }

    }
}
