package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks

class AboutState(refLink: RefLinks) : State(refLink) {

    private val shapeRenderer = ShapeRenderer()
    private val titleFont = BitmapFont().apply {
        data.setScale(2f)
        color = Color(1f, 0.84f, 0f, 1f)
    }
    private val textFont = BitmapFont().apply {
        data.setScale(1.2f)
        color = Color.WHITE
    }

    private val aboutText = arrayOf(
        "LOST EXPEDITION",
        "",
        "Un joc de aventură 2D creat cu LibGDX",
        "",
        "Caracteristici:",
        "• 3 niveluri captivante",
        "• Puzzle-uri complexe",
        "• Combate cu inamici",
        "• Sistem de salvare/încărcare",
        "",
        "Creat de: Ana",
        "Versiune: 1.0",
        "",
        "Apasă ESC pentru a reveni"
    )

    init {
        println("AboutState initialized")
    }

    override fun update(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            refLink.setState(MenuState(refLink))
        }
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        // Background
        batch.begin()
        Assets.backgroundMenu?.let {
            batch.draw(it, 0f, 0f, width, height)
        }
        batch.end()

        // Overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()

        // Content
        batch.begin()

        var yPos = height - 100f
        for (line in aboutText) {
            val font = if (line == "LOST EXPEDITION") titleFont else textFont
            val layout = font.draw(batch, line, 0f, 0f)
            val xPos = (width - layout.width) / 2f
            font.draw(batch, line, xPos, yPos)
            yPos -= if (line.isEmpty()) 20f else 35f
        }

        batch.end()
    }

    override fun dispose() {
        shapeRenderer.dispose()
        titleFont.dispose()
        textFont.dispose()
    }
}
