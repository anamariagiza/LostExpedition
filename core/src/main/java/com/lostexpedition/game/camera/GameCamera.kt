package com.lostexpedition.game.camera

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.RefLinks

class GameCamera(
    private val refLink: RefLinks,
    screenWidth: Int,
    screenHeight: Int
) : OrthographicCamera() {

    private var mapWidthPixels = 0f
    private var mapHeightPixels = 0f

    init {
        // Păstrăm logica de zoom desktop (1500px lățime vizibilă)
        val aspectRatio = screenWidth.toFloat() / screenHeight.toFloat()
        val gameWidth = 1500f
        val gameHeight = gameWidth / aspectRatio
        setToOrtho(false, gameWidth, gameHeight)
    }

    // Această funcție este apelată din GameState când se încarcă harta
    fun setMapBounds(widthInTiles: Int, heightInTiles: Int) {
        mapWidthPixels = widthInTiles * TileConstants.TILE_SIZE
        mapHeightPixels = heightInTiles * TileConstants.TILE_SIZE
    }

    fun updateCamera() {
        val player = refLink.player ?: return

        var targetX = player.x + player.width / 2
        var targetY = player.y + player.height / 2

        // Calculăm jumătatea ecranului
        val halfW = viewportWidth / 2
        val halfH = viewportHeight / 2

        // Aici blocăm camera să nu iasă din hartă
        // mapWidthPixels a fost setat la Pasul 2 prin setMapBounds()
        if (mapWidthPixels > viewportWidth) {
            targetX = MathUtils.clamp(targetX, halfW, mapWidthPixels - halfW)
        }
        if (mapHeightPixels > viewportHeight) {
            targetY = MathUtils.clamp(targetY, halfH, mapHeightPixels - halfH)
        }

        position.set(targetX, targetY, 0f)
        update()
    }
}
