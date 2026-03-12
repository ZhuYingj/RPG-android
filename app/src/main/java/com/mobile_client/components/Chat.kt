package com.mobile_client.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun ChatBox(modifier: Modifier = Modifier, chatViewModel: ChatViewModel, lobbyChatViewModel: ChatViewModel? = null, lobbyCode: String) {
    val connectionStatus by chatViewModel.connectionStatus.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    var mostRecentMessage by remember { mutableStateOf<String?>(null) }
    var listState = rememberLazyListState()
    var isCollapsed by remember { mutableStateOf(false) }
    var emojiSelected by remember { mutableStateOf("❤️") }
    val maxChar = 200
    val socketManager = SocketService.instance

    var isLobbyChat by remember { mutableStateOf(false) }
    val activeViewModel = if (isLobbyChat && lobbyChatViewModel != null) lobbyChatViewModel else chatViewModel
    val chatMessages by activeViewModel.messages.collectAsState()

    LaunchedEffect(activeViewModel) {
        socketManager.socket?.off(MessageEvents.CHAT_MESSAGE)
        socketManager.socket?.off(MessageEvents.GLOBAL_CHAT_MESSAGE)

        socketManager.initializeChatListeners { message ->
            activeViewModel.handleChatMessage(message)
        }
    }

    LaunchedEffect(lobbyChatViewModel) {
        if (lobbyChatViewModel == null)  isLobbyChat = false
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }
    Box(modifier.zIndex(1f).padding(16.dp)) {
        if (isCollapsed) {
            FloatingActionButton(
                onClick = { isCollapsed = !isCollapsed },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Chat",
                    tint = Color.White
                )
            }
        }
        else {
            Card(
                modifier = modifier
                    .widthIn(max = 400.dp)
                    .height(400.dp),
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
                                imageVector = Icons.Default.ChatBubble,
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
                        if (lobbyChatViewModel != null && !isCollapsed) {
                            Row() {
                                OutlinedButton(
                                    onClick = { isLobbyChat = false },
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = if((!isLobbyChat)) Color.Yellow else Color.Transparent),
                                    border = BorderStroke(2.dp, Color.White),
                                    shape = RoundedCornerShape(0.dp)){
                                    Text(
                                        "Global",
                                        color = if (!isLobbyChat) Color.Black else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                                OutlinedButton(onClick = { isLobbyChat = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = if((isLobbyChat)) Color.Yellow else Color.Transparent),
                                    border = BorderStroke(2.dp, Color.White),
                                    shape = RoundedCornerShape(0.dp)){
                                    Text(
                                        "Lobby",
                                        color = if (isLobbyChat) Color.Black else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = {isCollapsed = !isCollapsed}){
                            Icon(
                                imageVector = Icons.Default.CloseFullscreen,
                                contentDescription = "Collapse Chat",
                                tint = Color.White)
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
                        println(chatMessages)
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
                            modifier = Modifier.fillMaxWidth() , emojiSelected = emojiSelected
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
                            onValueChange = { if(it.length <= maxChar) newMessage = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Entrez un message...") },
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send   // ou Done
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
    }
}

@Composable
fun MessageBox(chatMessage: ChatMessage) {
    val isFromCurrentUser = chatMessage.username == AccountService.instance.username

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
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
                    color = if (isFromCurrentUser)
                        Color.Green
                    else
                        Color.Black
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
    }
}


//@Preview(showBackground = true, device="spec:width=2000px,height=1200px, orientation=landscape")
//@Composable
//fun ChatBoxPreview() {
//    MobileclientTheme {
//        ChatBox()
//    }
//}
