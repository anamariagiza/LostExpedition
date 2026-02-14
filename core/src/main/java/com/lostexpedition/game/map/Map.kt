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
 */
class Map(private val refLink: RefLinks, path: String, private val levelIndex: Int) {

    val tiledMap: TiledMap = TmxMapLoader().load(path)
    val renderer = OrthogonalTiledMapRenderer(tiledMap)

    val width: Int = (tiledMap.layers[0] as TiledMapTileLayer).width
    val height: Int = (tiledMap.layers[0] as TiledMapTileLayer).height

    init {
        DebugLogger.log("Map", "Loaded map: $path (${width}x${height} tiles, level $levelIndex)")
        TileFactory.clearCache()
    }

    fun update() { }

    fun render(camera: OrthographicCamera) {
        renderer.setView(camera)
        renderer.render()
    }

    /**
     * NOU: Parcurgem toate layerele hărții. Dacă GĂSIM un element SOLID pe orice layer,
     * atunci blocăm trecerea. (Asta rezolvă problema ușilor de pe layer-ul 3).
     */
    fun getTile(x: Int, y: Int): Tile {
        // Dacă ieșim de pe hartă, ne lovim de un perete
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Tile(33, true)
        }

        var finalIsSolid = false
        var topGid = 0

        // Citim straturile de la cel mai de jos (0) la cel mai de sus
        for (layer in tiledMap.layers) {
            if (layer is TiledMapTileLayer) {
                val cell = layer.getCell(x, y)
                if (cell != null && cell.tile != null && cell.tile.id != 0) {
                    topGid = cell.tile.id

                    // Dala desenată ultima (deasupra) DICTEAZĂ REGULA!
                    // Astfel, dacă scara e peste perete, isSolid devine FALSE.
                    finalIsSolid = TileFactory.getTile(topGid, levelIndex).isSolid
                }
            }
        }

        if (topGid == 0) return Tile(0, false)
        return Tile(topGid, finalIsSolid)
    }

    fun isTileSolid(x: Int, y: Int): Boolean {
        return getTile(x, y).isSolid
    }

    fun isTrapTile(x: Int, y: Int): Boolean {
        return getTile(x, y).isTrap()
    }

    fun isDoorTile(x: Int, y: Int): Boolean {
        // Căutăm explicit pe layer-ul de obiecte (care la tine se numește "objects" sau index 1/2)
        // O variantă mai robustă:
        for (layer in tiledMap.layers) {
            if (layer is TiledMapTileLayer) {
                val cell = layer.getCell(x, y)
                if (cell != null && cell.tile != null) {
                    val tile = TileFactory.getTile(cell.tile.id, levelIndex)
                    if (tile.isDoor()) return true
                }
            }
        }
        return false
    }

    fun isTopDoorTile(x: Int, y: Int): Boolean {
        return getTile(x, y).isTopDoor()
    }

    fun getTileGid(x: Int, y: Int): Int {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0

        // Returnăm cel mai "de sus" GID util
        for (i in tiledMap.layers.count - 1 downTo 0) {
            val layer = tiledMap.layers[i]
            if (layer is TiledMapTileLayer) {
                val cell = layer.getCell(x, y)
                if (cell != null && cell.tile != null) {
                    return cell.tile.id
                }
            }
        }
        return 0
    }

    fun changeTileGid(x: Int, y: Int, newGid: Int, layerIndex: Int = 1) { // 1 e de obicei objects layer
        // AICI ESTE UN FIX IMPORTANT: Funcția originală deschidea ușa pe `layerIndex: 0`, adică pe iarbă!
        // Ușa e pe layer-ul de deasupra (cum ați pus-o în Tiled). Așa că forțăm căutarea layer-ului corect.

        var targetLayer: TiledMapTileLayer? = null

        // Găsim layer-ul unde este ușa închisă momentan
        for (layer in tiledMap.layers) {
            if (layer is TiledMapTileLayer) {
                val cell = layer.getCell(x, y)
                if (cell != null && cell.tile != null && cell.tile.id != 0) {
                    targetLayer = layer
                }
            }
        }

        if (targetLayer != null) {
            val cell = targetLayer.getCell(x, y)
            if (cell != null) {
                if (newGid == 0) {
                    // Dacă GID=0 înseamnă că distrugem ușa, golim celula
                    cell.tile = null
                } else {
                    val tile = tiledMap.tileSets.getTile(newGid)
                    if (tile != null) {
                        cell.tile = tile
                    }
                }
            }
        }
    }

    fun dispose() {
        tiledMap.dispose()
        renderer.dispose()
    }
}
