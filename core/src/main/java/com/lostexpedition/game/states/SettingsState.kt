package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.utils.SettingsManager

class SettingsState(refLink: RefLinks) : State(refLink) {

    private val font = BitmapFont().apply {
        data.setScale(2.5f)
    }

    private val titleFont = BitmapFont().apply {
        data.setScale(3.5f)
        color = Color.GOLD
    }

    private val shapeRenderer = ShapeRenderer()

    // ✅ SALVĂM PROIECȚIA ORIGINALĂ
    private val originalProjectionMatrix = refLink.gameCamera.combined.cpy()

    // Bounds pentru butoane
    private val musicBtnBounds = Rectangle()
    private val soundBtnBounds = Rectangle()
    private val volumeDownBounds = Rectangle()
    private val volumeUpBounds = Rectangle()
    private val backBtnBounds = Rectangle()

    init {
        println("SettingsState initialized")
        calculateLayout()
    }

    private fun calculateLayout() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val centerX = w / 2
        val startY = h * 0.7f
        val gap = 120f

        musicBtnBounds.set(centerX - 200f, startY, 400f, 80f)
        soundBtnBounds.set(centerX - 200f, startY - gap, 400f, 80f)
        volumeDownBounds.set(centerX - 200f, startY - gap * 2, 80f, 80f)
        volumeUpBounds.set(centerX + 120f, startY - gap * 2, 80f, 80f)
        backBtnBounds.set(centerX - 150f, 100f, 300f, 80f)
    }

    override fun update(delta: Float) {
        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            if (musicBtnBounds.contains(touchX, touchY)) {
                SettingsManager.isMusicEnabled = !SettingsManager.isMusicEnabled
            }
            else if (soundBtnBounds.contains(touchX, touchY)) {
                SettingsManager.isSoundEnabled = !SettingsManager.isSoundEnabled
            }
            else if (volumeDownBounds.contains(touchX, touchY)) {
                SettingsManager.masterVolume -= 0.1f
            }
            else if (volumeUpBounds.contains(touchX, touchY)) {
                SettingsManager.masterVolume += 0.1f
            }
            else if (backBtnBounds.contains(touchX, touchY)) {
                // ✅ RESTAURĂM PROIECȚIA ÎNAINTE DE IEȘIRE
                restoreProjection()
                refLink.setState(MenuState(refLink))
            }
        }
    }

    override fun render(batch: SpriteBatch) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        // ✅ SETĂM PROIECȚIA PENTRU UI (NU O PĂSTRĂM PERMANENT)
        val tempProjection = batch.projectionMatrix.cpy()
        batch.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.projectionMatrix = batch.projectionMatrix

        // Desenare butoane
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = if (SettingsManager.isMusicEnabled) Color(0f, 0.6f, 0f, 1f) else Color(0.6f, 0f, 0f, 1f)
        shapeRenderer.rect(musicBtnBounds.x, musicBtnBounds.y, musicBtnBounds.width, musicBtnBounds.height)

        shapeRenderer.color = if (SettingsManager.isSoundEnabled) Color(0f, 0.6f, 0f, 1f) else Color(0.6f, 0f, 0f, 1f)
        shapeRenderer.rect(soundBtnBounds.x, soundBtnBounds.y, soundBtnBounds.width, soundBtnBounds.height)

        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(volumeDownBounds.x, volumeDownBounds.y, volumeDownBounds.width, volumeDownBounds.height)
        shapeRenderer.rect(volumeUpBounds.x, volumeUpBounds.y, volumeUpBounds.width, volumeUpBounds.height)

        shapeRenderer.color = Color(0.2f, 0.2f, 0.8f, 1f)
        shapeRenderer.rect(backBtnBounds.x, backBtnBounds.y, backBtnBounds.width, backBtnBounds.height)

        shapeRenderer.end()

        // Contururi
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(musicBtnBounds.x, musicBtnBounds.y, musicBtnBounds.width, musicBtnBounds.height)
        shapeRenderer.rect(soundBtnBounds.x, soundBtnBounds.y, soundBtnBounds.width, soundBtnBounds.height)
        shapeRenderer.rect(volumeDownBounds.x, volumeDownBounds.y, volumeDownBounds.width, volumeDownBounds.height)
        shapeRenderer.rect(volumeUpBounds.x, volumeUpBounds.y, volumeUpBounds.width, volumeUpBounds.height)
        shapeRenderer.rect(backBtnBounds.x, backBtnBounds.y, backBtnBounds.width, backBtnBounds.height)
        shapeRenderer.end()

        // Text
        batch.begin()

        val title = "SETARI"
        val titleLayout = GlyphLayout(titleFont, title)
        titleFont.draw(batch, title, (Gdx.graphics.width - titleLayout.width) / 2, Gdx.graphics.height - 50f)

        font.color = Color.WHITE
        drawCenteredText(batch, "MUZICA: ${if (SettingsManager.isMusicEnabled) "ON" else "OFF"}", musicBtnBounds)
        drawCenteredText(batch, "SUNET: ${if (SettingsManager.isSoundEnabled) "ON" else "OFF"}", soundBtnBounds)
        drawCenteredText(batch, "-", volumeDownBounds)
        drawCenteredText(batch, "+", volumeUpBounds)

        val volPercent = (SettingsManager.masterVolume * 100).toInt()
        val volText = "$volPercent%"
        val volLayout = GlyphLayout(font, volText)
        val volX = volumeDownBounds.x + volumeDownBounds.width + (volumeUpBounds.x - (volumeDownBounds.x + volumeDownBounds.width) - volLayout.width) / 2
        val volY = volumeDownBounds.y + (volumeDownBounds.height + volLayout.height) / 2
        font.draw(batch, volText, volX, volY)

        drawCenteredText(batch, "INAPOI", backBtnBounds)

        batch.end()

        // ✅ RESTAURĂM PROIECȚIA TEMPORARĂ
        batch.projectionMatrix = tempProjection
    }

    private fun drawCenteredText(batch: SpriteBatch, text: String, rect: Rectangle) {
        val layout = GlyphLayout(font, text)
        val x = rect.x + (rect.width - layout.width) / 2
        val y = rect.y + (rect.height + layout.height) / 2
        font.draw(batch, text, x, y)
    }

    // ✅ FUNCȚIE PENTRU RESTAURAREA PROIECȚIEI
    private fun restoreProjection() {
        refLink.gameCamera.combined.set(originalProjectionMatrix)
        refLink.gameCamera.update()
    }

    override fun dispose() {
        // ✅ LA DISPOSE, DE ASEMENEA RESTAURĂM
        restoreProjection()
        font.dispose()
        titleFont.dispose()
        shapeRenderer.dispose()
    }
}
