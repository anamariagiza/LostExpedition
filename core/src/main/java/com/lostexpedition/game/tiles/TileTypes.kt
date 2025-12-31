package com.lostexpedition.game.tiles

enum class TileTypes(val gid: Int, val isSolid: Boolean) {
    GRASS(1, false),
    STONE(2, true),
    WATER(3, true),
    DIRT(4, false);

    companion object {
        fun fromGid(gid: Int): TileTypes? {
            return values().find { it.gid == gid }
        }
    }
}
