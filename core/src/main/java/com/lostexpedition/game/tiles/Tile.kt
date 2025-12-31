package com.lostexpedition.game.tiles

class Tile(
    val gid: Int,
    val isSolid: Boolean
) {
    companion object {
        const val TILE_WIDTH = 32
        const val TILE_HEIGHT = 32

        const val DOOR_OPEN_TOP_LEFT_GID = 72
        const val DOOR_OPEN_TOP_RIGHT_GID = 73
        const val DOOR_OPEN_BOTTOM_LEFT_GID = 118
        const val DOOR_OPEN_BOTTOM_RIGHT_GID = 119
    }
}
