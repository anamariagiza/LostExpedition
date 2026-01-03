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
           // hideStatusBar = true // ❌ Această linie a fost ștearsă (nu mai există în LibGDX nou) [cite: 1]
        }

        val game = LostExpeditionGame()
        initialize(game, config)

        // ✅ Setup InputProcessor for TouchController
        Gdx.app.postRunnable {
            val inputMultiplexer = InputMultiplexer()

            val touchProcessor = object : InputProcessor {
                override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                    // Folosim try-catch sau verificări pentru siguranță
                    try {
                        val controller = game.getRefLinks().touchController
                        // ⚠️ LibGDX Y coordinate is BOTTOM-UP, flip it
                        val flippedY = Gdx.graphics.height - screenY
                        return controller.touchDown(screenX.toFloat(), flippedY.toFloat(), pointer)
                    } catch (e: Exception) {
                        return false
                    }
                }

                override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                    try {
                        val controller = game.getRefLinks().touchController
                        val flippedY = Gdx.graphics.height - screenY
                        return controller.touchUp(screenX.toFloat(), flippedY.toFloat(), pointer)
                    } catch (e: Exception) {
                        return false
                    }
                }

                override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                    try {
                        val controller = game.getRefLinks().touchController
                        val flippedY = Gdx.graphics.height - screenY
                        return controller.touchDragged(screenX.toFloat(), flippedY.toFloat(), pointer)
                    } catch (e: Exception) {
                        return false
                    }
                }

                // ✅ FIX: Această metodă lipsea și cauza eroarea "Object is not abstract"
                override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                    return false
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
