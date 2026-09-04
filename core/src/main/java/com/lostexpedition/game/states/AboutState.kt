package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
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
    private val buttonFont = BitmapFont().apply {
        data.setScale(1.5f)
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
        "Versiune: 1.0"
    )

    private val backBtnBounds = Rectangle()

    init {
        println("AboutState initialized")
        calculateLayout()
    }

    private fun calculateLayout() {
        val w = Gdx.graphics.width.toFloat()
        backBtnBounds.set(w / 2f - 150f, 60f, 300f, 80f)
    }

    override fun update(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            refLink.setState(MenuState(refLink))
        }

        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            if (backBtnBounds.contains(touchX, touchY)) {
                refLink.setState(MenuState(refLink))
            }
        }
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        batch.begin()
        Assets.backgroundMenu?.let {
            batch.draw(it, 0f, 0f, width, height)
        }
        batch.end()

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
        shapeRenderer.rect(0f, 0f, width, height)

        shapeRenderer.color = Color(0.2f, 0.2f, 0.8f, 1f)
        shapeRenderer.rect(backBtnBounds.x, backBtnBounds.y, backBtnBounds.width, backBtnBounds.height)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(backBtnBounds.x, backBtnBounds.y, backBtnBounds.width, backBtnBounds.height)
        shapeRenderer.end()

        batch.begin()

        var yPos = height - 100f
        for (line in aboutText) {
            val font = if (line == "LOST EXPEDITION") titleFont else textFont
            val layout = font.draw(batch, line, 0f, 0f)
            val xPos = (width - layout.width) / 2f
            font.draw(batch, line, xPos, yPos)
            yPos -= if (line.isEmpty()) 20f else 35f
        }

        val backLayout = GlyphLayout(buttonFont, "ÎNAPOI")
        buttonFont.draw(
            batch, "ÎNAPOI",
            backBtnBounds.x + (backBtnBounds.width - backLayout.width) / 2f,
            backBtnBounds.y + (backBtnBounds.height + backLayout.height) / 2f
        )

        batch.end()
    }

    override fun dispose() {
        shapeRenderer.dispose()
        titleFont.dispose()
        textFont.dispose()
        buttonFont.dispose()
    }
}
