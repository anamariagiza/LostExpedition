package com.lostexpedition.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.states.LoadingScreenState
import com.lostexpedition.game.states.State
import com.lostexpedition.game.utils.DebugLogger
import com.lostexpedition.game.utils.RefLinks

/**
 * LostExpeditionGame - Main game class
 *
 * Desktop viewport: 1500x843
 * Supports fullscreen toggle with F11 key
 *
 * @author LostExpedition Team
 */
class LostExpeditionGame : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch
    private lateinit var refLinks: RefLinks
    private var isFullscreen = false

    override fun create() {
        DebugLogger.log("LostExpeditionGame", "create() - DESKTOP MATCHED MODE")
        batch = SpriteBatch()
        refLinks = RefLinks(this)

        refLinks.setState(LoadingScreenState(refLinks))
        DebugLogger.log("LostExpeditionGame", "Initialized with desktop viewport: 1500px width")
    }

    override fun render() {
        // Handle fullscreen toggle (F11 key)
        handleFullscreenToggle()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val delta = Gdx.graphics.deltaTime

        if (State.currentState == null) {
            DebugLogger.warn("LostExpeditionGame", "State.currentState is NULL!")
            return
        }

        State.currentState?.update(delta)
        State.currentState?.render(batch)
    }

    /**
     * Handles fullscreen toggle with F11 key (like Java version)
     */
    private fun handleFullscreenToggle() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            isFullscreen = !isFullscreen
            if (isFullscreen) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
                DebugLogger.log("LostExpeditionGame", "Switched to fullscreen mode")
            } else {
                Gdx.graphics.setWindowedMode(1500, 843)
                DebugLogger.log("LostExpeditionGame", "Switched to windowed mode (1500x843)")
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        // ✅ DESKTOP EXACT: 1500px viewport width (ca Java AWT)
        val aspectRatio = width.toFloat() / height.toFloat()
        val gameWidth = 1500f  // ← EXACT ca desktop
        val gameHeight = gameWidth / aspectRatio  // ~675f pentru 2400x1080

        refLinks.gameCamera.viewportWidth = gameWidth
        refLinks.gameCamera.viewportHeight = gameHeight
        refLinks.gameCamera.update()

        // Update batch projection pentru UI rendering
        batch.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())

        println("✓ Resize: ${width}x${height} → viewport ${gameWidth}x${gameHeight}")
    }

    override fun dispose() {
        batch.dispose()
        refLinks.dispose()
        Assets.dispose()
        println("✓ LostExpeditionGame disposed")
    }

    fun getRefLinks(): RefLinks = refLinks
}
