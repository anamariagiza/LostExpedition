package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.utils.RefLinks

class PauseState(refLink: RefLinks) : State(refLink) {

    private val font = BitmapFont().apply {
        data.setScale(3f)
    }

    private val buttons = arrayOf("RESUME", "EXIT TO MENU")
    private val buttonBounds = mutableListOf<Rectangle>()
    private val shapeRenderer = ShapeRenderer()

    init {
        // Calculăm pozițiile butoanelor o singură dată
        val buttonWidth = 400f
        val buttonHeight = 100f
        val centerX = (Gdx.graphics.width - buttonWidth) / 2
        val startY = Gdx.graphics.height / 2f

        // Butonul RESUME
        buttonBounds.add(Rectangle(centerX, startY + 20f, buttonWidth, buttonHeight))
        // Butonul EXIT
        buttonBounds.add(Rectangle(centerX, startY - 120f, buttonWidth, buttonHeight))
    }

    override fun update(delta: Float) {
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat() // Inversăm Y pentru coordonatele lumii

            // Verificăm RESUME
            if (buttonBounds[0].contains(touchX, touchY)) {
                resumeGame()
            }
            // Verificăm EXIT
            else if (buttonBounds[1].contains(touchX, touchY)) {
                refLink.setState(MenuState(refLink))
            }
        }
    }

    private fun resumeGame() {
        // Reîncărcăm jocul din salvarea pe care am făcut-o automat când am apăsat butonul de pauză
        // Parametrii: level 0 (ignorat la load), isLoadingFromSave = true
        refLink.setState(GameState(refLink, 0, true))
    }

    override fun render(batch: SpriteBatch) {
        // 1. Curățăm ecranul cu o culoare închisă
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // 2. Setăm matricea de proiecție pentru UI
        batch.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.projectionMatrix = batch.projectionMatrix

        // 3. Desenăm butoanele (ShapeRenderer)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Buton RESUME (Verde)
        shapeRenderer.color = Color(0f, 0.5f, 0f, 1f)
        shapeRenderer.rect(buttonBounds[0].x, buttonBounds[0].y, buttonBounds[0].width, buttonBounds[0].height)

        // Buton EXIT (Roșu)
        shapeRenderer.color = Color(0.5f, 0f, 0f, 1f)
        shapeRenderer.rect(buttonBounds[1].x, buttonBounds[1].y, buttonBounds[1].width, buttonBounds[1].height)

        shapeRenderer.end()

        // 4. Desenăm textul (SpriteBatch)
        // AICI ERA EROAREA: Trebuie să apelăm begin() înainte de draw()
        batch.begin()

        // Titlu PAUSE
        font.color = Color.WHITE
        val titleLayout = GlyphLayout(font, "GAME PAUSED")
        font.draw(batch, "GAME PAUSED", (Gdx.graphics.width - titleLayout.width) / 2, Gdx.graphics.height - 100f)

        // Text RESUME
        val resumeText = "RESUME"
        val resumeLayout = GlyphLayout(font, resumeText)
        font.draw(batch, resumeText,
            buttonBounds[0].x + (buttonBounds[0].width - resumeLayout.width) / 2,
            buttonBounds[0].y + (buttonBounds[0].height + resumeLayout.height) / 2
        )

        // Text EXIT
        val exitText = "EXIT"
        val exitLayout = GlyphLayout(font, exitText)
        font.draw(batch, exitText,
            buttonBounds[1].x + (buttonBounds[1].width - exitLayout.width) / 2,
            buttonBounds[1].y + (buttonBounds[1].height + exitLayout.height) / 2
        )

        batch.end()
    }

    override fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
    }
}
