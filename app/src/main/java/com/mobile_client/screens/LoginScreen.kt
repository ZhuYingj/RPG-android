package com.mobile_client.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.services.AccountService
import com.mobile_client.services.HttpService
import com.mobile_client.utils.LoginResponse
import com.mobile_client.utils.Screen
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, snackbarHostState: SnackbarHostState) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val maxUsernameLength = 20
    val scope: CoroutineScope = rememberCoroutineScope()

    fun loginValidate() {
        scope.launch {
            errorMessage = null
            try {
                val body: Map<String, String> = mapOf(
                    "username" to username,
                    "password" to password
                )

                val response: HttpResponse = HttpService.instance.post("$ENVIRONMENT/api/auth/login", body)
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val loginData: LoginResponse = response.body()
                        AccountService.instance.setAccount(loginData.account, loginData.token)
                        scope.launch{ snackbarHostState.showSnackbar("Connexion réussie", duration = SnackbarDuration.Short) }
                        navController.navigate(Screen.Home.route)
                    }
                    HttpStatusCode.Unauthorized -> {
                        errorMessage = "Nom d'utilisateur ou mot de passe invalide"
                    }
                    HttpStatusCode.Forbidden -> {
                        errorMessage = "Ce compte est déjà connecté"
                    }
                    else -> {
                        errorMessage = "Une erreur s'est produite lors de la connexion"
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
            text = "Connexion",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                if(it.length <= maxUsernameLength)
                    username = it},
            label = { Text("Nom d'utilisateur") },
            modifier = Modifier.padding(bottom = 16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                if(it.length <= maxUsernameLength)
                    password = it},
            label = { Text("Mot de passe") },
            modifier = Modifier.padding(bottom = 16.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
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
            Text("Connexion")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate(Screen.SignUp.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("Créer un compte")
        }

    }
}
