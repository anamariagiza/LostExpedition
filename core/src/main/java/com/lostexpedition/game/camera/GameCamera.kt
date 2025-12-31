package com.lostexpedition.game.camera

import com.badlogic.gdx.graphics.OrthographicCamera
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.utils.RefLinks

class GameCamera(
    private val refLink: RefLinks,
    screenWidth: Int,
    screenHeight: Int
) : OrthographicCamera() {

    private var mapWidthPixels = 0f
    private var mapHeightPixels = 0f

    init {
        setToOrtho(false, screenWidth.toFloat(), screenHeight.toFloat())
    }

    fun setMapBounds(mapWidth: Int, mapHeight: Int) {
        mapWidthPixels = mapWidth * Tile.TILE_WIDTH.toFloat()
        mapHeightPixels = mapHeight * Tile.TILE_HEIGHT.toFloat()
    }

    fun centerOnEntity(entityX: Float, entityY: Float, entityWidth: Float, entityHeight: Float) {
        position.x = entityX + entityWidth / 2f
        position.y = entityY + entityHeight / 2f

        clampToMapBounds()
        update()
    }

    private fun clampToMapBounds() {
        if (mapWidthPixels == 0f || mapHeightPixels == 0f) return

        val halfWidth = viewportWidth / 2f
        val halfHeight = viewportHeight / 2f

        if (position.x - halfWidth < 0f) {
            position.x = halfWidth
        }
        if (position.x + halfWidth > mapWidthPixels) {
            position.x = mapWidthPixels - halfWidth
        }

        if (position.y - halfHeight < 0f) {
            position.y = halfHeight
        }
        if (position.y + halfHeight > mapHeightPixels) {
            position.y = mapHeightPixels - halfHeight
        }
    }

    fun updateCamera() {
        val player = refLink.player ?: return
        centerOnEntity(player.x, player.y, player.width.toFloat(), player.height.toFloat())
    }
}
