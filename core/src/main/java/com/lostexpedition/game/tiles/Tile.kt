package com.lostexpedition.game.tiles

/**
 * Tile - Base class for all tile types
 *
 * Represents a single tile in the game map with its properties.
 * This is the base class for specialized tile types (GrassTile, WallTile, etc.)
 *
 * @property gid The global tile ID from the tileset
 * @property isSolid Whether the tile blocks movement
 * @author LostExpedition Team
 */
open class Tile(
    val gid: Int,
    open val isSolid: Boolean
) {
    companion object {
        const val TILE_WIDTH = 32
        const val TILE_HEIGHT = 32

        // Door GIDs
        const val DOOR_OPEN_TOP_LEFT_GID = 72
        const val DOOR_OPEN_TOP_RIGHT_GID = 73
        const val DOOR_OPEN_BOTTOM_LEFT_GID = 118
        const val DOOR_OPEN_BOTTOM_RIGHT_GID = 119
    }

    /**
     * Returns true if this tile is a grass tile
     */
    open fun isGrass(): Boolean = false

    /**
     * Returns true if this tile is a wall tile
     */
    open fun isWall(): Boolean = false

    /**
     * Returns true if this tile is a rock tile
     */
    open fun isRock(): Boolean = false

    /**
     * Returns true if this tile is a door tile
     */
    open fun isDoor(): Boolean = false

    /**
     * Returns true if this tile is a trap tile
     */
    open fun isTrap(): Boolean = false

    /**
     * Returns true if this tile is a top door tile (used for special collision)
     */
    open fun isTopDoor(): Boolean = false
}

/**
 * GrassTile - Represents grass terrain
 *
 * Grass tiles are typically non-solid and allow free movement.
 *
 * @param gid The global tile ID
 * @param solid Whether this specific grass tile is solid (e.g., tall grass)
 */
class GrassTile(gid: Int, solid: Boolean = false) : Tile(gid, solid) {
    override fun isGrass(): Boolean = true
}

/**
 * WallTile - Represents wall obstacles
 *
 * Wall tiles are always solid and block movement.
 *
 * @param gid The global tile ID
 */
class WallTile(gid: Int) : Tile(gid, true) {
    override val isSolid: Boolean = true
    override fun isWall(): Boolean = true
}

/**
 * RockTile - Represents rock obstacles
 *
 * Rock tiles are always solid and block movement.
 *
 * @param gid The global tile ID
 */
class RockTile(gid: Int) : Tile(gid, true) {
    override val isSolid: Boolean = true
    override fun isRock(): Boolean = true
}

/**
 * DoorTile - Represents door tiles
 *
 * Door tiles can be either open (non-solid) or closed (solid).
 *
 * @param gid The global tile ID
 * @param isOpen Whether the door is open
 * @param isTop Whether this is a top door tile (for special collision detection)
 */
class DoorTile(
    gid: Int,
    val isOpen: Boolean = false,
    private val isTop: Boolean = false
) : Tile(gid, !isOpen) {
    override val isSolid: Boolean = !isOpen
    override fun isDoor(): Boolean = true
    override fun isTopDoor(): Boolean = isTop
}

/**
 * TrapTile - Represents trap tiles
 *
 * Trap tiles are typically non-solid but cause damage when stepped on.
 *
 * @param gid The global tile ID
 * @param damage The damage dealt by this trap
 */
class TrapTile(gid: Int, val damage: Int = 10) : Tile(gid, false) {
    override fun isTrap(): Boolean = true
}
