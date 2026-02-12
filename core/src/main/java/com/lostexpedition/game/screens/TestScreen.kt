package com.lostexpedition.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.lostexpedition.game.LostExpeditionGame
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.input.TouchController

class TestScreen(val game: LostExpeditionGame) : Screen {

    private lateinit var touchController: TouchController
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont

    private var assetsLoaded = false
    private var loadingStarted = false
    private var errorMessage = ""

    override fun show() {
        batch = SpriteBatch()
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(2f)

        val screenWidth = Gdx.graphics.width
        val screenHeight = Gdx.graphics.height
        touchController = TouchController(screenWidth, screenHeight)

        // ✅ REPARAT: Semnăturile metodelor trebuie să se potrivească cu TouchController
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                return touchController.touchDown(screenX, screenY, pointer, button)
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                return touchController.touchUp(screenX, screenY, pointer, button)
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                return touchController.touchDragged(screenX, screenY, pointer)
            }
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (!loadingStarted) {
            loadingStarted = true
            try {
                Assets.load()
                assetsLoaded = true
            } catch (e: Exception) {
                errorMessage = "Asset loading failed: ${e.message}"
            }
        }

        touchController.update() // ✅ Adăugat update()
        touchController.draw()

        // ✅ REPARAT: Folosim noile proprietăți din TouchController
        val moveDirX = touchController.joystickDeltaX
        val moveDirY = touchController.joystickDeltaY
        val jumpPressed = touchController.isInteractPressed
        val attackPressed = touchController.isAttackPressed
        val interactPressed = touchController.isInteractPressed

        batch.begin()
        var y = Gdx.graphics.height - 50f
        font.draw(batch, "=== LOST EXPEDITION - TEST SCREEN ===", 50f, y)
        y -= 60f

        if (assetsLoaded) {
            font.color = Color.GREEN
            font.draw(batch, "Assets: LOADED!", 50f, y)
        } else if (errorMessage.isNotEmpty()) {
            font.color = Color.RED
            font.draw(batch, "Assets: ERROR", 50f, y)
            y -= 40f
            font.draw(batch, errorMessage, 50f, y)
        }
        y -= 60f

        font.color = Color.WHITE
        font.draw(batch, "Touch Controller Status:", 50f, y)
        y -= 40f

        font.draw(batch, "Move Direction: (${String.format("%.2f", moveDirX)}, ${String.format("%.2f", moveDirY)})", 50f, y)
        y -= 40f

        font.color = if (jumpPressed) Color.GREEN else Color.GRAY
        font.draw(batch, "Jump: ${if (jumpPressed) "PRESSED" else "released"}", 50f, y)
        y -= 40f

        font.color = if (attackPressed) Color.GREEN else Color.GRAY
        font.draw(batch, "Attack: ${if (attackPressed) "PRESSED" else "released"}", 50f, y)
        y -= 40f

        font.color = if (interactPressed) Color.GREEN else Color.GRAY
        font.draw(batch, "Interact: ${if (interactPressed) "PRESSED" else "released"}", 50f, y)
        batch.end()
    }

    // ... (restul metodelor resize, pause, etc., neschimbate)

    override fun dispose() {
        touchController.dispose()
        batch.dispose()
        font.dispose()
        Assets.dispose()
    }

    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
}
