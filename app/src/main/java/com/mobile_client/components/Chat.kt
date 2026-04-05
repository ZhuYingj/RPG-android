package com.mobile_client.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mobile_client.services.AccountService
import com.mobile_client.services.SocketService
import com.mobile_client.utils.ChatMessage
import com.mobile_client.utils.MessageEvents
import com.mobile_client.viewModels.ChatViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.utils.ImageUtils
import com.mobile_client.viewModels.FriendsViewModel
import com.mobile_client.viewModels.ThemeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBox(modifier: Modifier = Modifier, chatViewModel: ChatViewModel, lobbyChatViewModel: ChatViewModel? = null, lobbyCode: String, friendsViewModel: FriendsViewModel, themeViewModel: ThemeViewModel) {
    val connectionStatus by chatViewModel.connectionStatus.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    var mostRecentMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    var isCollapsed by remember { mutableStateOf(true) }
    var emojiSelected by remember { mutableStateOf("❤️") }
    val maxChar = 200
    val socketManager = SocketService.instance

    var isLobbyChat by remember { mutableStateOf(false) }
    val activeViewModel = if (isLobbyChat && lobbyChatViewModel != null) lobbyChatViewModel else chatViewModel
    val chatMessages by activeViewModel.messages.collectAsState()
    val isKeyboardVisible = WindowInsets.isImeVisible

    val assets = themeViewModel.assets
    LaunchedEffect(chatViewModel, lobbyChatViewModel) {
        socketManager.socket?.off(MessageEvents.CHAT_MESSAGE)
        socketManager.socket?.off(MessageEvents.GLOBAL_CHAT_MESSAGE)
        socketManager.socket?.off(MessageEvents.CHAT_WARNING)

        val isBlocked: (String) -> Boolean = {
            friendsViewModel.blockedUsers.value.contains(it) ||
                friendsViewModel.blockedByUsers.value.contains(it)
        }

        socketManager.initializeChatListeners { message, event ->
            if (event == MessageEvents.GLOBAL_CHAT_MESSAGE) {
                chatViewModel.handleChatMessage(message, isBlocked)
            } else {
                lobbyChatViewModel?.handleChatMessage(message, isBlocked)
            }
        }
    }

    LaunchedEffect(lobbyChatViewModel) {
        if (lobbyChatViewModel == null) isLobbyChat = false
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(
        modifier
            .zIndex(1f)
            .imePadding(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column {
            if (isCollapsed) {
                FloatingActionButton(
                    onClick = { isCollapsed = !isCollapsed },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Chat",
                        tint = Color.White
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .heightIn(max = 600.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "Chat",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Clavardage",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        connectionStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            IconButton(onClick = { isCollapsed = !isCollapsed }) {
                                Icon(
                                    imageVector = Icons.Default.CloseFullscreen,
                                    contentDescription = "Collapse Chat",
                                    tint = Color.White
                                )
                            }
                        }

                        if (lobbyChatViewModel != null && !isCollapsed) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (!isLobbyChat) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                                        .clickable { isLobbyChat = false }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Général",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (!isLobbyChat) Color.White else Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isLobbyChat) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                                        .clickable { isLobbyChat = true }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Partie",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isLobbyChat) Color.White else Color.Gray
                                    )
                                }
                            }
                        }

                        // Messages
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            items(chatMessages) { message ->
                                MessageBox(message)
                            }
                        }

                        // Input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReactionPicker(
                                onReactionSelected = { emojiSelected = it },
                                modifier = Modifier.fillMaxWidth(),
                                emojiSelected = emojiSelected
                            )

                            ShakeListener(
                                onVerticalShake = {
                                    emojiSelected.let { emoji ->
                                        activeViewModel.sendMessage(emoji, if (isLobbyChat) lobbyCode else "")
                                        mostRecentMessage = emoji
                                    }
                                },
                                onHorizontalShake = {
                                    mostRecentMessage?.let { lastMsg ->
                                        activeViewModel.sendMessage(lastMsg, if (isLobbyChat) lobbyCode else "")
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = newMessage,
                                onValueChange = { if (it.length <= maxChar) newMessage = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Entrez un message...") },
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (newMessage.isNotBlank()) {
                                            mostRecentMessage = newMessage
                                            activeViewModel.sendMessage(newMessage, if (isLobbyChat) lobbyCode else "")
                                            newMessage = ""
                                        }
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (newMessage.isNotBlank()) {
                                        mostRecentMessage = newMessage
                                        activeViewModel.sendMessage(newMessage, if (isLobbyChat) lobbyCode else "")
                                        newMessage = ""
                                    }
                                },
                                enabled = newMessage.isNotBlank()
                            ) {
                                Icon(Icons.AutoMirrored.Default.Send, contentDescription = "Send")
                            }
                        }
                    }
                }
            }

            if (!isKeyboardVisible) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MessageBox(chatMessage: ChatMessage) {
    val isFromCurrentUser = chatMessage.username == AccountService.instance.username
    val avatarBitmap = remember(chatMessage.avatar) {
        ImageUtils.base64ToBitmap(chatMessage.avatar)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromCurrentUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        painter = rememberAsyncImagePainter(avatarBitmap),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = chatMessage.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = if (isFromCurrentUser)
                MaterialTheme.colorScheme.primary
            else
                Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = chatMessage.username,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFromCurrentUser) Color.Green else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chatMessage.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromCurrentUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = chatMessage.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFromCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        painter = rememberAsyncImagePainter(avatarBitmap),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = chatMessage.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
