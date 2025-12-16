package com.lostexpedition.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lostexpedition.game.screens.MainMenuScreen

class LostExpeditionGame : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()
        // Pornim direct ecranul de meniu
        setScreen(MainMenuScreen(this))
    }

    override fun render() {
        super.render() // Important!
    }

    override fun dispose() {
        batch.dispose()
    }
}
