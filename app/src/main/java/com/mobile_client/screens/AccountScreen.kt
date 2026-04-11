package com.mobile_client.screens

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.components.PressableButton
import com.mobile_client.components.ThemeSelector
import com.mobile_client.utils.AccountStats
import com.mobile_client.utils.FontSize
import com.mobile_client.utils.ImageResources.avatarResources
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Screen
import com.mobile_client.utils.Validation
import com.mobile_client.utils.showDismissible
import com.mobile_client.viewModels.AccountViewModel
import com.mobile_client.viewModels.ThemeViewModel
import com.mobile_client.viewModels.TutorialViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AccountScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    accountViewModel: AccountViewModel = viewModel(),
    themeViewModel: ThemeViewModel,
    tutorialViewModel: TutorialViewModel
) {
    val assets = themeViewModel.assets
    val account by accountViewModel.account.collectAsState()
    val snackbarMessage by accountViewModel.message.collectAsState()
    val showQrCode by accountViewModel.showQrCode.collectAsState()
    val qrBitmap by accountViewModel.qrBitmap.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember(account) { mutableStateOf(account?.username ?: "") }
    var email by remember(account) { mutableStateOf(account?.email ?: "") }
    val stats = account?.stats ?: AccountStats()
    var showDeleteDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarBase64 by remember { mutableStateOf<String?>(null) }

    var selectedDefaultResId by remember { mutableStateOf<Int?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    }

    val ownedAvatarCosmetics by accountViewModel.ownedAvatarCosmetics.collectAsState()


    val currentAvatarBitmap = remember(account?.avatar) {
        ImageUtils.base64ToBitmap(account?.avatar)
    }
    val currentBase64 = remember(account?.avatar) { account?.avatar }
    val hasAvatarChange = avatarBase64 != null && avatarBase64 != currentBase64
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            val bitmap = ImageUtils.uriToBitmap(context, tempPhotoUri!!)
            bitmap?.let {
                val resized = ImageUtils.resizeBitmap(it, 512)
                var quality = 70
                var base64 = ImageUtils.bitmapToBase64(resized, quality)

                while (base64.length > 500_000 && quality > 10) {
                    quality -= 10
                    base64 = ImageUtils.bitmapToBase64(resized, quality)
                }

                if (base64.length <= 500_000) {
                    avatarUri = tempPhotoUri
                    selectedDefaultResId = null
                    avatarBase64 = base64
                } else {
                    snackbarHostState.showDismissible(scope, "La photo est trop volumineuse, veuillez réessayer")
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            snackbarHostState.showDismissible(scope, "Permission caméra refusée")
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showDismissible(scope, it)
            accountViewModel.clearMessage()
        }
    }

    LaunchedEffect(currentAvatarBitmap) {
        if (currentAvatarBitmap != null) {
            avatarUri = null
            selectedDefaultResId = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(assets.backgroundAccount),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // === LEFT COLUMN ===
            Column(
                modifier = Modifier
                    .width(450.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(assets.boxAccount.copy(alpha = 0.7f))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(BorderStroke(3.dp, Color(0xFFE91E63)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedDefaultResId != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedDefaultResId),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            avatarUri != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(avatarUri),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            currentAvatarBitmap != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(currentAvatarBitmap),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Text(
                                    text = account?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PressableButton(
                            shadowColor = Color(0xFF2196F3),
                            cornerRadius = 8.dp,
                        ) { interactionSource, pressModifier ->
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                modifier = pressModifier,
                                interactionSource = interactionSource,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = assets.textAccount, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Photo", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
                            }
                        }

                        PressableButton(
                            shadowColor = assets.equipAlreadyBackground,
                            cornerRadius = 8.dp,
                        ) { interactionSource, pressModifier ->
                            OutlinedButton(
                                onClick = { showAvatarPicker = true },
                                modifier = pressModifier,
                                interactionSource = interactionSource,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = assets.equipAlreadyBackground,
                                    contentColor = assets.textAccount
                                ),
                                border = BorderStroke(1.dp, assets.buttonOutlineAccount),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Choisir un avatar", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
                            }
                        }

                        PressableButton(
                            shadowColor = assets.equipAlreadyBackground,
                            cornerRadius = 8.dp,
                        ) { interactionSource, pressModifier ->
                            OutlinedButton(
                                onClick = { accountViewModel.toggleQrCode() },
                                modifier = pressModifier,
                                interactionSource = interactionSource,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = assets.equipAlreadyBackground,
                                    contentColor = assets.textAccount
                                ),
                                border = BorderStroke(1.dp, assets.buttonOutlineAccount),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.QrCode, null, tint = assets.textAccount, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showQrCode) "Masquer QR" else "Montrer QR", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PressableButton(
                            shadowColor = assets.equipAlreadyBackground,
                            cornerRadius = 8.dp,
                            enabled = hasAvatarChange,
                        ) { interactionSource, pressModifier ->
                            OutlinedButton(
                                onClick = {
                                    avatarBase64 = null
                                    avatarUri = null
                                    selectedDefaultResId = null
                                },
                                modifier = pressModifier,
                                interactionSource = interactionSource,
                                enabled = hasAvatarChange,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = assets.equipAlreadyBackground,
                                    contentColor = assets.textAccount,
                                    disabledContainerColor = assets.equipAlreadyBackground.copy(alpha = 0.5f),
                                    disabledContentColor = assets.textAccount.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(1.dp, assets.buttonOutlineAccount),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Réinitialiser avatar", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
                            }
                        }

                        PressableButton(
                            shadowColor = if (hasAvatarChange) Color(0xFF2196F3) else assets.buttonBackgroundAccount,
                            cornerRadius = 8.dp,
                            enabled = hasAvatarChange,
                        ) { interactionSource, pressModifier ->
                            Button(
                                onClick = {
                                    avatarBase64?.let {
                                        accountViewModel.updateAccountAvatar(it)
                                        avatarBase64 = null
                                    }
                                },
                                modifier = pressModifier,
                                interactionSource = interactionSource,
                                enabled = hasAvatarChange,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF2196F3).copy(alpha = 0.5f),
                                    disabledContentColor = assets.textAccount.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Enregistrer avatar", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(assets.boxAccount.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Navigation",
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.SUBTITLE.sp,
                            color = assets.textAccount,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val navItems = listOf("Détails du compte", "Historique des actions", "Parties jouées")
                        val navColors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))

                        navItems.forEachIndexed { index, title ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTabIndex == index) assets.textAccount.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedTabIndex = index }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(navColors[index])
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    title,
                                    color = assets.textAccount,
                                    fontSize = FontSize.BODY.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Statistics section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(assets.boxAccount.copy(alpha = 0.6f))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Statistiques",
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.SUBTITLE.sp,
                            color = assets.textAccount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            textAlign = TextAlign.Start
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBlock("${stats.classicGamesPlayed}", "Parties classiques", assets.textAccount, Modifier.weight(1f))
                            StatBlock("${stats.CTFGamesPlayed}", "Parties CTF", assets.textAccount, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBlock("${stats.gamesWon}", "Victoires", assets.textAccount, Modifier.weight(1f))
                            StatBlock("${stats.challengesCompleted}", "Défis réussis", assets.textAccount, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        StatBlock(
                            "${stats.averageGameTime.roundToInt()} S",
                            "Durée moyenne par partie",
                            assets.textAccount,
                            Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // === RIGHT COLUMN ===
            Column(
                modifier = Modifier
                    .width(450.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(assets.boxAccount.copy(alpha = 0.7f))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // Theme
                        Text(
                            "Thème visuel",
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.SUBTITLE.sp,
                            color = assets.textAccount,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ThemeSelector(themeViewModel = themeViewModel)
                        }

                        HorizontalDivider(color = assets.textAccount.copy(alpha = 0.3f))
                        Text("Tutoriel",
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.SUBTITLE.sp,
                            color = assets.textAccount,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center)

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PressableButton(
                                    shadowColor = assets.equipAlreadyBackground,
                                    cornerRadius = 8.dp
                                ) { interactionSource, pressModifier ->
                                    OutlinedButton(
                                        onClick = {
                                            tutorialViewModel.continueFromSaved()
                                        },
                                        modifier = pressModifier,
                                        interactionSource = interactionSource,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = assets.equipAlreadyBackground,
                                            contentColor = assets.textAccount,
                                        ),
                                        border = BorderStroke(1.dp, assets.buttonOutlineAccount),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Continuer le tutoriel", color = assets.textAccount)
                                    }
                                }
                                PressableButton(
                                    shadowColor = Color(0xFF2196F3),
                                    cornerRadius = 8.dp
                                ) { interactionSource, pressModifier ->
                                    OutlinedButton(
                                        onClick = {
                                            tutorialViewModel.restart()
                                        },
                                        modifier = pressModifier,
                                        interactionSource = interactionSource,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2196F3),
                                            contentColor = Color.White,
                                        ),
                                        border = BorderStroke(1.dp, assets.buttonOutlineAccount),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Recommencer le tutoriel", color = assets.textAccount)
                                    }
                                }
                            }
                        }






                        HorizontalDivider(color = assets.textAccount.copy(alpha = 0.3f))

                        // Name & Email
                        val textFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = assets.textAccount,
                            unfocusedTextColor = assets.textAccount,
                            focusedBorderColor = assets.textAccount,
                            unfocusedBorderColor = assets.textAccount.copy(alpha = 0.5f),
                            cursorColor = assets.textAccount,
                            focusedLabelColor = assets.textAccount,
                            unfocusedLabelColor = assets.textAccount.copy(alpha = 0.7f),
                        )

                        Text("Nom d'utilisateur", fontSize = FontSize.BODY.sp, color = assets.textAccount)
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                val filtered = it.filterNot { c -> c.isWhitespace() }
                                if (filtered.length <= Validation.MAX_USERNAME_LENGTH) name = filtered
                                nameError = Validation.validateUsername(name)
                            },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = assets.textAccount) },
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it, color = Color.Red) } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors,
                            textStyle = TextStyle(fontSize = FontSize.BODY.sp),
                        )

                        Text("Adresse courriel", fontSize = FontSize.BODY.sp, color = assets.textAccount)
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                val filtered = it.filterNot { c -> c.isWhitespace() }
                                if (filtered.length <= Validation.MAX_EMAIL_LENGTH) email = filtered
                                emailError = Validation.validateEmail(email)
                            },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = assets.textAccount) },
                            isError = emailError != null,
                            supportingText = { emailError?.let { Text(it, color = Color.Red) } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors,
                            textStyle = TextStyle(fontSize = FontSize.BODY.sp),
                        )

                        // Save/Reset buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val hasFieldChanges = name != (account?.username ?: "") || email != (account?.email ?: "")
                            PressableButton(
                                shadowColor = assets.equipAlreadyBackground,
                                cornerRadius = 8.dp,
                                enabled = hasFieldChanges,
                                modifier = Modifier.weight(1f),
                            ) { interactionSource, pressModifier ->
                                OutlinedButton(
                                    onClick = {
                                        name = account?.username ?: ""
                                        email = account?.email ?: ""
                                        nameError = null
                                        emailError = null
                                    },
                                    modifier = pressModifier.fillMaxWidth(),
                                    interactionSource = interactionSource,
                                    enabled = hasFieldChanges,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = assets.equipAlreadyBackground,
                                        contentColor = assets.textAccount,
                                        disabledContainerColor = assets.equipAlreadyBackground.copy(alpha = 0.5f),
                                        disabledContentColor = assets.textAccount.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(1.dp, assets.buttonOutlineAccount),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Réinitialiser", color = assets.textAccount, fontSize = FontSize.BODY.sp)
                                }
                            }

                            PressableButton(
                                shadowColor = Color(0xFF2196F3),
                                cornerRadius = 8.dp,
                                modifier = Modifier.weight(1f),
                                enabled = hasFieldChanges && nameError == null && emailError == null,
                            ) { interactionSource, pressModifier ->
                                Button(
                                    onClick = { accountViewModel.updateAccount(name = name, email = email) },
                                    enabled = hasFieldChanges && nameError == null && emailError == null,
                                    modifier = pressModifier.fillMaxWidth(),
                                    interactionSource = interactionSource,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFF2196F3).copy(alpha = 0.5f),
                                        disabledContentColor = assets.textAccount.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Enregistrer", color = assets.textAccount, fontSize = FontSize.BODY.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delete button
                        PressableButton(
                            shadowColor = Color.Red,
                            cornerRadius = 8.dp,
                        ) { interactionSource, pressModifier ->
                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = pressModifier.fillMaxWidth(),
                                interactionSource = interactionSource,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Supprimer le compte", color = Color.White, fontSize = FontSize.BODY.sp)
                            }
                        }
                    }

                    1 -> AccountHistoryTab(account = account, dateFormat = dateFormat, themeViewModel)

                    2 -> MatchHistoryTab(account = account, dateFormat = dateFormat, themeViewModel)
                }
            }
        }

        // QR Code overlay
        if (showQrCode && qrBitmap != null) {
            Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "Code QR",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(8.dp)
            )
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "Supprimer le compte?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "Êtes-vous certain de supprimer votre compte? Cette action est irréversible.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    PressableButton(
                        shadowColor = Color(0xFF757575),
                        cornerRadius = 20.dp,
                    ) { interactionSource, pressModifier ->
                        Button(
                            onClick = { showDeleteDialog = false },
                            modifier = pressModifier,
                            interactionSource = interactionSource,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Non, garder mon compte", color = Color.White, fontSize = 15.sp)
                        }
                    }

                    PressableButton(
                        shadowColor = Color(0xFFE53935),
                        cornerRadius = 20.dp,
                    ) { interactionSource, pressModifier ->
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                accountViewModel.deleteAccount {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            modifier = pressModifier,
                            interactionSource = interactionSource,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Oui, supprimer mon compte", color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
            }
        )
    }

    // Avatar picker dialog
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            confirmButton = {},
            title = { Text("Choisir un avatar", fontSize = FontSize.BODY.sp) },
            text = {
                Column {
                    for (row in 0..2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (col in 0..3) {
                                val index = row * 4 + col
                                val resId = avatarResources[index]
                                val isLocked = index >= 8 && ownedAvatarCosmetics.none { it.filePath.contains("user-profile-avatar/default${index + 1}") }

                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(resId),
                                        contentDescription = "Avatar",
                                        colorFilter = if (isLocked) {
                                            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                                        } else null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(enabled = !isLocked) {
                                                val bitmap = BitmapFactory.decodeResource(context.resources, resId)
                                                val resized = ImageUtils.resizeBitmap(bitmap, 1024)
                                                avatarBase64 = ImageUtils.bitmapToBase64(resized)
                                                avatarUri = null
                                                selectedDefaultResId = resId
                                                showAvatarPicker = false
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                    if (isLocked) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.lock),
                                                contentDescription = "Locked",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        )
    }
}

@Composable
fun StatBlock(value: String, label: String, textColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = textColor)
        Text(label, fontSize = FontSize.BUTTON.sp, color = textColor, textAlign = TextAlign.Center)
    }
}
@Composable
fun AccountHistoryTab(
    account: com.mobile_client.utils.Account?,
    dateFormat: SimpleDateFormat,
    themeViewModel: ThemeViewModel
) {
    val assets = themeViewModel.assets
    val actionHistory = account?.actionHistory ?: emptyList()

    if (actionHistory.isEmpty()) {
        Text(
            "Aucune action enregistrée",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = FontSize.SUBTITLE.sp
        )
    } else {
        val sorted = remember(actionHistory) {
            actionHistory.sortedByDescending { it.timestamp }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 800.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sorted.size) { index ->
                val log = sorted[index]
                val isLogin = log.action.label.lowercase().contains("connexion") &&
                    !log.action.label.lowercase().contains("déconnexion")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(assets.boxAccount.copy(alpha = 0.25f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isLogin) "→]" else "[→",
                            color = if (isLogin) Color(134, 183, 251, 225) else Color(0xFFFF8C2E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            log.action.label,
                            fontWeight = FontWeight.Bold,
                            color = assets.textAccount,
                            fontSize = FontSize.BUTTON.sp
                        )
                        Text(
                            dateFormat.format(log.timestamp),
                            color = assets.textAccount.copy(alpha = 0.7f),
                            fontSize = FontSize.SMALL.sp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MatchHistoryTab(
    account: com.mobile_client.utils.Account?,
    dateFormat: SimpleDateFormat,
    themeViewModel: ThemeViewModel
) {
    val assets = themeViewModel.assets
    val matchHistory = account?.matchHistory ?: emptyList()

    if (matchHistory.isEmpty()) {
        Text(
            "Aucune partie enregistrée",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = FontSize.SUBTITLE.sp
        )
    } else {
        val sorted = remember(matchHistory) {
            matchHistory.sortedByDescending { it.startTime }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 800.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sorted.size) { index ->
                val match = sorted[index]
                val statusColor = when {
                    match.hasLeft -> Color(0xFFFF8C2E)
                    match.gameWon -> Color(0xFF4CAF50)
                    else -> Color(0xFFEF5350)
                }
                val statusText = when {
                    match.hasLeft -> "Abandonné"
                    match.gameWon -> "Victoire"
                    else -> "Défaite"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(assets.boxAccount.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (match.hasLeft) "[→" else if (match.gameWon) "✓" else "✖",
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                match.gameType.label,
                                fontWeight = FontWeight.Bold,
                                color = assets.textAccount,
                                fontSize = FontSize.BUTTON.sp
                            )
                            Text(
                                dateFormat.format(match.startTime),
                                color = assets.textAccount.copy(alpha = 0.7f),
                                fontSize = FontSize.SMALL.sp
                            )
                        }
                    }

                    Text(
                        statusText,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = FontSize.SMALL.sp
                    )
                }
            }
        }
    }
}
