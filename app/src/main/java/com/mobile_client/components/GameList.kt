package com.mobile_client.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mobile_client.screens.ui.theme.MobileclientTheme
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.SocketCommunicationConst
import com.mobile_client.utils.Tile
import com.mobile_client.utils.TileConstants
import com.mobile_client.viewModels.BaseGameListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.produceState

private val bitmapCache = mutableMapOf<Int, android.graphics.Bitmap>()

fun getCachedBitmap(context: android.content.Context, resId: Int, size: Int): android.graphics.Bitmap {
    return bitmapCache.getOrPut(resId) {
        val bmp = BitmapFactory.decodeResource(context.resources, resId)
        android.graphics.Bitmap.createScaledBitmap(bmp, size, size, false)
    }
}
@Composable
fun GameList(
    viewModel: BaseGameListViewModel,
    modifier: Modifier = Modifier
) {
    val maps by viewModel.maps.collectAsState()
    val currentGames by viewModel.currentGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var descriptionId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadMaps()
    }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = error ?: "Une erreur est survenue", color = Color.Red, fontSize = 19.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMaps() }) { Text("Réessayer") }
                    }
                }
            }
            maps.isEmpty() && !viewModel.isLobbyMode -> {
                Text(
                    text = "Aucun jeu disponible présentement.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 19.sp,
                    color = Color(0xFF555555)
                )
            }
            currentGames.isEmpty() && viewModel.isLobbyMode -> {
                Text(
                    text = "Aucune partie en cours présentement.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 19.sp,
                    color = Color(0xFF555555)
                )
            }
            else -> {
                if (viewModel.isLobbyMode) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(currentGames) { lobby ->
                            LobbyMapItem(
                                map = lobby.map as GameMap,
                                lobby = lobby,
                                showDescription = descriptionId == (lobby.map)._id,
                                onMouseEnter = { descriptionId = (lobby.map)._id },
                                onMouseLeave = { descriptionId = null },
                                onPlay = { viewModel.onClick(lobby.map) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(maps) { map ->
                            MapItem(
                                map = map,
                                showDescription = descriptionId == map._id,
                                onMouseEnter = { descriptionId = map._id },
                                onMouseLeave = { descriptionId = null },
                                onPlay = { viewModel.onClick(map) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LobbyMapItem(
    map: GameMap,
    lobby: SocketCommunicationConst.SendableLobbies,
    showDescription: Boolean,
    onMouseEnter: () -> Unit,
    onMouseLeave: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .background(color = Color(0xFFFFFFFF).copy(alpha = 0.92f), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFFDDDDDD), shape = RoundedCornerShape(8.dp))
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MapTilesDisplay(
                tiles = map.tiles,
                modifier = Modifier.size(200.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 40.dp, end = 10.dp)
            ) {
                if (showDescription) {
                    DescriptionBubble(description = map.description)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MapInfo(map = map, showLastModified = false)
                        Spacer(modifier = Modifier.height(8.dp))
                        LobbyInfo(lobby = lobby)
                    }
                }
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(60.dp)
                    .background(color = Color(0xFF91CDFD), shape = RoundedCornerShape(5.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Rejoindre",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun LobbyInfo(lobby: SocketCommunicationConst.SendableLobbies) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LobbyBadge(
            label = "Code : ${lobby.code}",
            backgroundColor = Color(0xFFE3F2FD),
            textColor = Color(0xFF1565C0)
        )
        LobbyBadge(
            label = if (lobby.hasFriend) "🔒 Amis seulement" else "🔓 Public",
            backgroundColor = if (lobby.hasFriend) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
            textColor = if (lobby.hasFriend) Color(0xFFE65100) else Color(0xFF2E7D32)
        )
        LobbyBadge(
            label = "Hôte : ${lobby.host}",
            backgroundColor = Color(0xFFF3E5F5),
            textColor = Color(0xFF6A1B9A)
        )
        val maxPlayers = ImageResources.sizeToPlayerNumber[(lobby.map as GameMap).size] ?: 0
        LobbyBadge(
            label = "${lobby.playerNumber}/${maxPlayers}",
            backgroundColor = Color(0xFFF3E5F5),
            textColor = Color(0xFF6A1B9A)
        )
        LobbyBadge(
            label = "Accès: ${if (lobby.isLocked) "Vérrouillé" else "Déverrouillé"}",
            backgroundColor = Color(0xFFF3E5F5),
            textColor = Color(0xFF6A1B9A)
        )

        LobbyBadge(
            label = "💰 ${lobby.fee}",
            backgroundColor = Color(0xFFFFFDE7),
            textColor = Color(0xFFF57F17)
        )
        if (lobby.hasBlockedUser) {
            LobbyBadge(
                label = "⚠️ Utilisateur bloqué présent",
                backgroundColor = Color(0xFFFFEBEE),
                textColor = Color(0xFFC62828)
            )
        }
    }
}

@Composable
fun LobbyBadge(label: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MapItem(
    map: GameMap,
    showDescription: Boolean,
    onMouseEnter: () -> Unit,
    onMouseLeave: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .background(color = Color(0xFFFFFFFF).copy(alpha = 0.92f), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFFDDDDDD), shape = RoundedCornerShape(8.dp))
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MapTilesDisplay(
                tiles = map.tiles,
                modifier = Modifier.size(200.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 40.dp, end = 10.dp)
            ) {
                if (showDescription) {
                    DescriptionBubble(description = map.description)
                } else {
                    MapInfo(map = map)
                }
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(60.dp)
                    .background(color = Color(0xFF91CDFD), shape = RoundedCornerShape(5.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MapTilesDisplay(
    tiles: List<List<Tile>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, tiles) {
        value = withContext(Dispatchers.IO) {
            val tileSize = 20
            val width = (tiles.firstOrNull()?.size ?: 1) * tileSize
            val height = tiles.size * tileSize
            val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)

            tiles.forEachIndexed { rowIdx, row ->
                row.forEachIndexed { colIdx, tile ->
                    ImageResources.tileTypeToImage[tile.type]?.let { resId ->
                        val tileBmp = getCachedBitmap(context, resId, tileSize)  // already scaled
                        canvas.drawBitmap(tileBmp, (colIdx * tileSize).toFloat(), (rowIdx * tileSize).toFloat(), null)
                    }
                    if (tile.item != TileConstants.Items.None) {
                        ImageResources.itemToImage[tile.item]?.let { resId ->
                            val itemBmp = getCachedBitmap(context, resId, tileSize)  // already scaled
                            canvas.drawBitmap(itemBmp, (colIdx * tileSize).toFloat(), (rowIdx * tileSize).toFloat(), null)
                        }
                    }
                }
            }
            bmp
        }
    }

    if (bitmap != null) {
        Image(
            painter = rememberAsyncImagePainter(bitmap),
            contentDescription = "Map",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun DescriptionBubble(description: String) {
    Box(
        modifier = Modifier
            .width(525.dp)
            .height(175.dp)
            .border(width = 3.dp, color = Color(0xFFA09C7F), shape = RoundedCornerShape(0.dp))
            .background(color = Color(0xFFCCBDA3), shape = RoundedCornerShape(0.dp))
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 10.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Description", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 2.dp))
            Text(text = description, fontSize = 17.sp, color = Color.Black)
        }
    }
}

@Composable
fun MapInfo(map: GameMap, showLastModified: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = map.name, fontSize = 35.sp, fontWeight = FontWeight.Normal, color = Color(0xFF313131), letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Taille : ${map.size}", fontSize = 19.sp, color = Color(0xFF555555))
        Text(text = "Mode : ${if (map.isCaptureTheFlag) "CTF" else "Classique"}", fontSize = 19.sp, color = Color(0xFF555555))
        if (showLastModified) {
            Text(text = "Dernière Modification : ${map.lastModified}", fontSize = 19.sp, color = Color(0xFF555555))
        }
    }
}

@Preview(showBackground = true, device = "spec:width=2000px,height=1200px,orientation=landscape")
@Composable
fun GameListPreview() {
    MobileclientTheme {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            //GameList(GameListViewModel())
        }
    }
}
