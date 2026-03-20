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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Validation
import com.mobile_client.viewModels.AccountViewModel
import kotlinx.coroutines.launch
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.mobile_client.utils.AccountStats
import com.mobile_client.utils.Screen
import java.io.File
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun AccountScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    accountViewModel: AccountViewModel = viewModel()
) {

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

    val avatarResources = listOf(
        R.drawable.default1, R.drawable.default2, R.drawable.default3, R.drawable.default4,
        R.drawable.default5, R.drawable.default6, R.drawable.default7, R.drawable.default8
    )

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
                    .padding(horizontal = 24.dp),
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
                        Text("Photo")
                    }
                    OutlinedButton(onClick = { showAvatarPicker = true }) {
                        Text("Choisir un avatar")
                    }
                    OutlinedButton(onClick = { accountViewModel.toggleQrCode() }, modifier = Modifier.width(170.dp)) {
                        Icon(Icons.Default.QrCode, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showQrCode) "Masquer QR" else "Montrer QR")
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
                    ) { Text("Réinitialiser avatar") }

                    Button(
                        onClick = {
                            avatarBase64?.let {
                                accountViewModel.updateAccountAvatar(it)
                                avatarBase64 = null
                            }
                        },
                        enabled = hasAvatarChange
                    ) { Text("Enregistrer avatar") }
                }

                // --- Stats Section ---
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Statistiques", style = MaterialTheme.typography.titleMedium)

                Column(modifier = Modifier.fillMaxWidth()) {
                    StatRow("Parties classiques jouées", "${stats.classicGamesPlayed}")
                    StatRow("Parties CTF jouées", "${stats.CTFGamesPlayed}")
                    StatRow("Parties gagnées", "${stats.gamesWon}")
                    StatRow("Temps moyen de partie", String.format("%.1f s", stats.averageGameTime))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= Validation.MAX_USERNAME_LENGTH) name = it
                        nameError = Validation.validateUsername(name)
                    },
                    label = { Text("Nom") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        if (it.length <= Validation.MAX_EMAIL_LENGTH) email = it
                        emailError = Validation.validateEmail(email)
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                    ) { Text("Réinitialiser") }

                    Button(
                        onClick = { accountViewModel.updateAccount(name = name, email = email) },
                        enabled = nameError == null && emailError == null,
                        modifier = Modifier.weight(1f)
                    ) { Text("Enregistrer") }
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
                    for (row in 0..1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (col in 0..3) {
                                val index = row * 4 + col
                                Image(
                                    painter = rememberAsyncImagePainter(avatarResources[index]),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val bitmap = BitmapFactory.decodeResource(
                                                context.resources, avatarResources[index]
                                            )
                                            val resized = ImageUtils.resizeBitmap(bitmap, 1024)
                                            avatarBase64 = ImageUtils.bitmapToBase64(resized)
                                            avatarUri = null
                                            selectedDefaultResId = avatarResources[index]
                                            showAvatarPicker = false
                                        },
                                    contentScale = ContentScale.Crop
                                )
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
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
