package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks
import kotlin.math.sin

class PauseState(refLink: RefLinks) : State(refLink) {

    private val menuOptions = arrayOf(
        "RESUME",
        "SAVE GAME",
        "MAIN MENU",
        "QUIT"
    )

    private var selectedOption = 0
    private val buttonBounds = mutableListOf<Rectangle>()

    private val shapeRenderer = ShapeRenderer()
    private val titleFont = BitmapFont().apply {
        data.setScale(3f)
        color = Color.YELLOW
    }
    private val buttonFont = BitmapFont().apply {
        data.setScale(1.3f)
        color = Color.WHITE
    }

    private val backgroundColor = Color(0f, 0f, 0f, 0.7f)
    private val selectedColor = Color(0.63f, 0.32f, 0.18f, 1f)
    private val unselectedColor = Color(1f, 1f, 1f, 0.7f)

    private var lastTouchTime = 0L
    private val touchCooldown = 200L

    init {
        println("PauseState initialized")
        calculateButtonBounds()
    }

    override fun update(delta: Float) {
        handleInput()
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        // Render background state
        refLink.getPersistedGameState()?.render(batch)

        // Overlay
        batch.end()
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = backgroundColor
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()
        batch.begin()

        // Title
        batch.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        val title = "PAUZĂ"
        val titleLayout = titleFont.draw(batch, title, 0f, 0f)
        val titleX = (width - titleLayout.width) / 2f
        titleFont.draw(batch, title, titleX, height - 100f)

        batch.end()

        // Buttons
        val buttonWidth = 400f
        val buttonHeight = 60f
        val startY = height / 2f + 100f
        val gap = 70f

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

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.YELLOW
            shapeRenderer.rect(x, y, buttonWidth, buttonHeight)
            shapeRenderer.end()

            batch.begin()
            buttonFont.color = if (isSelected) Color.WHITE else Color.YELLOW
            val textLayout = buttonFont.draw(batch, menuOptions[i], 0f, 0f)
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.P) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            resumeGame()
        }
    }

    private fun executeSelectedOption() {
        when (selectedOption) {
            0 -> resumeGame()
            1 -> saveGame()
            2 -> returnToMenu()
            3 -> quitGame()
        }
    }

    private fun resumeGame() {
        println("Resuming game...")
        refLink.getPersistedGameState()?.let { gameState ->
            refLink.setState(gameState)
        }
    }

    private fun saveGame() {
        println("Saving game...")
        refLink.getPersistedGameState()?.let { gameState ->
            gameState.saveCurrentState()
        }
    }

    private fun returnToMenu() {
        println("Returning to main menu...")
        refLink.setState(MenuState(refLink))
    }

    private fun quitGame() {
        println("Quitting game...")
        Gdx.app.exit()
    }

    private fun calculateButtonBounds() {
        buttonBounds.clear()
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        val buttonWidth = 400f
        val buttonHeight = 60f
        val startY = height / 2f + 100f
        val gap = 70f

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
