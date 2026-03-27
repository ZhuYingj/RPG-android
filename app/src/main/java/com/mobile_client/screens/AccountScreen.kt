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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    LaunchedEffect(account?.avatar) {
        avatarUri = null
        selectedDefaultResId = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(assets.backgroundMainPage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Single scrollable column for everything
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
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
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(Icons.Default.CameraAlt, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photo", color = Color.White)
                    }
                    OutlinedButton(onClick = { showAvatarPicker = true }) {
                        Text("Choisir un avatar", color = Color.White)
                    }
                    OutlinedButton(onClick = { accountViewModel.toggleQrCode() }, modifier = Modifier.width(170.dp)) {
                        Icon(Icons.Default.QrCode, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showQrCode) "Masquer QR" else "Montrer QR", color = Color.White)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            avatarBase64 = null
                            avatarUri = null
                            selectedDefaultResId = null
                        },
                        enabled = hasAvatarChange
                    ) { Text("Réinitialiser avatar", color = Color.White) }

                    Button(
                        onClick = {
                            avatarBase64?.let {
                                accountViewModel.updateAccountAvatar(it)
                                avatarBase64 = null
                            }
                        },
                        enabled = hasAvatarChange
                    ) { Text("Enregistrer avatar", color = Color.White) }
                }

                // In AccountScreen, after the theme selector section:
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Tutoriel", style = MaterialTheme.typography.titleMedium, color = Color.White)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {
                        tutorialViewModel.continueFromSaved()
                    }) {
                        Text("Continuer le tutoriel", color = Color.White)
                    }
                    OutlinedButton(onClick = {
                        tutorialViewModel.restart()
                    }) {
                        Text("Recommencer le tutoriel", color = Color.White)
                    }
                }

                // --- Stats Section ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Statistiques", style = MaterialTheme.typography.titleMedium, color = Color.White)

                Column(modifier = Modifier.fillMaxWidth()) {
                    StatRow("Parties classiques jouées", "${stats.classicGamesPlayed}", Color.White)
                    StatRow("Parties CTF jouées", "${stats.CTFGamesPlayed}", Color.White)
                    StatRow("Parties gagnées", "${stats.gamesWon}", Color.White)
                    StatRow("Temps moyen de partie", "${stats.averageGameTime.roundToInt()} s", Color.White)
                    //StatRow("Battailles gagnées", "${stats.battlesWins}", Color.White)
                }

                // --- Action History Section ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Historique d'actions", style = MaterialTheme.typography.titleMedium, color = Color.White)

                val actionHistory = account?.actionHistory ?: emptyList()
                if (actionHistory.isEmpty()) {
                    Text(
                        "Aucune action enregistrée",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
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
                                    log.action?.label ?: "Action inconnue",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Text(
                                    dateFormat.format(log.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // --- Match History Section ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Historique des parties", style = MaterialTheme.typography.titleMedium, color = Color.White)

                val matchHistory = account?.matchHistory ?: emptyList()
                if (matchHistory.isEmpty()) {
                    Text(
                        "Aucune partie enregistrée",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
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
                                        match.gameType?.label ?: "Type inconnu",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        dateFormat.format(match.startTime),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = when {
                                        match.hasLeft -> "Quitté"
                                        match.gameWon -> "Victoire"
                                        else -> "Défaite"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        match.hasLeft -> Color.Gray
                                        match.gameWon -> Color(0xFF4CAF50)
                                        else -> Color(0xFFEF5350)
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Thème visuel", style = MaterialTheme.typography.titleMedium, color = Color.White)

                ThemeSelector(themeViewModel = themeViewModel)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= Validation.MAX_USERNAME_LENGTH) name = it
                        nameError = Validation.validateUsername(name)
                    },
                    label = { Text("Nom") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.White) },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        if (it.length <= Validation.MAX_EMAIL_LENGTH) email = it
                        emailError = Validation.validateEmail(email)
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.White) },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            name = account?.username ?: ""
                            email = account?.email ?: ""
                            nameError = null
                            emailError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Réinitialiser", color = Color.White) }

                    Button(
                        onClick = { accountViewModel.updateAccount(name = name, email = email) },
                        enabled = nameError == null && emailError == null,
                        modifier = Modifier.weight(1f)
                    ) { Text("Enregistrer", color = Color.White) }
                }

                // --- Delete Account ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Supprimer le compte", color = Color.White)
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
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer votre compte? Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    accountViewModel.deleteAccount {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) { Text("Supprimer", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            confirmButton = {},
            title = { Text("Choisir un avatar") },
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
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🔒", style = MaterialTheme.typography.bodySmall)
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
fun StatRow(label: String, value: String, textColor: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = textColor)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
    }
}
