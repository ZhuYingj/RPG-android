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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.components.ThemeSelector
import com.mobile_client.utils.AccountStats
import com.mobile_client.utils.ImageResources.avatarResources
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Screen
import com.mobile_client.utils.Validation
import com.mobile_client.viewModels.AccountViewModel
import com.mobile_client.viewModels.ThemeViewModel
import com.mobile_client.viewModels.TutorialViewModel
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.TextStyle
import com.mobile_client.utils.FontSize

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
    val tabTitles = listOf("Détails du compte", "Historique du compte", "Historique des parties jouées")

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    }

    val ownedAvatarCosmetics by accountViewModel.ownedAvatarCosmetics.collectAsState()

    val hasAvatarChange = avatarBase64 != null
    val currentAvatarBitmap = remember(account?.avatar) {
        ImageUtils.base64ToBitmap(account?.avatar)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            avatarUri = tempPhotoUri
            selectedDefaultResId = null
            val bitmap = ImageUtils.uriToBitmap(context, tempPhotoUri!!)
            bitmap?.let {
                val resized = ImageUtils.resizeBitmap(it, 1024)
                avatarBase64 = ImageUtils.bitmapToBase64(resized, 70)
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
            scope.launch { snackbarHostState.showSnackbar("Permission caméra refusée") }
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
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
            painter = painterResource(assets.backgroundMainPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 24.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    containerColor = assets.tabIndicatorColor,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color = assets.textAccount.copy(alpha = 0.85f)
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) assets.textAccount else assets.textAccount.copy(alpha = 0.3f),
                                    fontSize = if (selectedTabIndex == index) FontSize.BODY.sp else FontSize.BUTTON.sp
                                )
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(assets.boxAccount)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> AccountDetailsTab(
                            account = account,
                            name = name,
                            email = email,
                            nameError = nameError,
                            emailError = emailError,
                            hasAvatarChange = hasAvatarChange,
                            avatarUri = avatarUri,
                            selectedDefaultResId = selectedDefaultResId,
                            currentAvatarBitmap = currentAvatarBitmap,
                            showQrCode = showQrCode,
                            qrBitmap = qrBitmap,
                            stats = stats,
                            onNameChange = { newName ->
                                if (newName.length <= Validation.MAX_USERNAME_LENGTH) name = newName
                                nameError = Validation.validateUsername(name)
                            },
                            onEmailChange = { newEmail ->
                                if (newEmail.length <= Validation.MAX_EMAIL_LENGTH) email = newEmail
                                emailError = Validation.validateEmail(email)
                            },
                            onTakePhoto = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            onChooseAvatar = { showAvatarPicker = true },
                            onToggleQr = { accountViewModel.toggleQrCode() },
                            onResetAvatar = {
                                avatarBase64 = null
                                avatarUri = null
                                selectedDefaultResId = null
                            },
                            onSaveAvatar = {
                                avatarBase64?.let {
                                    accountViewModel.updateAccountAvatar(it)
                                    avatarBase64 = null
                                }
                            },
                            onResetFields = {
                                name = account?.username ?: ""
                                email = account?.email ?: ""
                                nameError = null
                                emailError = null
                            },
                            onSaveFields = { accountViewModel.updateAccount(name = name, email = email) },
                            onDeleteAccount = { showDeleteDialog = true },
                            onContinueTutorial = { tutorialViewModel.continueFromSaved() },
                            onRestartTutorial = { tutorialViewModel.restart() },
                            themeViewModel = themeViewModel
                        )
                        1 -> AccountHistoryTab(
                            account = account,
                            dateFormat = dateFormat,
                            themeViewModel
                        )
                        2 -> MatchHistoryTab(
                            account = account,
                            dateFormat = dateFormat,
                            themeViewModel
                        )
                    }
                }
            }
        }

        if (showQrCode && qrBitmap != null) {
            Image(
                bitmap = qrBitmap!!.asImageBitmap(),
                contentDescription = "QR Code",
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression", fontSize = FontSize.SUBTITLE.sp) },
            text = { Text("Êtes-vous sûr de vouloir supprimer votre compte? Cette action est irréversible.", fontSize = FontSize.SUBTITLE.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    accountViewModel.deleteAccount {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) { Text("Supprimer", color = Color.Red, fontSize = FontSize.BODY.sp) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler", fontSize = FontSize.BODY.sp) }
            }
        )
    }

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

// ==================== TAB 1: Détails du compte ====================

@Composable
private fun AccountDetailsTab(
    account: com.mobile_client.utils.Account?,
    name: String,
    email: String,
    nameError: String?,
    emailError: String?,
    hasAvatarChange: Boolean,
    avatarUri: Uri?,
    selectedDefaultResId: Int?,
    currentAvatarBitmap: android.graphics.Bitmap?,
    showQrCode: Boolean,
    qrBitmap: android.graphics.Bitmap?,
    stats: AccountStats,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onTakePhoto: () -> Unit,
    onChooseAvatar: () -> Unit,
    onToggleQr: () -> Unit,
    onResetAvatar: () -> Unit,
    onSaveAvatar: () -> Unit,
    onResetFields: () -> Unit,
    onSaveFields: () -> Unit,
    onDeleteAccount: () -> Unit,
    onContinueTutorial: () -> Unit,
    onRestartTutorial: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val assets = themeViewModel.assets
    // Avatar display
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape),
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
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = FontSize.BODY.sp
                )
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onTakePhoto,
            colors = ButtonDefaults.buttonColors(
                containerColor = assets.buttonBackgroundAccount,
                contentColor = assets.textAccount
            )) {
            Icon(Icons.Default.CameraAlt, null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Photo", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
        }
        OutlinedButton(onClick = onChooseAvatar,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = assets.buttonBackgroundAccount,
                contentColor = assets.textAccount
            ),
            border = BorderStroke(1.dp, assets.buttonOutlineAccount)) {
            Text("Choisir un avatar", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
        }
        OutlinedButton(onClick = onToggleQr, modifier = Modifier.width(170.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = assets.buttonBackgroundAccount,
                contentColor = assets.textAccount
            ),
            border = BorderStroke(1.dp, assets.buttonOutlineAccount)) {
            Icon(Icons.Default.QrCode, null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showQrCode) "Masquer QR" else "Montrer QR", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onResetAvatar,
            enabled = hasAvatarChange,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = assets.buttonBackgroundAccount,
                contentColor = assets.textAccount
            ),
            border = BorderStroke(1.dp, assets.buttonOutlineAccount)
        ) { Text("Réinitialiser avatar", color = assets.textAccount, fontSize = FontSize.BUTTON.sp) }

        Button(
            onClick = onSaveAvatar,
            enabled = hasAvatarChange,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasAvatarChange) {
                    assets.buttonBackgroundAccount.copy(alpha = 0.9f) // whiter
                } else {
                    assets.buttonBackgroundAccount
                },
                contentColor = assets.textAccount
            )
        ) { Text("Enregistrer avatar", color = assets.textAccount, fontSize = FontSize.BUTTON.sp) }
    }

    // Tutorial
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Text("Tutoriel", style = MaterialTheme.typography.titleMedium, color = assets.textAccount, fontSize = FontSize.BODY.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onContinueTutorial,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = assets.buttonBackgroundAccount,
                contentColor = assets.textAccount
            ),
            border = BorderStroke(1.dp, assets.buttonOutlineAccount)
            ) {
            Text("Continuer le tutoriel", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
        }
        OutlinedButton(
            onClick = onRestartTutorial,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = assets.buttonBackgroundAccount,
                contentColor = assets.textAccount
            ),
            border = BorderStroke(1.dp, assets.buttonOutlineAccount)
        ) {
            Text("Recommencer le tutoriel", color = assets.textAccount, fontSize = FontSize.BUTTON.sp)
        }
    }

    // Stats
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Text("Statistiques", style = MaterialTheme.typography.titleMedium, color = assets.textAccount, fontSize = FontSize.BODY.sp)
    Column(modifier = Modifier.fillMaxWidth()) {
        StatRow("Parties classiques jouées", "${stats.classicGamesPlayed}", assets.textAccount)
        StatRow("Parties CTF jouées", "${stats.CTFGamesPlayed}", assets.textAccount)
        StatRow("Parties gagnées", "${stats.gamesWon}", assets.textAccount)
        StatRow("Temps moyen de partie", "${stats.averageGameTime.roundToInt()} s", assets.textAccount)
        StatRow("Défis accomplis", "${stats.challengesCompleted}", assets.textAccount)
    }

    // Theme
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Text("Thème visuel", style = MaterialTheme.typography.titleMedium, color = assets.textAccount, fontSize = FontSize.BODY.sp)
    ThemeSelector(themeViewModel = themeViewModel)

    // Name & Email fields
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = assets.textAccount,
        unfocusedTextColor = assets.textAccount,
        focusedBorderColor = assets.textAccount,
        unfocusedBorderColor = assets.textAccount.copy(alpha = 0.5f),
        cursorColor = assets.textAccount,
        focusedLabelColor = assets.textAccount,
        unfocusedLabelColor = assets.textAccount.copy(alpha = 0.7f),
    )

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nom") },
        leadingIcon = { Icon(Icons.Default.Person, null, tint = assets.textAccount) },
        isError = nameError != null,
        supportingText = { nameError?.let { Text(it, color = Color.Red) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = textFieldColors,
        textStyle = TextStyle(fontSize = FontSize.BODY.sp),
    )

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, null, tint = assets.textAccount) },
        isError = emailError != null,
        supportingText = { emailError?.let { Text(it, color = Color.Red) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = textFieldColors,
        textStyle = TextStyle(fontSize = FontSize.BODY.sp),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onResetFields,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = assets.buttonBackgroundAccount,
                contentColor = assets.textAccount
            ),
            border = BorderStroke(1.dp, assets.buttonOutlineAccount)
        ) { Text("Réinitialiser", color = assets.textAccount, fontSize = FontSize.BODY.sp) }

        Button(
            onClick = onSaveFields,
            enabled = nameError == null && emailError == null,
            modifier = Modifier.weight(1f)
        ) { Text("Enregistrer", color = assets.textAccount, fontSize = FontSize.BODY.sp) }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Button(
        onClick = onDeleteAccount,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Supprimer le compte", color = assets.textAccount, fontSize = FontSize.BODY.sp)
    }
}

