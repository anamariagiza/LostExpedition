package com.lostexpedition.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.states.LoadingScreenState
import com.lostexpedition.game.states.State
import com.lostexpedition.game.utils.RefLinks

class LostExpeditionGame : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch
    private lateinit var refLinks: RefLinks

    override fun create() {
        println("🎮 LostExpeditionGame.create() STARTED")
        batch = SpriteBatch()
        refLinks = RefLinks(this)

        // DO NOT load assets here - LoadingScreenState will do it on main thread
        // Assets.load() // ❌ REMOVED - causes double loading and threading issues

        refLinks.setState(LoadingScreenState(refLinks))
        println("✓ LostExpeditionGame initialized - LoadingScreenState set")
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val delta = Gdx.graphics.deltaTime

        // Debug: Check if state exists
        if (State.currentState == null) {
            println("⚠️ WARNING: State.currentState is NULL!")
            return
        }

        State.currentState?.update(delta)
        State.currentState?.render(batch)
    }

    override fun resize(width: Int, height: Int) {
        batch.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
        println("✓ Screen resized to ${width}x${height}")
    }

    override fun dispose() {
        batch.dispose()
        refLinks.dispose()
        Assets.dispose()
        println("✓ LostExpeditionGame disposed")
    }

    fun getRefLinks(): RefLinks = refLinks
}
