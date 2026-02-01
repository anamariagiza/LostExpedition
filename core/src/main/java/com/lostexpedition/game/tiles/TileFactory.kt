package com.lostexpedition.game.tiles

/**
 * TileFactory - Factory for creating tiles with Flyweight pattern
 *
 * Uses the Flyweight design pattern to cache and reuse tile instances,
 * reducing memory allocation for frequently used tile types.
 *
 * Based on the Java version's tile caching system.
 *
 * @author LostExpedition Team
 */
object TileFactory {

    /** Cache for tile instances (Flyweight pattern) */
    private val tileCache = mutableMapOf<Int, Tile>()

    /** Maximum cache size to prevent memory issues */
    private const val MAX_CACHE_SIZE = 1000

    /**
     * Gets or creates a tile for the given GID
     *
     * @param gid The global tile ID
     * @param levelIndex The current level index for context-specific tile behavior
     * @return A Tile instance (may be cached)
     */
    fun getTile(gid: Int, levelIndex: Int = 0): Tile {
        // Check cache first
        val cacheKey = gid * 10 + levelIndex
        tileCache[cacheKey]?.let { return it }

        // Create appropriate tile type
        val tile = createTile(gid, levelIndex)

        // Cache if under limit
        if (tileCache.size < MAX_CACHE_SIZE) {
            tileCache[cacheKey] = tile
        }

        return tile
    }

    /**
     * Creates a new tile of the appropriate type based on GID
     *
     * @param gid The global tile ID
     * @param levelIndex The current level index
     * @return A new Tile instance
     */
    private fun createTile(gid: Int, levelIndex: Int): Tile {
        // Empty tile
        if (gid == 0) {
            return Tile(0, false)
        }

        // Rock tiles (always solid)
        if (gid in TileConstants.ROCK_GIDS) {
            return RockTile(gid)
        }

        // Closed door tiles (solid)
        if (gid in TileConstants.DOOR_CLOSED_GIDS) {
            val isTopDoor = isTopDoorGid(gid)
            return DoorTile(gid, isOpen = false, isTop = isTopDoor)
        }

        // Open door tiles (non-solid)
        if (gid in TileConstants.DOOR_OPEN_GIDS_L3) {
            val isTopDoor = isTopDoorGid(gid)
            return DoorTile(gid, isOpen = true, isTop = isTopDoor)
        }

        // Final closed doors (Level 3)
        if (gid in TileConstants.DOOR_FINAL_CLOSED_GIDS) {
            val isTopDoor = isTopDoorGid(gid)
            return DoorTile(gid, isOpen = false, isTop = isTopDoor)
        }

        // Wall tiles
        if (gid == TileConstants.WALL_TILE_SOLID || gid == TileConstants.WALL_LEVEL3) {
            return WallTile(gid)
        }

        // Grass tiles (solid in Level 1)
        if (gid == TileConstants.GRASS_TILE_SOLID) {
            val solid = levelIndex == 0 // Solid only in Level 1
            return GrassTile(gid, solid)
        }

        // Trap tile GIDs (if defined)
        if (isTrapTileGid(gid)) {
            return TrapTile(gid)
        }

        // Default: non-solid tile
        return Tile(gid, false)
    }

    /**
     * Checks if a GID represents a top door tile
     *
     * @param gid The tile GID
     * @return True if this is a top door tile
     */
    private fun isTopDoorGid(gid: Int): Boolean {
        // Top door tiles are typically the upper parts of door graphics
        return gid == Tile.DOOR_OPEN_TOP_LEFT_GID ||
            gid == Tile.DOOR_OPEN_TOP_RIGHT_GID ||
            gid == 56 || gid == 57 || // Closed top door tiles
            gid == 70 || gid == 71    // Final closed top door tiles
    }

    /**
     * Checks if a GID represents a trap tile
     *
     * @param gid The tile GID
     * @return True if this is a trap tile
     */
    private fun isTrapTileGid(gid: Int): Boolean {
        // Define trap tile GIDs here based on your tileset
        return false // Extend with actual trap GIDs
    }

    /**
     * Creates a grass tile
     *
     * @param gid The tile GID
     * @param solid Whether the grass is solid
     * @return A GrassTile instance
     */
    fun createGrassTile(gid: Int, solid: Boolean = false): GrassTile {
        return GrassTile(gid, solid)
    }

    /**
     * Creates a wall tile
     *
     * @param gid The tile GID
     * @return A WallTile instance
     */
    fun createWallTile(gid: Int): WallTile {
        return WallTile(gid)
    }

    /**
     * Creates a rock tile
     *
     * @param gid The tile GID
     * @return A RockTile instance
     */
    fun createRockTile(gid: Int): RockTile {
        return RockTile(gid)
    }

    /**
     * Creates a door tile
     *
     * @param gid The tile GID
     * @param isOpen Whether the door is open
     * @param isTop Whether this is a top door tile
     * @return A DoorTile instance
     */
    fun createDoorTile(gid: Int, isOpen: Boolean, isTop: Boolean = false): DoorTile {
        return DoorTile(gid, isOpen, isTop)
    }

    /**
     * Creates a trap tile
     *
     * @param gid The tile GID
     * @param damage The damage dealt by this trap
     * @return A TrapTile instance
     */
    fun createTrapTile(gid: Int, damage: Int = 10): TrapTile {
        return TrapTile(gid, damage)
    }

    /**
     * Clears the tile cache
     * Call this when changing levels to free memory
     */
    fun clearCache() {
        tileCache.clear()
    }

    /**
     * Returns the current cache size
     */
    fun getCacheSize(): Int = tileCache.size
}
