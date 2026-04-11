package com.mobile_client.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobile_client.components.PressableButton
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.services.AccountService
import com.mobile_client.services.HttpService
import com.mobile_client.utils.FontSize
import com.mobile_client.utils.LoginResponse
import com.mobile_client.utils.Screen
import com.mobile_client.utils.Validation.MAX_PASSWORD_LENGTH
import com.mobile_client.utils.Validation.MAX_USERNAME_LENGTH
import com.mobile_client.utils.showDismissible
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
    var passwordVisible by remember { mutableStateOf(false) }
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
                        println("logindata.account = " + loginData.account)
                        AccountService.instance.setAccount(loginData.account, loginData.token)
                        snackbarHostState.showDismissible(scope, "Connexion réussie")
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
                errorMessage = "Erreur réseau: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.main_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.imePadding()
                    .width(450.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 40.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Connexion",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = FontSize.TITLE.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column {
                    Text(
                        text = "Nom d'utilisateur",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = FontSize.SUBTITLE.sp
                    )
                    OutlinedTextField(
                        value = username,
                        textStyle = TextStyle(fontSize = FontSize.BODY.sp),
                        onValueChange = {
                            val filtered = it.filterNot { c -> c.isWhitespace() }
                            if (filtered.length <= MAX_USERNAME_LENGTH) username = filtered
                        },
                        singleLine = true,
                        modifier = Modifier
                            .width(350.dp)
                            .padding(bottom = 16.dp)
                    )
                }

                Column {
                    Text(
                        text = "Mot de passe",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = FontSize.SUBTITLE.sp
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            if (it.length <= MAX_PASSWORD_LENGTH) password = it
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = FontSize.BODY.sp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Masquer" else "Afficher"
                                )
                            }
                        },
                        modifier = Modifier
                            .width(350.dp)
                            .padding(bottom = 8.dp)
                    )
                }

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.width(350.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PressableButton(
                        shadowColor = Color.LightGray,
                        cornerRadius = 5.dp,
                        shadowTopInset = 2.dp
                    ) { interactionSource, pressModifier ->
                        Button(
                            onClick = { navController.navigate(Screen.SignUp.route) },
                            modifier = pressModifier.height(50.dp).width(165.dp),
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Créer un compte", fontSize = FontSize.SUBTITLE.sp)
                        }
                    }

                    PressableButton(
                        shadowColor = Color(0xFF357abd),
                        cornerRadius = 5.dp,
                        enabled = username.isNotBlank() && password.isNotBlank(),
                        shadowTopInset = 2.dp
                    ) { interactionSource, pressModifier ->
                        Button(
                            onClick = { loginValidate() },
                            enabled = username.isNotBlank() && password.isNotBlank(),
                            modifier = pressModifier.height(50.dp).width(165.dp),
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF357abd),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.DarkGray
                            )
                        ) {
                            Text("Connexion", fontSize = FontSize.SUBTITLE.sp)
                        }
                    }
                }
            }
        }
    }
}
