package com.lostexpedition.game.states

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.utils.RefLinks

abstract class State(protected val refLink: RefLinks) {

    companion object {
        var currentState: State? = null  // ← ȘTERGE private

        private var previousState: State? = null

        fun setState(state: State) {
            previousState = currentState
            currentState = state
        }

        fun getPreviousState(): State? = previousState
    }

    abstract fun update(delta: Float)
    abstract fun render(batch: SpriteBatch)

    open fun dispose() {}
}
