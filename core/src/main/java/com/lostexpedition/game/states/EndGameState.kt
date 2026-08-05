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

class EndGameState(refLink: RefLinks) : State(refLink) {

    private val menuOptions = arrayOf(
        "RETURN TO MAIN MENU",
        "QUIT"
    )

    private var selectedOption = 0
    private val buttonBounds = mutableListOf<Rectangle>()

    private val shapeRenderer = ShapeRenderer()
    private val s: Float = UiFont.scale()
    private val titleFont = UiFont.make((42 * s).toInt(), 3f * s, Color(1f, 0.84f, 0f, 1f))
    private val buttonFont = UiFont.make((26 * s).toInt(), 2f * s, Color.WHITE)

    private val backgroundColor = Color(0f, 0f, 0f, 0.8f)
    private val selectedColor = Color(0.63f, 0.32f, 0.18f, 1f)
    private val unselectedColor = Color(1f, 1f, 1f, 0.7f)

    private var lastTouchTime = 0L
    private val touchCooldown = 200L

    init {
        println("✓ EndGameState initialized - VICTORY!")
        SoundManager.stopMusic()
        SoundManager.playSfx(SoundManager.SFX_VICTORY)
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

        // Overlay
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = backgroundColor
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()

        // Titlu VICTORY
        batch.begin()
        val title = "FELICITĂRI!"
        val subtitle = "Ai terminat Lost Expedition!"
        val titleLayout = GlyphLayout(titleFont, title)
        val titleX = (width - titleLayout.width) / 2f
        titleFont.draw(batch, title, titleX, height / 2f + 240f * s)

        val subtitleLayout = GlyphLayout(titleFont, subtitle)
        val subtitleX = (width - subtitleLayout.width) / 2f
        titleFont.draw(batch, subtitle, subtitleX, height / 2f + 165f * s)
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
            shapeRenderer.color = Color(1f, 0.84f, 0f, 1f)
            shapeRenderer.rect(x, y, buttonWidth, buttonHeight)
            shapeRenderer.end()

            // Text
            batch.begin()
            buttonFont.color = if (isSelected) Color.WHITE else Color(0.69f, 0.57f, 0f, 1f)
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
            0 -> {
                println("Returning to main menu...")
                refLink.setState(MenuState(refLink))
            }
            1 -> {
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
