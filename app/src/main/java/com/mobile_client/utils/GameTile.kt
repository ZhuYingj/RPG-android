package com.mobile_client.utils

data class GameTile(
    val type: TileConstants.Types,
    var item: TileConstants.Items? = TileConstants.Items.None,
    var cost: Int = 99,
    var parentTile: Position? = null
)

fun List<List<Tile>>.toGameTiles(): List<List<GameTile>> {
    return map { row ->
        row.map { tile ->
            GameTile(type = tile.type, item = tile.item)
        }
    }
}
