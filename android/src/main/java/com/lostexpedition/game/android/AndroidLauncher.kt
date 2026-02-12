package com.lostexpedition.game.android

import android.os.Bundle
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.lostexpedition.game.LostExpeditionGame

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useImmersiveMode = true
        }

        val game = LostExpeditionGame()
        initialize(game, config)

        // Setup InputProcessor for TouchController
        Gdx.app.postRunnable {
            val inputMultiplexer = InputMultiplexer()

            val touchProcessor = object : InputProcessor {
                override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                    try {
                        val controller = game.getRefLinks().touchController
                        // ✅ FIX: Trimitem direct Int și adăugăm parametrul 'button'
                        return controller.touchDown(screenX, screenY, pointer, button)
                    } catch (e: Exception) {
                        return false
                    }
                }

                override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                    try {
                        val controller = game.getRefLinks().touchController
                        // ✅ FIX: Trimitem direct Int și adăugăm parametrul 'button'
                        return controller.touchUp(screenX, screenY, pointer, button)
                    } catch (e: Exception) {
                        return false
                    }
                }

                override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                    try {
                        val controller = game.getRefLinks().touchController
                        // ✅ FIX: Trimitem Int (TouchController se ocupă intern de flip Y)
                        return controller.touchDragged(screenX, screenY, pointer)
                    } catch (e: Exception) {
                        return false
                    }
                }

                override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                    try {
                        val controller = game.getRefLinks().touchController
                        return controller.touchCancelled(screenX, screenY, pointer, button)
                    } catch (e: Exception) {
                        return false
                    }
                }

                override fun mouseMoved(screenX: Int, screenY: Int): Boolean = false
                override fun scrolled(amountX: Float, amountY: Float): Boolean = false
                override fun keyDown(keycode: Int): Boolean = false
                override fun keyUp(keycode: Int): Boolean = false
                override fun keyTyped(character: Char): Boolean = false
            }

            inputMultiplexer.addProcessor(touchProcessor)
            Gdx.input.inputProcessor = inputMultiplexer

            println("✓ InputProcessor configured for TouchController")
        }
    }
}
