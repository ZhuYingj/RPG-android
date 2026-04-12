package com.mobile_client.services

import com.mobile_client.utils.GameTile
import com.mobile_client.utils.ORTHOGONAL_DIRECTIONS
import com.mobile_client.utils.Player
import com.mobile_client.utils.Position
import com.mobile_client.utils.TileConstants
import com.mobile_client.utils.TypeToCost

class GameMovementService {

    var tiles: List<List<GameTile>> = emptyList()
    var visibleBushList: List<Position> = emptyList()
    var isDebug: Boolean = false

    // Track only the tiles we actually modified so we can reset selectively
    private val modifiedTiles = mutableListOf<Position>()

    fun availableMovement(player: Player, players: List<Player>): List<Position> {
        resetTiles()
        val pos = player.position
        if (!isInMap(pos)) return emptyList()

        val visited = HashSet<Long>()
        val accessibleTiles = mutableListOf<Position>()

        accessibleTiles.add(pos)
        visited.add(posKey(pos))
        tiles[pos.x][pos.y].cost = 0
        tiles[pos.x][pos.y].parentTile = pos
        modifiedTiles.add(pos)

        var index = 0
        while (index < accessibleTiles.size) {
            checkAccessibleTile(accessibleTiles, visited, index, player, players)
            index++
        }

        return accessibleTiles
    }

    fun visibleBushTiles(player: Player): List<Position> {
        val pos = player.position
        if (!isInMap(pos)) return emptyList()
        val visibleBushTiles = mutableListOf<Position>()
        if (tiles[pos.x][pos.y].type != TileConstants.Types.Bush) {
            for (dir in ORTHOGONAL_DIRECTIONS) {
                val x = pos.x + dir.x
                val y = pos.y + dir.y
                if (!isInMap(Position(x, y))) continue
                visibleBushTiles.add(Position(x, y))
            }
        } else {
            val queue = ArrayDeque<Position>()
            val visitedSet = HashSet<Long>()
            queue.add(pos)
            visitedSet.add(posKey(pos))

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                visibleBushTiles.add(current)

                for (dir in ORTHOGONAL_DIRECTIONS) {
                    val nx = current.x + dir.x
                    val ny = current.y + dir.y
                    val nextPos = Position(nx, ny)

                    if (!isInMap(nextPos)) continue

                    val nextTile = tiles[nx][ny]
                    val key = posKey(nextPos)

                    if (nextTile.type == TileConstants.Types.Bush && visitedSet.add(key)) {
                        queue.add(nextPos)
                    }
                }
            }
        }
        return visibleBushTiles
    }

    fun resetTiles() {
        for (pos in modifiedTiles) {
            if (pos.x < tiles.size && pos.y < tiles[0].size) {
                tiles[pos.x][pos.y].cost = 99
                tiles[pos.x][pos.y].parentTile = null
            }
        }
        modifiedTiles.clear()
    }

    private fun checkAccessibleTile(
        accessibleTiles: MutableList<Position>,
        visited: HashSet<Long>,
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
            val tileType = if (isDebug && (adjacentTile.type == TileConstants.Types.ClosedDoor
                    || adjacentTile.type == TileConstants.Types.ClosedAutoDoor))
                TileConstants.Types.OpenDoor else adjacentTile.type

            if (tileType == TileConstants.Types.Wall
                || tileType == TileConstants.Types.ClosedDoor
                || tileType == TileConstants.Types.ClosedAutoDoor) continue

            if (hasPlayer(Position(x, y), players)
                && (tiles[x][y].type != TileConstants.Types.Bush || Position(x, y) in visibleBushList)) continue

            val newCost = tiles[tile.x][tile.y].cost + (TypeToCost[tileType] ?: 99)
            if (adjacentTile.parentTile == null || newCost < adjacentTile.cost) {
                adjacentTile.cost = newCost
                adjacentTile.parentTile = tile
                modifiedTiles.add(Position(x, y))
                if (isDebug || player.movement >= newCost) {
                    val key = posKey(Position(x, y))
                    if (visited.add(key)) {
                        accessibleTiles.add(Position(x, y))
                    }
                }
            }
        }
    }

    fun hasPlayer(position: Position, players: List<Player>): Boolean {
        return players.any { it.position.x == position.x && it.position.y == position.y }
    }

    private fun isInMap(position: Position): Boolean {
        return position.x >= 0 && position.y >= 0 && position.x < tiles.size && position.y < tiles[0].size
    }

    private fun posKey(p: Position): Long = p.x.toLong() * 10000 + p.y.toLong()
}
