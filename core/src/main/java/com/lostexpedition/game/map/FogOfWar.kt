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
import com.lostexpedition.game.utils.RefLinks

class FogOfWar(
    private val refLink: RefLinks,
    private val mapWidth: Int,
    private val mapHeight: Int
) {
    private val visionRadiusTiles = 5
    private var gradientTexture: Texture

    init {
        gradientTexture = createGradientTexture(256)
        println("DEBUG FogOfWar: Inițializat cu dimensiunile $mapWidth x $mapHeight")
    }

    fun update() {
        // Dacă vrei să faci lumina să pulseze, poți pune logica aici
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
