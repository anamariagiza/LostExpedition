package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks

class LoadingScreenState(refLink: RefLinks) : State(refLink) {

    private var progress = 0f
    private var loadingStarted = false
    private var assetsLoaded = false

    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont().apply {
        data.setScale(2f)
        color = Color.WHITE
    }

    init {
        println("✓ LoadingScreenState initialized")
    }

    override fun update(delta: Float) {
        // Load assets on MAIN THREAD (LibGDX requirement for Texture loading)
        if (!loadingStarted) {
            println("🎮 LoadingScreenState: Starting asset loading on main thread...")
            loadingStarted = true
        }

        if (!assetsLoaded && loadingStarted) {
            try {
                println("Loading all game assets...")
                Assets.load()
                assetsLoaded = true
                progress = 1.0f
                println("✓ Assets loaded successfully!")

                // Verify critical assets
                if (Assets.backgroundMenu != null) {
                    println("✓ Menu background loaded")
                } else {
                    println("⚠️ Menu background is NULL")
                }

                if (Assets.playerIdleDown != null) {
                    println("✓ Player animations loaded")
                } else {
                    println("⚠️ Player animations are NULL")
                }

            } catch (e: Exception) {
                System.err.println("❌ ERROR loading assets: ${e.message}")
                e.printStackTrace()
                progress = -1f
            }
        }

        if (progress >= 1.0f && assetsLoaded) {
            println("✅ Transitioning to MenuState...")
            refLink.setState(MenuState(refLink))
        } else if (progress < 0f) {
            println("❌ Loading FAILED - staying on loading screen")
        }
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        // Clear screen with dark blue
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Draw loading bar background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.3f, 0.3f, 0.3f, 1f)
        shapeRenderer.rect(width / 4f, height / 2f - 25f, width / 2f, 50f)
        shapeRenderer.end()

        // Draw loading bar progress
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.2f, 0.8f, 0.2f, 1f)
        shapeRenderer.rect(width / 4f, height / 2f - 25f, (width / 2f) * progress, 50f)
        shapeRenderer.end()

        // Draw loading text
        batch.begin()
        val loadingText = if (progress >= 0f) {
            "Loading... ${(progress * 100).toInt()}%"
        } else {
            "Loading failed - check console"
        }
        val textWidth = 200f // Approximate width
        font.draw(batch, loadingText, width / 2f - textWidth / 2f, height / 2f + 80f)
        batch.end()
    }

    override fun dispose() {
        shapeRenderer.dispose()
        font.dispose()
    }
}
