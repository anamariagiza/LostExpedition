package com.lostexpedition.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.states.LoadingScreenState
import com.lostexpedition.game.states.State
import com.lostexpedition.game.utils.RefLinks

/**
 * LostExpeditionGame - 100% DESKTOP-MATCHED
 * Desktop viewport: 1500x843
 */
class LostExpeditionGame : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch
    private lateinit var refLinks: RefLinks

    override fun create() {
        println("🎮 LostExpeditionGame.create() - DESKTOP MATCHED MODE")
        batch = SpriteBatch()
        refLinks = RefLinks(this)

        refLinks.setState(LoadingScreenState(refLinks))
        println("✓ Initialized with desktop viewport: 1500px width")
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val delta = Gdx.graphics.deltaTime

        if (State.currentState == null) {
            println("⚠️ WARNING: State.currentState is NULL!")
            return
        }

        State.currentState?.update(delta)
        State.currentState?.render(batch)
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
