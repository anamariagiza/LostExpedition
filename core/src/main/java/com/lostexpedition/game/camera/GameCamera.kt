package com.lostexpedition.game.camera

import com.badlogic.gdx.graphics.OrthographicCamera
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.utils.RefLinks

/**
 * GameCamera - 100% DESKTOP-MATCHED
 * Desktop viewport: 1500x843 (1:1 pixel mapping)
 * Android: Folosește EXACT aceleași dimensiuni pentru același zoom
 */
class GameCamera(
    private val refLink: RefLinks,
    screenWidth: Int,
    screenHeight: Int
) : OrthographicCamera() {

    private var mapWidthPixels = 0f
    private var mapHeightPixels = 0f

    init {
        // ✅ DESKTOP EXACT MATCH: 1500 pixels width (ca Java AWT)
        val aspectRatio = screenWidth.toFloat() / screenHeight.toFloat()

        val gameWidth = 1500f  // ← EXACT ca desktop window width
        val gameHeight = gameWidth / aspectRatio  // ~675f pentru 2400x1080

        setToOrtho(false, gameWidth, gameHeight)

        println("✓ GameCamera - DESKTOP MATCHED")
        println("  Desktop: 1500x843")
        println("  Android viewport: ${gameWidth}x${gameHeight}")
        println("  Screen: ${screenWidth}x${screenHeight}")
        println("  Aspect: $aspectRatio")
    }

    fun setMapBounds(mapWidth: Int, mapHeight: Int) {
        mapWidthPixels = mapWidth * Tile.TILE_WIDTH.toFloat()
        mapHeightPixels = mapHeight * Tile.TILE_HEIGHT.toFloat()
        println("✓ Map bounds: ${mapWidthPixels}x${mapHeightPixels}")
    }

    fun centerOnEntity(entityX: Float, entityY: Float, entityWidth: Float, entityHeight: Float) {
        // ✅ EXACT ca desktop: player centrat în mijlocul viewport-ului
        position.x = entityX + entityWidth / 2f
        position.y = entityY + entityHeight / 2f

        checkBlankSpace()
        update()
    }

    private fun checkBlankSpace() {
        if (mapWidthPixels == 0f || mapHeightPixels == 0f) return

        val halfWidth = viewportWidth / 2f
        val halfHeight = viewportHeight / 2f

        // Clamp X
        if (position.x - halfWidth < 0f) {
            position.x = halfWidth
        } else if (position.x + halfWidth > mapWidthPixels) {
            if (mapWidthPixels < viewportWidth) {
                position.x = mapWidthPixels / 2f
            } else {
                position.x = mapWidthPixels - halfWidth
            }
        }

        // Clamp Y
        if (position.y - halfHeight < 0f) {
            position.y = halfHeight
        } else if (position.y + halfHeight > mapHeightPixels) {
            if (mapHeightPixels < viewportHeight) {
                position.y = mapHeightPixels / 2f
            } else {
                position.y = mapHeightPixels - halfHeight
            }
        }
    }

    fun updateCamera() {
        val player = refLink.player ?: return
        centerOnEntity(player.x, player.y, player.width.toFloat(), player.height.toFloat())
    }

    // ✅ Compatibility methods matching Java getxOffset/getyOffset
    fun getxOffset(): Float = position.x - viewportWidth / 2f
    fun getyOffset(): Float = position.y - viewportHeight / 2f
}
