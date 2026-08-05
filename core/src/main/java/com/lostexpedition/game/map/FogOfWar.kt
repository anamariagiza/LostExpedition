package com.lostexpedition.game.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.DebugLogger
import com.lostexpedition.game.utils.RefLinks

/**
 * FogOfWar - Manages visibility and exploration tracking
 *
 * Implements a fog of war system that:
 * - Limits player visibility to a radius around them
 * - Permanently tracks explored tiles (like Java version)
 * - Provides exploration percentage calculation
 *
 * @param refLink Reference to game utilities
 * @param mapWidth Width of the map in tiles
 * @param mapHeight Height of the map in tiles
 * @author LostExpedition Team
 */
class FogOfWar(
    private val refLink: RefLinks,
    private val mapWidth: Int,
    private val mapHeight: Int
) {
    private val visionRadiusTiles = 5
    private var gradientTexture: Texture

    /** Tracks which tiles have been revealed (permanent exploration) */
    private val revealedTiles: Array<BooleanArray> = Array(mapWidth) { BooleanArray(mapHeight) { false } }

    /** Total number of explorable tiles */
    private var totalTiles: Int = mapWidth * mapHeight

    /** Number of currently revealed tiles */
    private var revealedCount: Int = 0

    init {
        gradientTexture = createGradientTexture(256)
        DebugLogger.log("FogOfWar", "Initialized with dimensions $mapWidth x $mapHeight")
    }

    /**
     * Updates the fog of war, revealing tiles near the player
     */
    fun update() {
        val player = refLink.player ?: return

        // Calculate player's tile position
        val playerTileX = (player.x / TileConstants.TILE_SIZE).toInt()
        val playerTileY = (player.y / TileConstants.TILE_SIZE).toInt()

        // Reveal tiles within vision radius
        for (dy in -visionRadiusTiles..visionRadiusTiles) {
            for (dx in -visionRadiusTiles..visionRadiusTiles) {
                val tileX = playerTileX + dx
                val tileY = playerTileY + dy

                // Check bounds
                if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) {
                    continue
                }

                // Check if within circular radius
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                if (distance <= visionRadiusTiles) {
                    // Reveal tile if not already revealed
                    if (!revealedTiles[tileX][tileY]) {
                        revealedTiles[tileX][tileY] = true
                        revealedCount++
                    }
                }
            }
        }
    }

    /**
     * Checks if a specific tile has been revealed/explored
     *
     * @param tileX The X coordinate of the tile
     * @param tileY The Y coordinate of the tile
     * @return True if the tile has been explored
     */
    fun isTileRevealed(tileX: Int, tileY: Int): Boolean {
        if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) {
            return false
        }
        return revealedTiles[tileX][tileY]
    }

    /**
     * Gets the exploration percentage
     *
     * @return Percentage of map explored (0-100)
     */
    fun getExplorationPercentage(): Float {
        return if (totalTiles > 0) {
            (revealedCount.toFloat() / totalTiles.toFloat()) * 100f
        } else {
            0f
        }
    }

    /**
     * Gets the number of revealed tiles
     *
     * @return Count of revealed tiles
     */
    fun getRevealedTileCount(): Int = revealedCount

    /**
     * Gets the total number of tiles
     *
     * @return Total tile count
     */
    fun getTotalTileCount(): Int = totalTiles

    /**
     * Reveals all tiles on the map (cheat/debug function)
     */
    fun revealAllTiles() {
        for (x in 0 until mapWidth) {
            for (y in 0 until mapHeight) {
                if (!revealedTiles[x][y]) {
                    revealedTiles[x][y] = true
                    revealedCount++
                }
            }
        }
        DebugLogger.log("FogOfWar", "All tiles revealed (debug mode)")
    }

    /**
     * Resets the fog of war to unexplored state
     */
    fun resetFogOfWar() {
        for (x in 0 until mapWidth) {
            for (y in 0 until mapHeight) {
                revealedTiles[x][y] = false
            }
        }
        revealedCount = 0
        DebugLogger.log("FogOfWar", "Fog of war reset")
    }

    fun render(batch: SpriteBatch, camera: OrthographicCamera) {
        val player = refLink.player ?: return

        // 1. Oprim batch-ul curent pentru a folosi ShapeRenderer
        if (batch.isDrawing) {
            batch.end()
        }

        // Setăm blending pentru transparență
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Culoarea neagră a ceții
        shapeRenderer.color = Color(0f, 0f, 0f, 0.863f)

        val lightRadius = visionRadiusTiles * TileConstants.TILE_SIZE
        val playerCenterX = player.x + player.width / 2
        val playerCenterY = player.y + player.height / 2

        val lightX = playerCenterX - lightRadius / 2f
        val lightY = playerCenterY - lightRadius / 2f

        val mapWidthPixels = mapWidth * TileConstants.TILE_SIZE
        val mapHeightPixels = mapHeight * TileConstants.TILE_SIZE

        // --- DREPTUNGHIURI NEGRE (Ceata solidă) ---
        // Stânga
        shapeRenderer.rect(0f, 0f, lightX, mapHeightPixels)
        // Dreapta
        shapeRenderer.rect(lightX + lightRadius, 0f, mapWidthPixels - (lightX + lightRadius), mapHeightPixels)
        // Sus
        shapeRenderer.rect(lightX, lightY + lightRadius, lightRadius, mapHeightPixels - (lightY + lightRadius))
        // Jos
        shapeRenderer.rect(lightX, 0f, lightRadius, lightY)

        shapeRenderer.end()
        shapeRenderer.dispose() // Curățăm ShapeRenderer local

        // --- DESENĂM GRADIENTUL ---
        batch.begin() // Deschidem batch-ul
        batch.color = Color.WHITE

        batch.draw(
            gradientTexture,
            lightX,
            lightY,
            lightRadius,
            lightRadius
        )

        // ✅ FIX CRITIC: Închidem batch-ul aici!
        // Dacă nu îl închidem, TouchController (care folosește ShapeRenderer)
        // sau următorul cadru vor crăpa.
        batch.end()
    }

    private fun createGradientTexture(size: Int): Texture {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        val center = size / 2
        val radius = size / 2

        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - center
                val dy = y - center
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                var alpha = 0f
                if (distance < radius) {
                    // Gradient simplu: 0 la centru, 1 la margine
                    alpha = distance / radius
                    // Curbă pentru a face marginea mai 'soft'
                    alpha = Math.pow(alpha.toDouble(), 2.0).toFloat()
                } else {
                    alpha = 1f
                }

                // Limităm alpha la valoarea ceții solide (~0.86) pentru a se îmbina perfect
                val maxAlpha = 220f / 255f
                if (alpha > maxAlpha) alpha = maxAlpha

                pixmap.setColor(0f, 0f, 0f, alpha)
                pixmap.drawPixel(x, y)
            }
        }

        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }

    fun dispose() {
        gradientTexture.dispose()
    }
}
