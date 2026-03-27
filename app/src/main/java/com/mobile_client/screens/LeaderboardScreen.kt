package com.mobile_client.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.services.AccountService
import com.mobile_client.utils.ImageUtils
import com.mobile_client.utils.LeaderboardEntry
import com.mobile_client.utils.LeaderboardFilterType
import com.mobile_client.utils.LeaderboardSortType
import com.mobile_client.viewModels.FriendsViewModel
import com.mobile_client.viewModels.LeaderboardViewModel
import com.mobile_client.viewModels.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderBoardScreen(
    themeViewModel: ThemeViewModel,
    friendsViewModel: FriendsViewModel,
    leaderboardViewModel: LeaderboardViewModel = viewModel(),
) {
    val assets = themeViewModel.assets.backgroundLeaderboardPage
    val friends by friendsViewModel.friends.collectAsState()
    val entries = leaderboardViewModel.entries.value
    val isLoading = leaderboardViewModel.isLoading.value
    val searchQuery = leaderboardViewModel.searchQuery.value
    val sortType = leaderboardViewModel.sortType.value
    val filterType = leaderboardViewModel.filterType.value

    var dropdownExpanded by remember { mutableStateOf(false) }

    val rankedEntries = when (filterType) {
        LeaderboardFilterType.GLOBAL -> {
            entries.mapIndexed { index, entry ->
                Pair(index + 1, entry)
            }
        }
        LeaderboardFilterType.FRIENDS -> {
            val currentUsername = AccountService.instance.username
            val friendUsernames = friends.map { it.username }.toSet()
            var friendRank = 0
            entries.mapNotNull { entry ->
                if (entry.username == currentUsername || friendUsernames.contains(entry.username)) {
                    friendRank++
                    Pair(friendRank, entry)
                } else {
                    null
                }
            }
        }
    }.filter { (_, entry) ->
        searchQuery.isEmpty() || entry.username.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        leaderboardViewModel.initializeSocketListeners()
        leaderboardViewModel.fetchLeaderboard()
        friendsViewModel.loadFriends()
    }

    LaunchedEffect(sortType) {
        leaderboardViewModel.fetchLeaderboard()
    }

    DisposableEffect(Unit) {
        onDispose {
            leaderboardViewModel.removeSocketListeners()
        }
    }

    val horizontalPad = 12.dp
    val topPad = 56.dp
    val bottomPad = 16.dp

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Image(
            painter = painterResource(assets),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight()
                .padding(top = topPad, bottom = bottomPad)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.80f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPad, start = horizontalPad, end = horizontalPad, bottom = bottomPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title
            Text(
                text = "Classement",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp, top = 36.dp)
            )

            // Top row: Dropdown + Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Dropdown sort
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.width(220.dp)
                ) {
                    OutlinedTextField(
                        value = sortType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trier par") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        LeaderboardSortType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    leaderboardViewModel.sortType.value = type
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Toggle Global/Friends
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeaderboardFilterType.entries.forEach { type ->
                        OutlinedButton(
                            onClick = { leaderboardViewModel.filterType.value = type },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Text(
                                type.label,
                                fontWeight = if (filterType == type) FontWeight.Bold else FontWeight.Normal,
                                color = if (filterType == type) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { leaderboardViewModel.searchQuery.value = it },
                label = { Text("Rechercher un joueur") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 8.dp),
                singleLine = true,
            )

            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("#", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                Text("", modifier = Modifier.width(48.dp)) // avatar space
                Text("Joueur", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(sortType.label, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Leaderboard list
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (rankedEntries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun joueur trouvé", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(rankedEntries.size) { index ->
                        val (rank, entry) = rankedEntries[index]
                        LeaderboardRowDisplay(
                            rank = rank,
                            entry = entry,
                            displayValue = leaderboardViewModel.getDisplayValue(entry),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRowDisplay(
    rank: Int,
    entry: LeaderboardEntry,
    displayValue: String,
) {
    val avatarBitmap = remember(entry.avatar) {
        ImageUtils.base64ToBitmap(entry.avatar)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Rank
        Text(
            text = "$rank",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = when (rank) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> Color.Black
            },
            modifier = Modifier.width(40.dp)
        )

        // Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
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
                Text(
                    text = entry.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Username
        Text(
            text = entry.username,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        // Value
        Text(
            text = displayValue,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF4CAF50)
        )
    }
}
