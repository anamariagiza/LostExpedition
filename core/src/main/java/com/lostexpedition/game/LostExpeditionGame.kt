package com.lostexpedition.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.states.LoadingScreenState
import com.lostexpedition.game.states.State
import com.lostexpedition.game.utils.DebugLogger
import com.lostexpedition.game.utils.RefLinks

class LostExpeditionGame : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch
    private lateinit var refLinks: RefLinks

    override fun create() {
        DebugLogger.log("LostExpeditionGame", "create() - ANDROID MODE")
        batch = SpriteBatch()
        refLinks = RefLinks(this)

        // ✅ SETARE ANDROID: Dezactivăm tastatura și activăm TouchController
        // refLinks.touchController este deja definit în RefLinks
        Gdx.input.inputProcessor = refLinks.touchController

        refLinks.setState(LoadingScreenState(refLinks))
    }

    override fun render() {
        // ✅ ELIMINAT: handleFullscreenToggle() - nu există F11 pe mobil

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

    override fun resize(width: Int, height: Int) {
        // ✅ OPTIMIZARE MOBIL: Calculăm viewport-ul bazat pe ecranul telefonului
        val aspectRatio = width.toFloat() / height.toFloat()
        val gameWidth = 1500f
        val gameHeight = gameWidth / aspectRatio

        refLinks.gameCamera.viewportWidth = gameWidth
        refLinks.gameCamera.viewportHeight = gameHeight
        refLinks.gameCamera.update()

        batch.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun dispose() {
        batch.dispose()
        refLinks.dispose()
        Assets.dispose()
        println("✓ LostExpeditionGame disposed")
    }

    fun getRefLinks(): RefLinks = refLinks
}
