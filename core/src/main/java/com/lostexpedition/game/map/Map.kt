package com.lostexpedition.game.map

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.tiles.Tile


class Map(
    private val refLink: RefLinks
) {
    private lateinit var tiledMap: TiledMap
    private lateinit var renderer: OrthogonalTiledMapRenderer

    var width: Int = 0
        private set
    var height: Int = 0
        private set

    private val tiles = mutableListOf<MutableList<Tile>>()

    fun loadMapFromFile(path: String) {
        tiledMap = TmxMapLoader().load(path)
        renderer = OrthogonalTiledMapRenderer(tiledMap)

        val properties = tiledMap.properties
        width = properties.get("width", Int::class.java)
        height = properties.get("height", Int::class.java)

        initTiles()
    }

    private fun initTiles() {
        tiles.clear()
        for (y in 0 until height) {
            val row = mutableListOf<Tile>()
            for (x in 0 until width) {
                row.add(Tile(0, false))
            }
            tiles.add(row)
        }

        // Parse TMX layers and populate tiles
        val layers = tiledMap.layers
        for (layer in layers) {
            if (layer is TiledMapTileLayer) {
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val cell = layer.getCell(x, y)
                        if (cell != null) {
                            val gid = cell.tile.id
                            val solid = cell.tile.properties.get("solid", false, Boolean::class.java)
                            tiles[y][x] = Tile(gid, solid)
                        }
                    }
                }
            }
        }
    }

    fun update() {
        // Map update logic if needed
    }

    fun render(batch: SpriteBatch, camera: OrthographicCamera) {
        renderer.setView(camera)
        renderer.render()
    }

    fun getTile(x: Int, y: Int): Tile {
        if (x in 0 until width && y in 0 until height) {
            return tiles[y][x]
        }
        return Tile(0, true) // Return solid tile for out of bounds
    }

    fun changeTileGid(x: Int, y: Int, newGid: Int, layerIndex: Int) {
        if (x in 0 until width && y in 0 until height) {
            tiles[y][x] = Tile(newGid, false)

            // Also update the actual TMX layer
            val layer = tiledMap.layers.get(layerIndex)
            if (layer is TiledMapTileLayer) {
                val cell = layer.getCell(x, y)
                if (cell != null) {
                    val tileset = tiledMap.tileSets.getTileSet(0)
                    cell.tile = tileset.getTile(newGid)
                }
            }
        }
    }


    fun dispose() {
        tiledMap.dispose()
        renderer.dispose()
    }
}
