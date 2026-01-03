package com.lostexpedition.game.map

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.RefLinks

// Constructorul primește levelIndex pentru a ști ce reguli de coliziune să aplice
class Map(private val refLink: RefLinks, path: String, private val levelIndex: Int) {

    val tiledMap: TiledMap = TmxMapLoader().load(path)
    val renderer = OrthogonalTiledMapRenderer(tiledMap)

    val width: Int = (tiledMap.layers[0] as TiledMapTileLayer).width
    val height: Int = (tiledMap.layers[0] as TiledMapTileLayer).height

    fun update() {
        // Logică update hartă
    }

    // ✅ FIX: Am scos parametrul 'batch' care nu era folosit
    fun render(camera: OrthographicCamera) {
        renderer.setView(camera)
        renderer.render()
    }

    fun getTile(x: Int, y: Int): Tile {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Tile(TileConstants.WALL_TILE_SOLID, true)
        }

        val layer = tiledMap.layers[0] as TiledMapTileLayer
        val cell = layer.getCell(x, y)
        val gid = cell?.tile?.id ?: 0

        // Verificăm soliditatea folosind indexul nivelului primit în constructor
        val solid = isSolidInternal(gid, x, y, levelIndex)

        return Tile(gid, solid)
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
