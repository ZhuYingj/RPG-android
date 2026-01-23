package com.mobile_client.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile_client.pages.ui.theme.MobileclientTheme
import com.mobile_client.services.ChatViewModel
import com.mobile_client.utils.ChatMessage

@Composable
fun ChatBox(chatViewModel: ChatViewModel) {
    var messages by remember { mutableStateOf(emptyList<String>()) }
    val connectionStatus by chatViewModel.connectionStatus.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    var listState = rememberLazyListState()
    Card(modifier = Modifier.width(200.dp).height(200.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            messages.forEach { message ->
                Text(text = message)
            }

        }
    }
}

@Composable
fun MessageBox(chatMessage: ChatMessage) {

}

@Preview(showBackground = true, device="spec:width=2000px,height=1200px, orientation=landscape")
@Composable
fun ChatBoxPreview() {
    MobileclientTheme {
        ChatBox(chatViewModel = viewModel<ChatViewModel>())
    }
}
