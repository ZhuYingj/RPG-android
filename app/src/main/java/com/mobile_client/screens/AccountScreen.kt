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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.Validation
import com.mobile_client.viewModels.AccountViewModel
import kotlinx.coroutines.launch
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun AccountScreen(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    accountViewModel: AccountViewModel = viewModel()
) {

    val account by accountViewModel.account.collectAsState()
    val snackbarMessage by accountViewModel.message.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember(account) { mutableStateOf(account?.username ?: "") }
    var email by remember(account) { mutableStateOf(account?.email ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var avatarBase64 by remember { mutableStateOf<String?>(null) }

    // Track whether a default avatar was selected (not a camera photo)
    var selectedDefaultResId by remember { mutableStateOf<Int?>(null) }

    var showAvatarPicker by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val avatarResources = listOf(
        R.drawable.default1, R.drawable.default2, R.drawable.default3, R.drawable.default4,
        R.drawable.default5, R.drawable.default6, R.drawable.default7, R.drawable.default8
    )

    val hasAvatarChange = avatarBase64 != null

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->

        if (success && tempPhotoUri != null) {

            avatarUri = tempPhotoUri
            selectedDefaultResId = null

            val bitmap = ImageUtils.uriToBitmap(context, tempPhotoUri!!)
            bitmap?.let {

                val resized = ImageUtils.resizeBitmap(it, 1024)
                val base64 = ImageUtils.bitmapToBase64(resized, 70)

                avatarBase64 = base64
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permission caméra refusée")
            }
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            accountViewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth(0.5f)
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
                    .border(
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedDefaultResId != null) {
                    // Showing a newly selected default avatar (not yet saved)
                    Image(
                        painter = rememberAsyncImagePainter(selectedDefaultResId),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (avatarUri != null) {
                    // Showing a newly taken camera photo (not yet saved)
                    Image(
                        painter = rememberAsyncImagePainter(avatarUri),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (account?.avatar != null) {
                    // Showing the saved avatar from the server
                    val bitmap = remember(account?.avatar) {
                        ImageUtils.base64ToBitmap(account?.avatar)
                    }
                    bitmap?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Text(
                        text = account?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            // Avatar buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Photo")
                }

                OutlinedButton(
                    onClick = { showAvatarPicker = true }
                ) {
                    Text("Choisir un avatar")
                }
            }

            // Save / Reset avatar buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        avatarBase64 = null
                        avatarUri = null
                        selectedDefaultResId = null
                    },
                    enabled = hasAvatarChange
                ) {
                    Text("Réinitialiser avatar")
                }

                Button(
                    onClick = {
                        avatarBase64?.let {
                            accountViewModel.updateAccountAvatar(it)
                            avatarBase64 = null
                            avatarUri = null
                            selectedDefaultResId = null
                        }
                    },
                    enabled = hasAvatarChange
                ) {
                    Text("Enregistrer avatar")
                }
            }

            // Username
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

            // Email
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
                ) {
                    Text("Réinitialiser")
                }

                Button(
                    onClick = {
                        accountViewModel.updateAccount(name = name, email = email)
                    },
                    enabled = nameError == null && emailError == null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }

    // Avatar picker dialog
    if (showAvatarPicker) {

        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            confirmButton = {},
            title = { Text("Choisir un avatar") },

            text = {

                Column {

                    for (row in 0..1) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            for (col in 0..3) {

                                val index = row * 4 + col

                                Image(
                                    painter = rememberAsyncImagePainter(avatarResources[index]),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {

                                            val bitmap =
                                                BitmapFactory.decodeResource(
                                                    context.resources,
                                                    avatarResources[index]
                                                )

                                            val resized =
                                                ImageUtils.resizeBitmap(bitmap, 1024)

                                            avatarBase64 =
                                                ImageUtils.bitmapToBase64(resized)

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
