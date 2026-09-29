package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.lostexpedition.game.graphics.UiFont
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.utils.SoundManager

class AboutState(refLink: RefLinks) : State(refLink) {

    private val s = UiFont.scale()
    private val shapeRenderer = ShapeRenderer()
    private val titleFont: BitmapFont = UiFont.make((52 * s).toInt(), 3f * s, Color(1f, 0.84f, 0f, 1f))
    private val textFont: BitmapFont = UiFont.make((24 * s).toInt(), 1.5f * s)
    private val buttonFont: BitmapFont = UiFont.make((28 * s).toInt(), 2f * s)

    private val paragraphs = listOf(
        "A 2D adventure through a hidden jungle, a labyrinth of ancient puzzles " +
            "and a final battle deep inside a cave.",
        "Created by Ana-Maria Gîza.",
        "Born from a school project and a passion for creating games.",
        "Thank you for playing! Good luck on your expedition, and have fun!"
    )

    private val backBtnBounds = Rectangle()

    init {
        val w = Gdx.graphics.width.toFloat()
        backBtnBounds.set(w / 2f - 150f * s, 40f * s, 300f * s, 70f * s)
    }

    override fun update(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            refLink.setState(MenuState(refLink))
            return
        }

        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            if (backBtnBounds.contains(touchX, touchY)) {
                SoundManager.click()
                refLink.setState(MenuState(refLink))
            }
        }
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        batch.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        MenuBackground.draw(batch, shapeRenderer, 0.6f)

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.2f, 0.2f, 0.8f, 1f)
        shapeRenderer.rect(backBtnBounds.x, backBtnBounds.y, backBtnBounds.width, backBtnBounds.height)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(1f, 0.84f, 0f, 1f)
        shapeRenderer.rect(backBtnBounds.x, backBtnBounds.y, backBtnBounds.width, backBtnBounds.height)
        shapeRenderer.end()

        batch.begin()

        var y = height - 60f * s
        val titleLayout = GlyphLayout(titleFont, "LOST EXPEDITION")
        titleFont.draw(batch, "LOST EXPEDITION", (width - titleLayout.width) / 2f, y)
        y -= titleLayout.height + 50f * s

        // Paragrafe centrate, cu word-wrap pe ~65% din lățimea ecranului
        val textWidth = width * 0.65f
        val textX = (width - textWidth) / 2f
        for (paragraph in paragraphs) {
            val layout = GlyphLayout(textFont, paragraph, Color.WHITE, textWidth, Align.center, true)
            textFont.draw(batch, layout, textX, y)
            y -= layout.height + 28f * s
        }

        textFont.color = Color.LIGHT_GRAY
        val version = "Version 1.0"
        val versionLayout = GlyphLayout(textFont, version)
        textFont.draw(batch, version, (width - versionLayout.width) / 2f, backBtnBounds.y + backBtnBounds.height + 40f * s)
        textFont.color = Color.WHITE

        val backLayout = GlyphLayout(buttonFont, "BACK")
        buttonFont.draw(
            batch, "BACK",
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
