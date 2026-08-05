package com.lostexpedition.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.lostexpedition.game.LostExpeditionGame
import com.lostexpedition.game.utils.SettingsManager

class MainMenuScreen(val game: LostExpeditionGame) : Screen {

    init {
        // --- TESTUL PENTRU TASK-UL DE AZI ---
        println("--- START TEST KOTLIN ---")
        println("Sunet activ? ${SettingsManager.isSoundEnabled}")

        SettingsManager.isSoundEnabled = !SettingsManager.isSoundEnabled
        println("Am inversat setarea!")

        println("Sunet acum? ${SettingsManager.isSoundEnabled}")
        println("--- END TEST ---")
    }

    override fun render(delta: Float) {
        // Facem ecranul VERDE ca să știm că e codul nou
        Gdx.gl.glClearColor(0f, 0.5f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {}
}
