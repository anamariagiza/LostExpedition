package com.lostexpedition.game.map

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.tiles.Tile

import kotlin.math.sqrt

class FogOfWar(
    private val refLink: RefLinks,
    private val mapWidth: Int,
    private val mapHeight: Int
) {
    private val shapeRenderer = ShapeRenderer()
    private val visibilityRadius = 5
    private val revealed = Array(mapHeight) { BooleanArray(mapWidth) { false } }

    fun update() {
        val player = refLink.player ?: return

        val playerTileX = (player.x / Tile.TILE_WIDTH).toInt()
        val playerTileY = (player.y / Tile.TILE_HEIGHT).toInt()

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                val dx = x - playerTileX
                val dy = y - playerTileY
                val distance = sqrt((dx * dx + dy * dy).toFloat())

                if (distance <= visibilityRadius) {
                    revealed[y][x] = true
                }
            }
        }
    }

    fun render(batch: SpriteBatch, camera: OrthographicCamera) {
        // End batch only if it's currently drawing
        if (batch.isDrawing) {
            batch.end()
        }

        val player = refLink.player ?: run {
            // Restart batch if we ended it
            if (!batch.isDrawing) {
                batch.begin()
            }
            return
        }

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                if (!revealed[y][x]) {
                    shapeRenderer.rect(
                        x * Tile.TILE_WIDTH.toFloat(),
                        y * Tile.TILE_HEIGHT.toFloat(),
                        Tile.TILE_WIDTH.toFloat(),
                        Tile.TILE_HEIGHT.toFloat()
                    )
                }
            }
        }

        shapeRenderer.end()

        // Always restart the batch after rendering fog
        if (!batch.isDrawing) {
            batch.begin()
        }
    }

    fun isRevealed(tileX: Int, tileY: Int): Boolean {
        if (tileX in 0 until mapWidth && tileY in 0 until mapHeight) {
            return revealed[tileY][tileX]
        }
        return false
    }

    fun revealAll() {
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                revealed[y][x] = true
            }
        }
    }

    fun reset() {
        val player = refLink.player ?: return

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                revealed[y][x] = false
            }
        }
    }

    fun dispose() {
        shapeRenderer.dispose()
    }
}
