package com.mobile_client.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun GameBoard(
    tiles: List<List<GameTile>>,
    players: List<Player>,
    player: Player,
    accessibleTiles: List<Position>,
    visibleBushTiles: List<Position>,
    path: List<Position>,
    isDebug: Boolean,
    onTileClick: (Position) -> Unit,
) {
    val rows = tiles.size
    if (rows == 0) return
    val cols = tiles[0].size

    Column(modifier = Modifier.aspectRatio(1f)) {
        for (x in 0 until rows) {
            Row(modifier = Modifier.weight(1f)) {
                for (y in 0 until cols) {
                    val tile = tiles[x][y]
                    val isAccessible = remember(accessibleTiles) { accessibleTiles.any { it.x == x && it.y == y } }
                    val isVisibleBush = remember(visibleBushTiles) { visibleBushTiles.any { it.x == x && it.y == y } }
                    val isPath = remember(path) { path.any { it.x == x && it.y == y } }
                    val playerAtTile = remember(players) { players.find { it.position.x == x && it.position.y == y } }

                    Box(modifier = Modifier.weight(1f)) {
                        TileCell(
                            tile = tile,
                            playerAtTile = playerAtTile,
                            isAccessible = isAccessible,
                            isVisibleBush = isVisibleBush,
                            isPath = isPath,
                            isDebug = isDebug,
                            onClick = { onTileClick(Position(x, y)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TileCell(
    tile: GameTile,
    playerAtTile: Player?,
    isAccessible: Boolean,
    isVisibleBush: Boolean,
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
        val typeToDraw = if ((tile.type == TileConstants.Types.Bush && isVisibleBush) || isDebug ) {
            TileConstants.Types.OpenedBush
        } else {
            tile.type
        }

        ImageResources.tileTypeToImage[typeToDraw]?.let { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Tile",
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (tile.type == TileConstants.Types.Bush && !isVisibleBush && !isDebug) Modifier.zIndex(3f) else Modifier),
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
                    modifier = Modifier.fillMaxSize(0.7f).zIndex(1f)
                )
            }
            for (cos in p.equippedItems) {
                ImageResources.cosmeticToImage[cos.filePath]?.let { resId ->
                    if(cos.type == 1) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "cosmétique",
                            modifier = Modifier.fillMaxSize(0.7f).zIndex(2f).offset(y = (-17).dp)
                        )
                    }
                }
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
                    ).zIndex(4f)
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
