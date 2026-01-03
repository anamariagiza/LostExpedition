package com.lostexpedition.game.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.tiles.Tile
import kotlin.math.sqrt
import kotlin.math.max
import kotlin.math.min

/**
 * FogOfWar - 100% DESKTOP-MATCHED
 * Desktop: VISION_RADIUS_TILES = 5, RadialGradientPaint cu dist={0.0, 0.7, 1.0}
 */
class FogOfWar(
    private val refLink: RefLinks,
    private val mapWidth: Int,
    private val mapHeight: Int
) {
    // ✅ DESKTOP EXACT: Vision radius = 5 tiles (NU 8!)
    private val visionRadiusTiles = 5f

    private val revealed = Array(mapHeight) { BooleanArray(mapWidth) { false } }
    private val gradientTexture: Texture

    init {
        gradientTexture = createRadialGradient()
        println("✓ FogOfWar - DESKTOP MATCHED (radius: 5 tiles)")
    }

    private fun createRadialGradient(): Texture {
        // ✅ DESKTOP EXACT: Gradient matching Java RadialGradientPaint
        // dist = {0.0f, 0.7f, 1.0f}
        // colors = {transparent, transparent, opaque(220)}
        val size = 512
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)

        val centerX = size / 2f
        val centerY = size / 2f
        val maxRadius = size / 2f

        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - centerX
                val dy = y - centerY
                val distance = sqrt(dx * dx + dy * dy)
                val normalizedDistance = distance / maxRadius

                // ✅ Desktop gradient: 0.0→0.7 = transparent, 0.7→1.0 = fade to opaque
                val alpha = when {
                    normalizedDistance <= 0.7f -> 0f  // Transparent zone
                    normalizedDistance >= 1.0f -> 0.863f  // 220/255 = 0.863
                    else -> {
                        // Linear interpolation from 0.7 to 1.0
                        val fadeProgress = (normalizedDistance - 0.7f) / 0.3f
                        fadeProgress * 0.863f
                    }
                }

                pixmap.setColor(0f, 0f, 0f, alpha)
                pixmap.drawPixel(x, y)
            }
        }

        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }

    private fun initializeStartArea() {
        val player = refLink.player ?: return

        val playerTileX = ((player.x + player.width / 2) / Tile.TILE_WIDTH).toInt()
        val playerTileY = ((player.y + player.height / 2) / Tile.TILE_HEIGHT).toInt()

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                val dx = x - playerTileX
                val dy = y - playerTileY
                val distance = sqrt((dx * dx + dy * dy).toFloat())

                if (distance <= visionRadiusTiles) {
                    revealed[y][x] = true
                }
            }
        }
    }

    fun update() {
        val player = refLink.player ?: return

        var playerTileX = ((player.x + player.width / 2) / Tile.TILE_WIDTH).toInt()
        var playerTileY = ((player.y + player.height / 2) / Tile.TILE_HEIGHT).toInt()

        playerTileX = max(0, min(mapWidth - 1, playerTileX))
        playerTileY = max(0, min(mapHeight - 1, playerTileY))

        if (!revealed[playerTileY][playerTileX]) {
            initializeStartArea()
        }

        for (yOffset in -visionRadiusTiles.toInt()..visionRadiusTiles.toInt()) {
            for (xOffset in -visionRadiusTiles.toInt()..visionRadiusTiles.toInt()) {
                val checkX = playerTileX + xOffset
                val checkY = playerTileY + yOffset

                if (checkX in 0 until mapWidth && checkY in 0 until mapHeight) {
                    val distance = sqrt((xOffset * xOffset + yOffset * yOffset).toFloat())
                    if (distance <= visionRadiusTiles) {
                        revealed[checkY][checkX] = true
                    }
                }
            }
        }
    }

    fun render(batch: SpriteBatch, camera: OrthographicCamera) {
        val player = refLink.player ?: return

        val wasBatchDrawing = batch.isDrawing

        if (!wasBatchDrawing) {
            batch.begin()
        }

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        val playerCenterX = player.x + player.width / 2
        val playerCenterY = player.y + player.height / 2

        // ✅ DESKTOP EXACT: radius = VISION_RADIUS_TILES * TILE_WIDTH (fără multiplicatori)
        val lightRadius = visionRadiusTiles * Tile.TILE_WIDTH

        batch.color = Color.WHITE
        batch.draw(
            gradientTexture,
            playerCenterX - lightRadius / 2f,
            playerCenterY - lightRadius / 2f,
            lightRadius,
            lightRadius
        )
        batch.color = Color.WHITE

        if (!wasBatchDrawing) {
            batch.end()
        }
    }

    fun isTileVisible(x: Int, y: Int): Boolean {
        val player = refLink.player ?: return false
        if (x !in 0 until mapWidth || y !in 0 until mapHeight) return false

        var playerTileX = ((player.x + player.width / 2) / Tile.TILE_WIDTH).toInt()
        var playerTileY = ((player.y + player.height / 2) / Tile.TILE_HEIGHT).toInt()

        playerTileX = max(0, min(mapWidth - 1, playerTileX))
        playerTileY = max(0, min(mapHeight - 1, playerTileY))

        val distance = sqrt(((playerTileX - x) * (playerTileX - x) +
            (playerTileY - y) * (playerTileY - y)).toFloat())
        return distance <= visionRadiusTiles
    }

    fun isRevealed(tileX: Int, tileY: Int): Boolean {
        if (tileX in 0 until mapWidth && tileY in 0 until mapHeight) {
            return revealed[tileY][tileX]
        }
        return false
    }

    fun isTileRevealed(x: Int, y: Int): Boolean = isRevealed(x, y)

    fun revealAll() {
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                revealed[y][x] = true
            }
        }
        println("DEBUG FogOfWar: Toate dalele au fost dezvăluite!")
    }

    fun revealAllTiles() = revealAll()

    fun reset() {
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                revealed[y][x] = false
            }
        }
        println("DEBUG FogOfWar: Fog of War resetat!")
    }

    fun resetFogOfWar() = reset()

    fun getVisionRadius(): Int = visionRadiusTiles.toInt()

    fun getExplorationPercentage(): Float {
        val totalTiles = mapWidth * mapHeight
        var revealedCount = 0

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                if (revealed[y][x]) {
                    revealedCount++
                }
            }
        }

        return (revealedCount.toFloat() / totalTiles) * 100f
    }

    fun dispose() {
        gradientTexture.dispose()
    }
}
