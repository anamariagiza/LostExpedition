package com.lostexpedition.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.LostExpeditionGame
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.input.TouchController

/**
 * Test screen to verify all converted components work correctly
 */
class TestScreen(val game: LostExpeditionGame) : Screen {

    private lateinit var touchController: TouchController
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont

    private var assetsLoaded = false
    private var loadingStarted = false
    private var errorMessage = ""

    override fun show() {
        println("=== TestScreen: Initializing ===")

        // Initialize rendering
        batch = SpriteBatch()
        font = BitmapFont()
        font.color = Color.WHITE
        font.data.setScale(2f)

        // Initialize TouchController
        val screenWidth = Gdx.graphics.width
        val screenHeight = Gdx.graphics.height
        // ✅ FIX: Apel constructor corect cu dimensiuni
        touchController = TouchController(screenWidth, screenHeight)

        // Set up input processor
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                return touchController.touchDown(screenX.toFloat(), (Gdx.graphics.height - screenY).toFloat(), pointer)
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                return touchController.touchUp(screenX.toFloat(), (Gdx.graphics.height - screenY).toFloat(), pointer)
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                return touchController.touchDragged(screenX.toFloat(), (Gdx.graphics.height - screenY).toFloat(), pointer)
            }
        }

        println("=== TestScreen: Initialized successfully ===")
    }

    override fun render(delta: Float) {
        // Clear screen with dark blue
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Start loading assets on first frame
        if (!loadingStarted) {
            loadingStarted = true
            try {
                println("=== Starting Asset Loading ===")
                Assets.load()
                assetsLoaded = true
                println("=== Assets Loaded Successfully! ===")
            } catch (e: Exception) {
                errorMessage = "Asset loading failed: ${e.message}"
                System.err.println(errorMessage)
                e.printStackTrace()
            }
        }

        // Draw TouchController
        touchController.draw()

        // Get input state
        // ✅ FIX: Apeluri corecte la metodele adăugate în TouchController
        val moveDir = touchController.getMoveDirection()
        val jumpPressed = touchController.isJumpPressed()
        // ✅ FIX: Accesare proprietăți fără paranteze
        val attackPressed = touchController.isAttackPressed
        val interactPressed = touchController.isInteractPressed

        // Draw UI text
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
        } else {
            font.color = Color.YELLOW
            font.draw(batch, "Assets: Loading...", 50f, y)
        }
        y -= 60f

        font.color = Color.WHITE
        font.draw(batch, "Touch Controller Status:", 50f, y)
        y -= 40f

        font.draw(batch, "Move Direction: (${String.format("%.2f", moveDir.x)}, ${String.format("%.2f", moveDir.y)})", 50f, y)
        y -= 40f

        font.color = if (jumpPressed) Color.GREEN else Color.GRAY
        font.draw(batch, "Jump: ${if (jumpPressed) "PRESSED" else "released"}", 50f, y)
        y -= 40f

        font.color = if (attackPressed) Color.GREEN else Color.GRAY
        font.draw(batch, "Attack: ${if (attackPressed) "PRESSED" else "released"}", 50f, y)
        y -= 40f

        font.color = if (interactPressed) Color.GREEN else Color.GRAY
        font.draw(batch, "Interact: ${if (interactPressed) "PRESSED" else "released"}", 50f, y)
        y -= 60f

        font.color = Color.CYAN
        font.draw(batch, "Screen: ${Gdx.graphics.width}x${Gdx.graphics.height}", 50f, y)
        y -= 40f
        font.draw(batch, "FPS: ${Gdx.graphics.framesPerSecond}", 50f, y)

        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        println("=== TestScreen: Resized to ${width}x${height} ===")
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        touchController.dispose()
        batch.dispose()
        font.dispose()
        Assets.dispose()
        println("=== TestScreen: Disposed ===")
    }
}
