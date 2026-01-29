package com.mobile_client.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.unit.sp
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.pages.ui.theme.Pink80
import com.mobile_client.services.HttpService
import org.json.JSONObject
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch


@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var hasSubmitted by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val httpService = HttpService()

    val emailRegex = "^[^\\s@]+@[^\\s@]+\\.[a-zA-Z]{2,}$".toRegex()

    fun validateClientSide(): Boolean {
        var isValid = true

        usernameError = null
        if (username.isBlank() && hasSubmitted) {
            usernameError = "Nom d'utilisateur requis"
            isValid = false
        }

        emailError = null

        if (!emailRegex.matches(email)) {
            emailError = "Format d'email invalide"
            isValid = false
        }

        passwordError = null
        if (!password.contains(Regex("[0-9]")) || password.length < 5) {
            passwordError = "Minimum de 5 caractères incluant un chiffre"
            isValid = false
        }
        return isValid
    }

    fun handleSignup() {
        hasSubmitted = true

        if (!validateClientSide()) {
            return
        }

        scope.launch {

            try {
                val body = mapOf(
                    "username" to username,
                    "email" to email,
                    "password" to password
                )

                val response = httpService.post("$ENVIRONMENT/api/auth/signup", body)

                when (response.status) {
                    HttpStatusCode.Created, HttpStatusCode.OK -> {
                        navController.navigate(Screen.Login.route)
                    }
                    HttpStatusCode.BadRequest -> {
                        val errorBody = response.bodyAsText()
                        val json = JSONObject(errorBody)
                        val message = json.getString("message")
                        val incorrectInput = message.substringBefore(" ").lowercase()

                        if (incorrectInput == "email") emailError = "Email is already taken"
                        else {usernameError = "Username is already taken"}

                    }
                    else -> {
                        usernameError = "Signup failed. Please try again" // To change for clearer error quand on aura le temps
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(600.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ).padding(bottom = 50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Créer un compte",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp, top = 48.dp),
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column() {
                Text(
                    text = "Nom d'utilisateur",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 25.sp
                )
                OutlinedTextField(
                    value = username,
                    singleLine = true,
                    onValueChange = {
                        username = it
                        usernameError = null
                        if (username.isBlank() && hasSubmitted) {
                            usernameError = "Nom d'utilisateur requis"
                        }
                    },
                    isError = usernameError != null,
                    supportingText = {
                        usernameError?.let { Text(it, color = Color.Red) }
                    },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .width(450.dp)
                )
            }

            Column() {
                Text(
                    text = "Courriel",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 25.sp
                )
                OutlinedTextField(
                    value = email,
                    singleLine = true,
                    onValueChange = {
                        email = it
                        emailError = null
                        if (hasSubmitted && email.isBlank() && !emailRegex.matches(email)) {
                            emailError = "Format d'email invalide"
                        }
                    },
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let { Text(it, color = Color.Red) }
                    },
                    modifier = Modifier.padding(bottom = 16.dp).width(450.dp)
                )
            }

            Column(modifier = Modifier.padding(bottom = 50.dp)) {
                Text(
                    text = "Mot de passe",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 25.sp
                )
                OutlinedTextField(
                    value = password,
                    singleLine = true,
                    onValueChange = {
                        password = it
                        passwordError = null
                        if (password.isBlank() && hasSubmitted) {
                            passwordError = "Minimum de 5 caractères incluant un chiffre"
                        }
                    },
                    isError = passwordError != null,
                    supportingText = {
                        passwordError?.let { Text(it, color = Color.Red) }
                    },
                    modifier = Modifier.padding(bottom = 16.dp).width(450.dp)
                )
            }

            Row(
                modifier = Modifier.width(450.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val modifierButton = Modifier.height(60.dp).width(215.dp).background(color = Pink80)

                Button(
                    onClick = { navController.navigate(Screen.Login.route) }, modifier = modifierButton, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Retour")
                }

                Button(
                    onClick = { handleSignup() },
                    enabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                    modifier = modifierButton, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0XFF357abd),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.DarkGray
                    )
                ) {
                    Text("S'inscrire")
                }
            }
        }
    }
}



