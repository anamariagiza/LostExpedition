package com.lostexpedition.game.states

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.utils.RefLinks

abstract class State(protected val refLink: RefLinks) {

    companion object {
        var currentState: State? = null  // ← ȘTERGE private

        private var previousState: State? = null

        fun setState(state: State) {
            val old = currentState
            // GameState e ținut viu intenționat în refLink.gameState, dar DOAR pentru
            // tranzițiile care se întorc explicit la aceeași instanță (Pause, Puzzle,
            // WordPuzzle, Settings - toate citesc refLink.gameState / getPreviousState()
            // ca să revină la ea, nu construiesc alta). Pentru orice altă tranziție dintr-un
            // GameState (GameOver, EndGame, sau un GameState nou la "TRY AGAIN"/nivel nou),
            // dispunem normal, altfel se scurge memorie (hartă, fog of war, font) la fiecare
            // moarte sau final de joc.
            val keepsGameStateAlive = old is GameState &&
                (state is PauseState || state is PuzzleState || state is WordPuzzleState || state is SettingsState)
            if (!keepsGameStateAlive) {
                old?.dispose()
            }
            previousState = old
            currentState = state
        }

        fun getPreviousState(): State? = previousState
    }

    abstract fun update(delta: Float)
    abstract fun render(batch: SpriteBatch)

    open fun dispose() {}
}
