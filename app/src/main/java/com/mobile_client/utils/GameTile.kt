package com.mobile_client.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

data class GameTile(
    val type: TileConstants.Types,
    var item: TileConstants.Items? = TileConstants.Items.None) {

    var cost by mutableIntStateOf(99)
    var parentTile: Position? = null
}

fun List<List<Tile>>.toGameTiles(): List<List<GameTile>> {
    return map { row ->
        row.map { tile ->
            GameTile(type = tile.type, item = tile.item)
        }
    }
}
