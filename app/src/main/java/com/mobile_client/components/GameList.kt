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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.text.style.TextAlign
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
import androidx.core.graphics.scale
import com.mobile_client.viewModels.ThemeViewModel
import androidx.core.graphics.createBitmap
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val bitmapCache = mutableMapOf<Int, android.graphics.Bitmap>()

fun getCachedBitmap(context: android.content.Context, resId: Int, size: Int): android.graphics.Bitmap {
    return bitmapCache.getOrPut(resId) {
        val bmp = BitmapFactory.decodeResource(context.resources, resId)
        bmp.scale(size, size, false)
        //android.graphics.Bitmap.createScaledBitmap(bmp, size, size, false)
    }
}
@Composable
fun GameList(
    viewModel: BaseGameListViewModel,
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel
) {
    val maps by viewModel.maps.collectAsState()
    val currentGames by viewModel.currentGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var descriptionId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val assets = themeViewModel.assets

    LaunchedEffect(Unit) {
        viewModel.loadMaps()
    }

    val isEmpty = (maps.isEmpty() && !viewModel.isLobbyMode) || (currentGames.isEmpty() && viewModel.isLobbyMode)
    val columnModifier = modifier.fillMaxWidth().wrapContentHeight().padding(20.dp)


    Column(modifier = columnModifier) {
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun jeu disponible présentement.",
                        fontSize = 19.sp,
                        color = assets.mainPageTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
            currentGames.isEmpty() && viewModel.isLobbyMode -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune partie en cours présentement.",
                        fontSize = 19.sp,
                        color = assets.mainPageTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                if (viewModel.isLobbyMode) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.wrapContentHeight(),
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
                        modifier = Modifier.wrapContentHeight(),
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
            .padding(bottom = 8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .background(color = Color(0xFFFFFFFF).copy(alpha = 0.92f), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFFDDDDDD), shape = RoundedCornerShape(8.dp))
            .padding(10.dp)
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
    val acces = if ((lobby.playerNumber == lobby.maxPlayers && lobby.isGameStarted) || (lobby.isLocked && !lobby.isGameStarted))
        "Vérrouillé" else "Déverrouillé"
    val statut = if (lobby.isGameStarted) "En cours" else "En attente"

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = (lobby.map as GameMap).name, fontSize = 35.sp, fontWeight = FontWeight.Normal, color = Color(0xFF313131), letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(text = "Code : ${lobby.code}", fontSize = 18.sp, color = Color(0xFF555555))
            Text(text = "Joueurs : ${lobby.playerNumber}/${lobby.maxPlayers}", fontSize = 18.sp, color = Color(0xFF555555))
            Text(text = "Statut : $statut", fontSize = 18.sp, color = Color(0xFF555555))
            Text(text = "Accès : $acces", fontSize = 18.sp, color = Color(0xFF555555))
        }
        Text(text = "Nombre de joueurs max : ${lobby.maxPlayers}", fontSize = 14.sp, color = Color(0xFF555555))
        Text(text = "Taille : ${lobby.map.size}", fontSize = 14.sp, color = Color(0xFF555555))
        Text(text = "Mode : ${if (lobby.map.isCaptureTheFlag) "CTF" else "Classique"}", fontSize = 14.sp, color = Color(0xFF555555))
        Text(text = "Hôte : ${lobby.host}", fontSize = 14.sp, color = Color(0xFF555555))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Frais d'entrée : ${lobby.fee} $", fontSize = 14.sp, color = Color(0xFF555555))
            if (lobby.hasFriend) {
                Text(text = "\uD83D\uDC65 Un ami est dans cette partie", fontSize = 14.sp)
            }
            if (lobby.hasBlockedUser) {
                Text(text = "⚠️ Utilisateur bloqué présent", fontSize = 14.sp)
            }
        }
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
            val bmp = createBitmap(width, height)
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
fun convertUTCToLocalDateTime(utcString: String): String {
    val instant: Instant = Instant.parse(utcString)
    val localDateTime = instant.atZone(ZoneId.of("America/Toronto")).toLocalDateTime()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss")
    return localDateTime.format(formatter)
}
@Composable
fun MapInfo(map: GameMap, showLastModified: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = map.name, fontSize = 35.sp, fontWeight = FontWeight.Normal, color = Color(0xFF313131), letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Taille : ${map.size}", fontSize = 19.sp, color = Color(0xFF555555))
        Text(text = "Mode : ${if (map.isCaptureTheFlag) "CTF" else "Classique"}", fontSize = 19.sp, color = Color(0xFF555555))
        if (showLastModified) {
            Text(text = "Dernière Modification : ${convertUTCToLocalDateTime(map.lastModified)}", fontSize = 19.sp, color = Color(0xFF555555))
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


