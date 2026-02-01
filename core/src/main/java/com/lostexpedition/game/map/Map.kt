package com.lostexpedition.game.map

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.tiles.TileFactory
import com.lostexpedition.game.utils.DebugLogger
import com.lostexpedition.game.utils.RefLinks

/**
 * Map - Manages the game map loaded from TMX files
 *
 * Handles map rendering, tile retrieval, and collision detection.
 * Uses TileFactory for efficient tile caching with Flyweight pattern.
 *
 * @param refLink Reference to game utilities
 * @param path Path to the TMX map file
 * @param levelIndex The level index (0, 1, 2) for level-specific collision rules
 * @author LostExpedition Team
 */
class Map(private val refLink: RefLinks, path: String, private val levelIndex: Int) {

    val tiledMap: TiledMap = TmxMapLoader().load(path)
    val renderer = OrthogonalTiledMapRenderer(tiledMap)

    val width: Int = (tiledMap.layers[0] as TiledMapTileLayer).width
    val height: Int = (tiledMap.layers[0] as TiledMapTileLayer).height

    init {
        DebugLogger.log("Map", "Loaded map: $path (${width}x${height} tiles, level $levelIndex)")
        // Clear tile cache when loading new map
        TileFactory.clearCache()
    }

    /**
     * Updates the map state
     */
    fun update() {
        // Map update logic (if needed)
    }

    /**
     * Renders the map
     * @param camera The camera to render with
     */
    fun render(camera: OrthographicCamera) {
        renderer.setView(camera)
        renderer.render()
    }

    /**
     * Gets a tile at the specified coordinates using TileFactory
     *
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @return The Tile at that position
     */
    fun getTile(x: Int, y: Int): Tile {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return TileFactory.createWallTile(TileConstants.WALL_TILE_SOLID)
        }

        val layer = tiledMap.layers[0] as TiledMapTileLayer
        val cell = layer.getCell(x, y)
        val gid = cell?.tile?.id ?: 0

        // Use TileFactory for efficient tile creation with caching
        return TileFactory.getTile(gid, levelIndex)
    }

    /**
     * Checks if a tile at the given coordinates is solid
     *
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @return True if the tile is solid
     */
    fun isTileSolid(x: Int, y: Int): Boolean {
        return getTile(x, y).isSolid
    }

    /**
     * Checks if a tile is a trap tile
     *
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @return True if the tile is a trap
     */
    fun isTrapTile(x: Int, y: Int): Boolean {
        return getTile(x, y).isTrap()
    }

    /**
     * Checks if a tile is a door tile
     *
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @return True if the tile is a door
     */
    fun isDoorTile(x: Int, y: Int): Boolean {
        return getTile(x, y).isDoor()
    }

    /**
     * Checks if a tile is a top door tile (for special collision detection)
     *
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @return True if the tile is a top door
     */
    fun isTopDoorTile(x: Int, y: Int): Boolean {
        return getTile(x, y).isTopDoor()
    }

    /**
     * Gets the GID of a tile at the specified coordinates
     *
     * @param x Tile X coordinate
     * @param y Tile Y coordinate
     * @return The tile GID or 0 if out of bounds
     */
    fun getTileGid(x: Int, y: Int): Int {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0

        val layer = tiledMap.layers[0] as TiledMapTileLayer
        val cell = layer.getCell(x, y)
        return cell?.tile?.id ?: 0
    }

    fun changeTileGid(x: Int, y: Int, newGid: Int, layerIndex: Int = 0) {
        if (layerIndex >= tiledMap.layers.count) return

        val layer = tiledMap.layers[layerIndex] as? TiledMapTileLayer ?: return
        val cell = layer.getCell(x, y)

        if (cell != null) {
            val tile = tiledMap.tileSets.getTile(newGid)
            if (tile != null) {
                cell.tile = tile
            }
        }
    }

    private fun isSolidInternal(gid: Int, x: Int, y: Int, levelIndex: Int): Boolean {
        if (gid == 0) return false

        if (gid in TileConstants.ROCK_GIDS) return true

        when (levelIndex) {
            0 -> { // Nivel 1
                if (gid == TileConstants.GRASS_TILE_SOLID) return true
                if (gid == TileConstants.WALL_TILE_SOLID) return true
            }
            1 -> { // Nivel 2
                if (gid == TileConstants.WALL_TILE_SOLID) return true
                if (gid in TileConstants.DOOR_CLOSED_GIDS) return true
            }
            2 -> { // Nivel 3
                if (tiledMap.layers.count > 2) {
                    val objectLayer = tiledMap.layers[2] as? TiledMapTileLayer
                    val objCell = objectLayer?.getCell(x, y)
                    val objGid = objCell?.tile?.id ?: 0
                    if (objGid != 0 && objGid !in TileConstants.DOOR_OPEN_GIDS_L3) {
                        return true
                    }
                }
                if (gid == TileConstants.WALL_LEVEL3) return true
                if (gid in TileConstants.DOOR_FINAL_CLOSED_GIDS) return true
            }
        }
        return false
    }

    fun dispose() {
        tiledMap.dispose()
        renderer.dispose()
    }
}
