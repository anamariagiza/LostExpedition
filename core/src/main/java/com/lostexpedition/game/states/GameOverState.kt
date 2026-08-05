package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.graphics.UiFont
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.utils.SoundManager
import kotlin.math.sin

class GameOverState(refLink: RefLinks) : State(refLink) {

    private val menuOptions = arrayOf(
        "TRY AGAIN",
        "RETURN TO MAIN MENU",
        "QUIT"
    )

    private var selectedOption = 0
    private val buttonBounds = mutableListOf<Rectangle>()

    // Nivelul pe care il reluam la "Try Again" = nivelul pe care a murit jucatorul
    // (inainte era mereu nivelul 3, chiar daca mureai pe nivelul 1).
    private val retryLevel: Int = refLink.gameState?.getCurrentLevel() ?: 0

    private val shapeRenderer = ShapeRenderer()
    private val s: Float = UiFont.scale()
    private val titleFont = UiFont.make((48 * s).toInt(), 3f * s, Color.RED)
    private val buttonFont = UiFont.make((26 * s).toInt(), 2f * s, Color.WHITE)

    private val backgroundColor = Color(0f, 0f, 0f, 0.85f)
    private val selectedColor = Color(0.63f, 0.32f, 0.18f, 1f)
    private val unselectedColor = Color(1f, 1f, 1f, 0.7f)

    private var lastTouchTime = 0L
    private val touchCooldown = 200L

    init {
        println("GameOverState initialized")
        SoundManager.stopMusic()
        SoundManager.playSfx(SoundManager.SFX_GAMEOVER)
        calculateButtonBounds()
    }

    override fun update(delta: Float) {
        handleInput()
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        // Fundal
        batch.begin()
        Assets.backgroundMenu?.let {
            batch.draw(it, 0f, 0f, width, height)
        }
        batch.end()

        // Overlay închis
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = backgroundColor
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()

        // Titlu GAME OVER
        batch.begin()
        val title = "GAME OVER"
        val titleLayout = GlyphLayout(titleFont, title)
        val titleX = (width - titleLayout.width) / 2f
        titleFont.draw(batch, title, titleX, height / 2f + 210f * s)
        batch.end()

        // Butoane
        val buttonWidth = 480f * s
        val buttonHeight = 70f * s
        val startY = height / 2f
        val gap = 90f * s

        buttonBounds.clear()

        for (i in menuOptions.indices) {
            val x = (width - buttonWidth) / 2f
            val y = startY - i * gap

            buttonBounds.add(Rectangle(x, y, buttonWidth, buttonHeight))

            val isSelected = (i == selectedOption)

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            if (isSelected) {
                val pulse = (sin(System.currentTimeMillis() * 0.007) * 7).toFloat()
                shapeRenderer.color = selectedColor
                shapeRenderer.rect(
                    x - pulse,
                    y - pulse,
                    buttonWidth + 2 * pulse,
                    buttonHeight + 2 * pulse
                )
            } else {
                shapeRenderer.color = unselectedColor
                shapeRenderer.rect(x, y, buttonWidth, buttonHeight)
            }
            shapeRenderer.end()

            // Contur
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.RED
            shapeRenderer.rect(x, y, buttonWidth, buttonHeight)
            shapeRenderer.end()

            // Text
            batch.begin()
            buttonFont.color = if (isSelected) Color.WHITE else Color.RED
            val textLayout = GlyphLayout(buttonFont, menuOptions[i])
            val textX = x + (buttonWidth - textLayout.width) / 2f
            val textY = y + (buttonHeight + textLayout.height) / 2f
            buttonFont.draw(batch, menuOptions[i], textX, textY)
            batch.end()
        }
    }

    private fun handleInput() {
        // Touch
        if (Gdx.input.justTouched() &&
            System.currentTimeMillis() - lastTouchTime > touchCooldown) {

            lastTouchTime = System.currentTimeMillis()
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            buttonBounds.forEachIndexed { index, rect ->
                if (rect.contains(touchX, touchY)) {
                    selectedOption = index
                    executeSelectedOption()
                }
            }
        }

        // Keyboard
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption--
            if (selectedOption < 0) selectedOption = menuOptions.size - 1
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption++
            if (selectedOption >= menuOptions.size) selectedOption = 0
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            executeSelectedOption()
        }
    }

    private fun executeSelectedOption() {
        when (selectedOption) {
            0 -> { // TRY AGAIN
                println("Restarting level ${retryLevel + 1}...")
                refLink.setState(GameState(refLink, startLevel = retryLevel))
            }
            1 -> { // RETURN TO MAIN MENU
                println("Returning to main menu...")
                refLink.setState(MenuState(refLink))
            }
            2 -> { // QUIT
                println("Quitting game...")
                Gdx.app.exit()
            }
        }
    }

    private fun calculateButtonBounds() {
        buttonBounds.clear()
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        val buttonWidth = 480f * s
        val buttonHeight = 70f * s
        val startY = height / 2f
        val gap = 90f * s

        for (i in menuOptions.indices) {
            val x = (width - buttonWidth) / 2f
            val y = startY - i * gap
            buttonBounds.add(Rectangle(x, y, buttonWidth, buttonHeight))
        }
    }

    override fun dispose() {
        shapeRenderer.dispose()
        titleFont.dispose()
        buttonFont.dispose()
    }
}
