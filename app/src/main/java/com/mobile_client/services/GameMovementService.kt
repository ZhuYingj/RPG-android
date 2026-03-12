package com.mobile_client.services

import com.mobile_client.utils.GameTile
import com.mobile_client.utils.ORTHOGONAL_DIRECTIONS
import com.mobile_client.utils.Player
import com.mobile_client.utils.Position
import com.mobile_client.utils.TileConstants
import com.mobile_client.utils.TypeToCost

class GameMovementService {

    var tiles: List<List<GameTile>> = emptyList()
    var isDebug: Boolean = false

    fun availableMovement(player: Player, players: List<Player>): List<Position> {
        resetTiles()
        val accessibleTiles = mutableListOf<Position>()
        accessibleTiles.add(player.position)
        tiles[player.position.x][player.position.y].cost = 0
        tiles[player.position.x][player.position.y].parentTile = player.position

        var index = 0
        while (index < accessibleTiles.size) {
            checkAccessibleTile(accessibleTiles, index, player, players)
            index++
        }

        return accessibleTiles.distinctBy { Pair(it.x, it.y) }
    }

    fun resetTiles() {
        tiles.forEach { row ->
            row.forEach { tile ->
                tile.cost = 99
                tile.parentTile = null
            }
        }
    }

    private fun checkAccessibleTile(
        accessibleTiles: MutableList<Position>,
        index: Int,
        player: Player,
        players: List<Player>
    ) {
        val tile = accessibleTiles[index]
        for (dir in ORTHOGONAL_DIRECTIONS) {
            val x = tile.x + dir.x
            val y = tile.y + dir.y

            if (!isInMap(Position(x, y))) continue

            val adjacentTile = tiles[x][y]
            val tileType = if(isDebug && adjacentTile.type == TileConstants.Types.ClosedDoor) TileConstants.Types.OpenDoor else adjacentTile.type

            if (tileType == TileConstants.Types.Wall || tileType == TileConstants.Types.ClosedDoor) continue
            if (hasPlayer(Position(x, y), players)) continue

            val newCost = tiles[tile.x][tile.y].cost + (TypeToCost[tileType] ?: 99)
            if (adjacentTile.parentTile == null || newCost < adjacentTile.cost) {
                adjacentTile.cost = newCost
                adjacentTile.parentTile = tile
                if (isDebug || player.movement >= newCost) {
                    accessibleTiles.add(Position(x, y))
                }
            }
        }
    }

    private fun hasPlayer(position: Position, players: List<Player>): Boolean {
        return players.any { it.position.x == position.x && it.position.y == position.y }
    }

    private fun isInMap(position: Position): Boolean {
        return position.x >= 0 && position.y >= 0 && position.x < tiles.size && position.y < tiles[0].size
    }
}
