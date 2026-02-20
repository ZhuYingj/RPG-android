package com.mobile_client.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
fun ReactionPicker(
    onReactionSelected: (String) -> Unit, modifier : Modifier = Modifier, emojiSelected: String? = null
) {
    var isOpen by remember { mutableStateOf(false) }

    Box {
        ReactionButton(emojiSelected = emojiSelected, onClick = { isOpen = !isOpen })

        if (isOpen) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { isOpen = false }
            ) {
                EmojiMenu(
                    onEmojiClick = { emoji ->
                        onReactionSelected(emoji)
                        isOpen = false
                    }
                )
            }
        }
    }
}

@Composable
fun ReactionButton(onClick: () -> Unit, emojiSelected: String? = null) {
    IconButton(onClick = onClick) {
        Text(emojiSelected ?: "❤️")
    }
}

@Composable
fun EmojiMenu(
    onEmojiClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("👍", "❤️", "😂", "😮").forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onEmojiClick(emoji) }
                )
            }
        }
    }
}

