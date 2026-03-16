package com.mobile_client.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.services.HttpService
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Screen
import com.mobile_client.utils.Validation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import org.json.JSONObject
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.services.AccountService
import com.mobile_client.utils.AccountStats
import com.mobile_client.utils.LoginResponse
import io.ktor.client.call.body

@Composable
fun SignUpScreen(navController: NavController) {

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var hasSubmitted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val avatarResources = listOf(
        R.drawable.default1, R.drawable.default2, R.drawable.default3, R.drawable.default4,
        R.drawable.default5, R.drawable.default6, R.drawable.default7, R.drawable.default8
    )

    var selectedAvatarIndex by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    val avatarBase64 by remember(selectedAvatarIndex) {
        mutableStateOf(
            run {
                val bitmap = BitmapFactory.decodeResource(
                    context.resources,
                    avatarResources[selectedAvatarIndex]
                )
                val resized = ImageUtils.resizeBitmap(bitmap)
                ImageUtils.bitmapToBase64(resized)
            }
        )
    }

    fun validateClientSide(): Boolean {
        var isValid = true

        usernameError = Validation.validateUsername(username)
        if (usernameError != null) isValid = false

        emailError = Validation.validateEmail(email)
        if (emailError != null) isValid = false

        passwordError = Validation.validatePassword(password)
        if (passwordError != null) isValid = false

        return isValid
    }

    fun handleSignup() {
        hasSubmitted = true

        if (!validateClientSide()) return

        scope.launch {
            try {

                val body = mapOf(
                    "username" to username,
                    "email" to email,
                    "password" to password,
                    "avatar" to avatarBase64,
                    "stats" to AccountStats()
                )

                val response: HttpResponse =
                    HttpService.instance.post("$ENVIRONMENT/api/auth/signup", body)

                when (response.status) {

                    HttpStatusCode.Created,
                    HttpStatusCode.OK -> {
                        val loginBody = mapOf(
                            "username" to username,
                            "password" to password
                        )
                        val loginResponse: HttpResponse =
                            HttpService.instance.post("$ENVIRONMENT/api/auth/login", loginBody)
                        if (loginResponse.status == HttpStatusCode.OK) {
                            val loginData: LoginResponse = loginResponse.body()
                            AccountService.instance.setAccount(loginData.account, loginData.token)
                            navController.navigate(Screen.Home.route)
                        } else {
                            navController.navigate(Screen.Login.route)
                        }
                    }

                    HttpStatusCode.BadRequest -> {
                        val errorBody = response.bodyAsText()
                        val json = JSONObject(errorBody)
                        val message = json.getString("message")
                        val incorrectInput = message.substringBefore(" ").lowercase()

                        if (incorrectInput == "email")
                            emailError = "Email déjà utilisé"
                        else
                            usernameError = "Nom déjà pris"
                    }

                    else -> {
                        usernameError = "Erreur lors de l'inscription"
                    }
                }

            } catch (e: Exception) {
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
                modifier = Modifier
                    .width(650.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(
                        Color.White.copy(alpha = 0.95f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Créer un compte",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("Choisir un avatar")

                        Spacer(modifier = Modifier.height(12.dp))

                        Image(
                            painter = rememberAsyncImagePainter(avatarResources[selectedAvatarIndex]),
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color(0xFF357abd), CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        for (row in 0..1) {

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                for (col in 0..3) {

                                    val index = row * 4 + col
                                    val isSelected = index == selectedAvatarIndex

                                    Image(
                                        painter = rememberAsyncImagePainter(avatarResources[index]),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) Color(0xFF357abd) else Color.LightGray,
                                                CircleShape
                                            )
                                            .clickable {
                                                selectedAvatarIndex = index
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Column {

                        Text("Nom d'utilisateur")

                        OutlinedTextField(
                            value = username,
                            singleLine = true,
                            onValueChange = {
                                if (it.length <= Validation.MAX_USERNAME_LENGTH)
                                    username = it
                                usernameError =
                                    if (!hasSubmitted && username.isBlank()) null
                                    else Validation.validateUsername(username)
                            },
                            isError = usernameError != null,
                            supportingText = {
                                usernameError?.let { Text(it, color = Color.Red) }
                            },
                            modifier = Modifier.width(260.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Courriel")

                        OutlinedTextField(
                            value = email,
                            singleLine = true,
                            onValueChange = {
                                if (it.length <= Validation.MAX_EMAIL_LENGTH)
                                    email = it
                                emailError =
                                    if (!hasSubmitted && email.isBlank()) null
                                    else Validation.validateEmail(email)
                            },
                            isError = emailError != null,
                            supportingText = {
                                emailError?.let { Text(it, color = Color.Red) }
                            },
                            modifier = Modifier.width(260.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Mot de passe")

                        OutlinedTextField(
                            value = password,
                            singleLine = true,
                            onValueChange = {
                                if (it.length <= Validation.MAX_PASSWORD_LENGTH)
                                    password = it
                                passwordError =
                                    if (!hasSubmitted && password.isBlank()) null
                                    else Validation.validatePassword(password)
                            },
                            isError = passwordError != null,
                            supportingText = {
                                passwordError?.let { Text(it, color = Color.Red) }
                            },
                            modifier = Modifier.width(260.dp),
                            visualTransformation =
                                if (passwordVisible)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        passwordVisible = !passwordVisible
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            if (passwordVisible)
                                                Icons.Filled.Visibility
                                            else
                                                Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    Button(
                        onClick = {
                            navController.navigate(Screen.Login.route)
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .width(170.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Retour")
                    }

                    Button(
                        onClick = { handleSignup() },
                        enabled =
                            username.isNotBlank() &&
                                email.isNotBlank() &&
                                password.isNotBlank(),
                        modifier = Modifier
                            .height(50.dp)
                            .width(170.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF357abd),
                            contentColor = Color.White
                        )
                    ) {
                        Text("S'inscrire")
                    }
                }
            }
        }
    }
}
