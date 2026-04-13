package com.mobile_client.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mobile_client.screens.R
import com.mobile_client.utils.FriendsTab
import com.mobile_client.utils.launchQrScanner
import com.mobile_client.viewModels.FriendsViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.showDismissible

@Composable
fun FriendsPanel(
    modifier: Modifier = Modifier,
    friendsViewModel: FriendsViewModel,
    snackbarHostState: SnackbarHostState,
) {
    var isOpen by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(FriendsTab.FRIENDS) }
    var isAddingFriend by remember { mutableStateOf(false) }
    var searchFilter by remember { mutableStateOf("") }

    val friends by friendsViewModel.friends.collectAsState()
    val friendRequests by friendsViewModel.friendRequests.collectAsState()
    val sentFriendRequests by friendsViewModel.sentFriendRequests.collectAsState()
    val allUsers by friendsViewModel.allUsers.collectAsState()
    val sentRequestUsernames by friendsViewModel.sentRequestUsernames.collectAsState()
    val receivedRequestIds by friendsViewModel.receivedRequestIds.collectAsState()
    val blockedUsers by friendsViewModel.blockedUsers.collectAsState()
    val pendingBlock by friendsViewModel.pendingBlock.collectAsState()
    val snackbarMessage by friendsViewModel.snackbarMessage.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        friendsViewModel.initializeSocketListeners()
        friendsViewModel.loadAll()
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showDismissible(scope, message)
            friendsViewModel.clearSnackbarMessage()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            friendsViewModel.removeSocketListeners()
        }
    }

    Box(modifier = modifier.zIndex(1f).then( if (isOpen) Modifier.imePadding() else Modifier)) {
        if (!isOpen) {
            FloatingActionButton(
                onClick = { isOpen = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = "Amis",
                    tint = Color.White
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .widthIn(max = 355.dp)
                    .height(420.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (isAddingFriend) {
                                    isAddingFriend = false
                                    searchFilter = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isAddingFriend) Icons.AutoMirrored.Filled.ArrowBack
                                else Icons.Default.PersonOutline,
                                contentDescription = if (isAddingFriend) "Retour" else "Friends",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isAddingFriend) "Ajouter un ami" else "Amis",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CloseFullscreen,
                            contentDescription = "Fermer",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                isOpen = false
                                isAddingFriend = false
                                searchFilter = ""
                            }
                        )
                    }

                    if (isAddingFriend) {
                        // Search bar
                        OutlinedTextField(
                            value = searchFilter,
                            onValueChange = {
                                val filtered = it.filterNot { c -> c.isWhitespace() }
                                if (filtered.length <= 15) {
                                    searchFilter = filtered
                                    friendsViewModel.loadAllUsers(filtered)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            placeholder = { Text("Rechercher un utilisateur") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )

                        // User list
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (allUsers.isEmpty()) {
                                item {
                                    Text(
                                        "Aucun utilisateur trouvé",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        textAlign = TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            }
                            items(allUsers, key = { it.username }) { user ->
                                val alreadySent = sentRequestUsernames.contains(user.username)
                                val receivedRequestId = receivedRequestIds[user.username]
                                val isBlocked = blockedUsers.contains(user.username)
                                val isPending = pendingBlock.contains(user.username)
                                FriendItem(
                                    username = user.username,
                                    avatarBase64 = user.avatar,
                                    actions = {
                                        if (receivedRequestId != null && !isBlocked && !isPending) {
                                            IconButton(onClick = { friendsViewModel.acceptFriendRequest(receivedRequestId) }) {
                                                Icon(Icons.Default.Check, contentDescription = "Accepter", tint = Color(0xFF4CAF50))
                                            }
                                            IconButton(onClick = { friendsViewModel.denyFriendRequest(receivedRequestId) }) {
                                                Icon(Icons.Default.Close, contentDescription = "Refuser", tint = Color(0xFFF44336))
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { if (!alreadySent) friendsViewModel.sendFriendRequest(user.username) },
                                                enabled = !alreadySent && !isBlocked && !isPending
                                            ) {
                                                Icon(
                                                    if (alreadySent && !isBlocked && !isPending) Icons.Default.Check else Icons.Default.PersonAddAlt,
                                                    contentDescription = if (alreadySent && !isBlocked && !isPending) "Déjà envoyé" else "Ajouter",
                                                    tint = if (alreadySent || isBlocked || isPending) Color.Gray else Color(0xFF4CAF50)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                if (!isBlocked && !isPending) {
                                                    friendsViewModel.blockUser(user.username)
                                                    snackbarHostState.showDismissible(
                                                        scope,
                                                        "Utilisateur ${user.username} bloqué"
                                                    )
                                                }
                                            },
                                            enabled = !isBlocked && !isPending
                                        ) {
                                            Icon(
                                                Icons.Default.Block,
                                                contentDescription = "Bloquer",
                                                tint = if (isBlocked || isPending) Color.Gray else Color(0xFFFF9800)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        // Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color(0xFFF8F8F8))
                        ) {
                            FriendsTab.entries.forEach { tab ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clickable { activeTab = tab },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (activeTab == tab) Color.Black else Color.Gray
                                    )
                                    if (activeTab == tab) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .width(85.dp)
                                                .height(3.dp)
                                                .background(
                                                    color = Color(0xFFFFA500),
                                                    shape = RoundedCornerShape(50)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        // Friends / Requests / Sent lists
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (activeTab) {
                                FriendsTab.FRIENDS -> {
                                    if (friends.isEmpty()) {
                                        item {
                                            Text(
                                                "Aucun ami",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                textAlign = TextAlign.Center,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    items(friends, key = { it.username }) { friend ->
                                        FriendItem(
                                            username = friend.username,
                                            avatarBase64 = friend.avatar,
                                            actions = {
                                                IconButton(onClick = { friendsViewModel.removeFriend(friend.username) }) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.person_remove_24px),
                                                        tint = Color(0xFFF44336),
                                                        contentDescription = "Remove person"
                                                    )
                                                }
                                                IconButton(onClick = { friendsViewModel.blockUser(friend.username); snackbarHostState.showDismissible(scope, "Utilisateur ${friend.username} bloqué") }) {
                                                    Icon(
                                                        Icons.Default.Block,
                                                        contentDescription = "Bloquer",
                                                        tint = Color(0xFFFF9800)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                FriendsTab.REQUESTS -> {
                                    if (friendRequests.isEmpty()) {
                                        item {
                                            Text(
                                                "Aucune requête",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                textAlign = TextAlign.Center,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    items(friendRequests, key = { it.id }) { request ->
                                        FriendItem(
                                            username = request.username,
                                            avatarBase64 = request.avatar,
                                            actions = {
                                                IconButton(onClick = { friendsViewModel.acceptFriendRequest(request.id) }) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = "Accepter",
                                                        tint = Color(0xFF4CAF50)
                                                    )
                                                }
                                                IconButton(onClick = { friendsViewModel.denyFriendRequest(request.id) }) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Refuser",
                                                        tint = Color(0xFFF44336)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                FriendsTab.SENT -> {
                                    if (sentFriendRequests.isEmpty()) {
                                        item {
                                            Text(
                                                "Aucune requête envoyée",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                textAlign = TextAlign.Center,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    items(sentFriendRequests, key = { it.id }) { request ->
                                        FriendItem(
                                            username = request.username,
                                            avatarBase64 = request.avatar,
                                            actions = {
                                                IconButton(onClick = { friendsViewModel.undoFriendRequest(request.id) }) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Annuler",
                                                        tint = Color(0xFFF44336)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                FriendsTab.BLOCKED -> {
                                    if (blockedUsers.isEmpty()) {
                                        item {
                                            Text(
                                                "Aucun utilisateur bloqué",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                textAlign = TextAlign.Center,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    items(blockedUsers, key = { it }) { username ->
                                        FriendItem(
                                            username = username,
                                            actions = {
                                                IconButton(onClick = { friendsViewModel.blockUser(username); snackbarHostState.showDismissible(scope, "Utilisateur $username débloqué") }) {
                                                    Icon(
                                                        Icons.Default.LockOpen,
                                                        contentDescription = "Débloquer",
                                                        tint = Color(0xFFF44336)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        // Add Friend + QR Scan buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        isAddingFriend = true
                                        friendsViewModel.loadAllUsers()
                                        friendsViewModel.loadSentRequestUsernames()
                                        friendsViewModel.loadReceivedRequestUsernames()
                                    }
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PersonAddAlt,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Ajouter un ami",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        launchQrScanner(
                                            context = context,
                                            onResult = { scannedUsername ->
                                                friendsViewModel.sendFriendRequest(scannedUsername.trim())
                                            },
                                            onError = { message ->
                                                snackbarHostState.showDismissible(scope, message)
                                            }
                                        )
                                    }
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Scanner QR",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendItem(
    username: String,
    avatarBase64: String? = null,
    actions: @Composable () -> Unit
) {
    val avatarBitmap = remember(avatarBase64) {
        ImageUtils.base64ToBitmap(avatarBase64)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (avatarBitmap != null) {
                Image(
                    painter = rememberAsyncImagePainter(avatarBitmap),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = "Avatar par défaut",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = username,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}
