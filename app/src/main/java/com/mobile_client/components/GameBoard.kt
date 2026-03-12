package com.mobile_client.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mobile_client.utils.GameTile
import com.mobile_client.utils.ImageResources
import com.mobile_client.utils.Player
import com.mobile_client.utils.Position
import com.mobile_client.utils.TileConstants
import kotlin.collections.get

@Composable
fun GameBoard(
    tiles: List<List<GameTile>>,
    players: List<Player>,
    player: Player,
    accessibleTiles: List<Position>,
    path: List<Position>,
    isDebug: Boolean,
    onTileClick: (Position) -> Unit,
) {
    val rows = tiles.size
    if (rows == 0) return
    val cols = tiles[0].size

    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = Modifier.aspectRatio(1f),
        userScrollEnabled = false
    ) {
        items(rows * cols) { index ->
            val x = index / cols
            val y = index % cols
            val tile = tiles[x][y]
            val pos = Position(x, y)
            val isAccessible = accessibleTiles.any { it.x == x && it.y == y }
            val isPath = path.any { it.x == x && it.y == y }
            val playerAtTile = players.find { it.position.x == x && it.position.y == y }
            val isSpawn = player.spawnPoint.x == x && player.spawnPoint.y == y

            TileCell(
                tile = tile,
                playerAtTile = playerAtTile,
                isAccessible = isAccessible,
                isPath = isPath,
                isDebug = isDebug,
                onClick = { onTileClick(pos) }
            )
        }
    }
}

@Composable
fun TileCell(
    tile: GameTile,
    playerAtTile: Player?,
    isAccessible: Boolean,
    isPath: Boolean,
    isDebug: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Tile background
        ImageResources.tileTypeToImage[tile.type]?.let { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Tile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Item on tile
        if (tile.item != TileConstants.Items.None) {
            ImageResources.itemToImage[tile.item]?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Item",
                    modifier = Modifier.fillMaxSize(0.6f)
                )
            }
        }

        // Player on tile
        playerAtTile?.let { p ->
            ImageResources.avatarToImage[p.avatar]?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = p.username,
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }

        // Accessible tile indicator
        if (isAccessible && !isDebug) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isPath) Color.Blue.copy(alpha = 0.3f)
                        else Color.Green.copy(alpha = 0.2f)
                    )
            )
        }

        // Debug cost
        if (isDebug && tile.cost < 99) {
            Text(
                "${tile.cost}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
