package com.lostexpedition.game.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.lostexpedition.game.window.LostExpeditionGame  // ← SCHIMBĂ AICI


object Lwjgl3Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setTitle("Lost Expedition")
            setWindowedMode(1500, 843)
            setResizable(false)
            setForegroundFPS(60)
        }
        Lwjgl3Application(LostExpeditionGame(), config)  // ← ȘI AICI
    }
}