// ==================== TAB 2: Historique du compte ====================

@Composable
private fun AccountHistoryTab(
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
        Column(modifier = Modifier.fillMaxWidth()) {
            for (log in actionHistory.sortedByDescending { it.timestamp }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        log.action.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = assets.textAccount,
                        fontSize = FontSize.BUTTON.sp
                    )
                    Text(
                        dateFormat.format(log.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = assets.textAccount.copy(alpha = 0.7f),
                        fontSize = FontSize.BUTTON.sp
                    )
                }
            }
        }
    }
}

// ==================== TAB 3: Historique des parties jouées ====================

@Composable
private fun MatchHistoryTab(
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
        Column(modifier = Modifier.fillMaxWidth()) {
            for (match in matchHistory.sortedByDescending { it.startTime }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            match.gameType.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = assets.textAccount,
                            fontSize = FontSize.BUTTON.sp
                        )
                        Text(
                            dateFormat.format(match.startTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = FontSize.BUTTON.sp
                        )
                    }
                    Text(
                        text = when {
                            match.hasLeft -> "Vous avez abandonné la partie"
                            match.gameWon -> "Victoire"
                            else -> "Défaite"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            match.hasLeft -> Color(0xFFEF5350)
                            match.gameWon -> Color(0xFF4CAF50)
                            else -> Color(0xFFEF5350)
                        },
                        fontSize = FontSize.BUTTON.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, textColor: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = textColor, fontSize = FontSize.BUTTON.sp)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, fontSize = FontSize.BUTTON.sp)
    }
}
