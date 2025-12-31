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
 * Implementează noțiunea de "ceață de război" (Fog of War) pentru harta jocului.
 * Folosește un gradient radial smooth pentru efectul de "cerc de lumină" dinamic.
 */
class FogOfWar(
    private val refLink: RefLinks,
    private val mapWidth: Int,
    private val mapHeight: Int
) {
    /** Raza de vizibilitate a jucătorului, măsurată în tile-uri */
    private val visionRadius = 10f

    /** Matrice booleană ce stochează dacă o dală a fost sau nu descoperită permanent */
    private val revealed = Array(mapHeight) { BooleanArray(mapWidth) { false } }

    /** Textură pentru gradient radial (cercul de lumină) */
    private val gradientTexture: Texture

    init {
        gradientTexture = createRadialGradient()
        // NU mai apelăm initializeStartArea() aici pentru a evita null pointer
        println("DEBUG FogOfWar: Inițializat cu dimensiunile ${mapWidth}x${mapHeight}")
    }

    /**
     * Creează o textură cu gradient radial pentru efectul de cerc de lumină
     */
    private fun createRadialGradient(): Texture {
        val size = 512 // Dimensiunea texturii (mai mare = mai smooth)
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

                // Gradient cu 3 zone (similar cu Java RadialGradientPaint)
                val alpha = when {
                    normalizedDistance <= 0.7f -> 0f // Transparent în centru și zona apropiată
                    normalizedDistance >= 1.0f -> 0.86f // Opac la margine (220/255 ≈ 0.86)
                    else -> {
                        // Fade smooth între 0.7 și 1.0
                        val fadeProgress = (normalizedDistance - 0.7f) / 0.3f
                        fadeProgress * 0.86f
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

    /**
     * Inițializează zona de start în jurul jucătorului
     */
    private fun initializeStartArea() {
        val player = refLink.player ?: return

        val playerTileX = ((player.x + player.width / 2) / Tile.TILE_WIDTH).toInt()
        val playerTileY = ((player.y + player.height / 2) / Tile.TILE_HEIGHT).toInt()

        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                val dx = x - playerTileX
                val dy = y - playerTileY
                val distance = sqrt((dx * dx + dy * dy).toFloat())

                if (distance <= visionRadius) {
                    revealed[y][x] = true
                }
            }
        }
    }

    /**
     * Actualizează starea de dezvăluire a hărții pe baza poziției jucătorului
     */
    fun update() {
        val player = refLink.player ?: return

        var playerTileX = ((player.x + player.width / 2) / Tile.TILE_WIDTH).toInt()
        var playerTileY = ((player.y + player.height / 2) / Tile.TILE_HEIGHT).toInt()

        // Asigură că coordonatele sunt în limitele hărții
        playerTileX = max(0, min(mapWidth - 1, playerTileX))
        playerTileY = max(0, min(mapHeight - 1, playerTileY))

        // Prima dată când se apelează update, inițializează zona de start
        if (!revealed[playerTileY][playerTileX]) {
            initializeStartArea()
        }

        // Dezvăluie dalele în raza de vizibilitate
        for (yOffset in -visionRadius.toInt()..visionRadius.toInt()) {
            for (xOffset in -visionRadius.toInt()..visionRadius.toInt()) {
                val checkX = playerTileX + xOffset
                val checkY = playerTileY + yOffset

                if (checkX in 0 until mapWidth && checkY in 0 until mapHeight) {
                    val distance = sqrt((xOffset * xOffset + yOffset * yOffset).toFloat())
                    if (distance <= visionRadius) {
                        revealed[checkY][checkX] = true
                    }
                }
            }
        }
    }

    /**
     * Randează efectul de "cerc de lumină" dinamic cu gradient radial smooth
     */
    fun render(batch: SpriteBatch, camera: OrthographicCamera) {
        val player = refLink.player ?: return

        // Verifică dacă batch-ul este în starea drawing
        val wasDrawing = batch.isDrawing

        // Dacă nu era drawing, începe acum
        if (!wasDrawing) {
            batch.begin()
        }

        // Activează blending pentru transparență
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // Calculează poziția pe ecran a jucătorului
        val playerCenterX = player.x + player.width / 2
        val playerCenterY = player.y + player.height / 2

        // Dimensiunea cercului de lumină (în pixeli)
        val lightRadius = visionRadius * Tile.TILE_WIDTH * 2f

        // Desenează cercul de gradient centrat pe jucător
        batch.color = Color.WHITE
        batch.draw(
            gradientTexture,
            playerCenterX - lightRadius / 2f,
            playerCenterY - lightRadius / 2f,
            lightRadius,
            lightRadius
        )

        // Resetează culoarea
        batch.color = Color.WHITE

        // Dacă nu era drawing înainte, termină acum
        if (!wasDrawing) {
            batch.end()
        }
    }

    /**
     * Verifică dacă o dală este vizibilă în prezent (în cercul de lumină)
     */
    fun isTileVisible(x: Int, y: Int): Boolean {
        val player = refLink.player ?: return false
        if (x !in 0 until mapWidth || y !in 0 until mapHeight) return false

        var playerTileX = ((player.x + player.width / 2) / Tile.TILE_WIDTH).toInt()
        var playerTileY = ((player.y + player.height / 2) / Tile.TILE_HEIGHT).toInt()

        playerTileX = max(0, min(mapWidth - 1, playerTileX))
        playerTileY = max(0, min(mapHeight - 1, playerTileY))

        val distance = sqrt(((playerTileX - x) * (playerTileX - x) +
            (playerTileY - y) * (playerTileY - y)).toFloat())
        return distance <= visionRadius
    }

    /**
     * Verifică dacă o dală a fost descoperită permanent (explorată)
     */
    fun isRevealed(tileX: Int, tileY: Int): Boolean {
        if (tileX in 0 until mapWidth && tileY in 0 until mapHeight) {
            return revealed[tileY][tileX]
        }
        return false
    }

    fun isTileRevealed(x: Int, y: Int): Boolean = isRevealed(x, y)

    /**
     * Dezvăluie instantaneu întreaga hartă
     */
    fun revealAll() {
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                revealed[y][x] = true
            }
        }
        println("DEBUG FogOfWar: Toate dalele au fost dezvăluite!")
    }

    fun revealAllTiles() = revealAll()

    /**
     * Resetează ceața de război
     */
    fun reset() {
        for (y in 0 until mapHeight) {
            for (x in 0 until mapWidth) {
                revealed[y][x] = false
            }
        }
        println("DEBUG FogOfWar: Fog of War resetat!")
    }

    fun resetFogOfWar() = reset()

    fun getVisionRadius(): Int = visionRadius.toInt()

    /**
     * Calculează procentajul hărții explorat
     */
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

    /**
     * Eliberează resursele
     */
    fun dispose() {
        gradientTexture.dispose()
    }
}
