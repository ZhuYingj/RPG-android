package com.mobile_client.screens

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
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
import com.mobile_client.components.PressableButton
import com.mobile_client.services.AccountService
import com.mobile_client.utils.FontSize
import com.mobile_client.utils.LoginResponse
import com.mobile_client.viewModels.TutorialViewModel
import io.ktor.client.call.body
import java.io.File

@Composable
fun SignUpScreen(navController: NavController, tutorialViewModel: TutorialViewModel) {

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

    // Camera support
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarBase64FromCamera by remember { mutableStateOf<String?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            avatarUri = tempPhotoUri
            val bitmap = ImageUtils.uriToBitmap(context, tempPhotoUri!!)
            bitmap?.let {
                val resized = ImageUtils.resizeBitmap(it, 1024)
                avatarBase64FromCamera = ImageUtils.bitmapToBase64(resized, 70)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "signup_avatar_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val avatarBase64 by remember(selectedAvatarIndex, avatarBase64FromCamera) {
        mutableStateOf(
            if (avatarBase64FromCamera != null) {
                avatarBase64FromCamera!!
            } else {
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
                    "avatar" to avatarBase64
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
                            tutorialViewModel.showIfFirstTime()
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
                    .width(900.dp)
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
                    fontSize = FontSize.TITLE.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {

                    //LEFT
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            "Selectionnez un avatar ou prenez une photo",
                            fontSize = FontSize.BODY.sp,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            painter = rememberAsyncImagePainter(
                                if (avatarUri != null) avatarUri else avatarResources[selectedAvatarIndex]
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color(0xFF357abd), CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Camera button
                        TextButton(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Prendre une photo",
                                tint = Color(0xFF357abd),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Prendre une photo",
                                fontSize = FontSize.BUTTON.sp,
                                color = Color(0xFF357abd)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        for (row in 0..1) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                for (col in 0..3) {
                                    val index = row * 4 + col
                                    val isSelected = index == selectedAvatarIndex && avatarUri == null

                                    Image(
                                        painter = rememberAsyncImagePainter(avatarResources[index]),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .border(
                                                if (isSelected) 3.dp else 0.dp,
                                                if (isSelected) Color(0xFF357abd) else Color.Transparent,
                                                CircleShape
                                            )
                                            .clickable {
                                                selectedAvatarIndex = index
                                                avatarUri = null
                                                avatarBase64FromCamera = null
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    //RIGHT
                    Column(modifier = Modifier.weight(1f)) {

                        Text("Nom d'utilisateur", fontSize = FontSize.SUBTITLE.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = username,
                            singleLine = true,
                            textStyle = TextStyle(fontSize = FontSize.BODY.sp),
                            onValueChange = {
                                if (it.length <= Validation.MAX_USERNAME_LENGTH)
                                    username = it.filterNot { c -> c.isWhitespace() }
                                usernameError =
                                    if (!hasSubmitted && username.isBlank()) null
                                    else Validation.validateUsername(username)
                            },
                            isError = usernameError != null,
                            supportingText = {
                                usernameError?.let { Text(it, color = Color.Red) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Courriel", fontSize = FontSize.SUBTITLE.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = email,
                            singleLine = true,
                            textStyle = TextStyle(fontSize = FontSize.BODY.sp),
                            onValueChange = {
                                val filtered = it.filterNot { c -> c.isWhitespace() }
                                if (filtered.length <= Validation.MAX_EMAIL_LENGTH)
                                    email = filtered
                                emailError =
                                    if (!hasSubmitted && email.isBlank()) null
                                    else Validation.validateEmail(email)
                            },
                            isError = emailError != null,
                            supportingText = {
                                emailError?.let { Text(it, color = Color.Red) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Mot de passe", fontSize = FontSize.SUBTITLE.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = password,
                            singleLine = true,
                            textStyle = TextStyle(fontSize = FontSize.BODY.sp),
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
                            modifier = Modifier.fillMaxWidth(),
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

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            PressableButton(
                                shadowColor = Color.LightGray,
                                cornerRadius = 5.dp,
                                modifier = Modifier.weight(1f),
                                shadowTopInset = 2.dp
                            ) { interactionSource, pressModifier ->
                                Button(
                                    onClick = { navController.navigate(Screen.Login.route) },
                                    modifier = pressModifier.height(50.dp).fillMaxWidth(),
                                    interactionSource = interactionSource,
                                    shape = RoundedCornerShape(5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.LightGray,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Retour", fontSize = FontSize.SUBTITLE.sp)
                                }
                            }

                            val isSignUpEnabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank()

                            PressableButton(
                                shadowColor = Color(0xFF357abd),
                                cornerRadius = 5.dp,
                                enabled = isSignUpEnabled,
                                modifier = Modifier.weight(1f),
                                shadowTopInset = 2.dp
                            ) { interactionSource, pressModifier ->
                                Button(
                                    onClick = { handleSignup() },
                                    enabled = isSignUpEnabled,
                                    modifier = pressModifier.height(50.dp).fillMaxWidth(),
                                    interactionSource = interactionSource,
                                    shape = RoundedCornerShape(5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF357abd),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color.Gray,
                                        disabledContentColor = Color.DarkGray
                                    )
                                ) {
                                    Text("S'inscrire", fontSize = FontSize.SUBTITLE.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
