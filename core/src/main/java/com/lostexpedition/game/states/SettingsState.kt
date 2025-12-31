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

class SettingsState(refLink: RefLinks) : State(refLink) {

    private val menuOptions = arrayOf(
        "SOUND: ON",
        "MUSIC: ON",
        "VOLUME: 100%",
        "BACK"
    )

    private var selectedOption = 0
    private val buttonBounds = mutableListOf<Rectangle>()

    private var soundEnabled = true
    private var musicEnabled = true
    private var volume = 100

    private val shapeRenderer = ShapeRenderer()
    private val titleFont = BitmapFont().apply {
        data.setScale(2f)
        color = Color(1f, 0.84f, 0f, 1f)
    }
    private val buttonFont = BitmapFont().apply {
        data.setScale(1.3f)
        color = Color.WHITE
    }

    private val selectedColor = Color(0.63f, 0.32f, 0.18f, 1f)
    private val unselectedColor = Color(1f, 1f, 1f, 0.7f)

    private var lastTouchTime = 0L
    private val touchCooldown = 200L

    init {
        println("SettingsState initialized")
        loadSettings()
        calculateButtonBounds()
    }

    private fun loadSettings() {
        // Acum returnează direct obiectul SettingsData (nu o listă)
        val settings = refLink.databaseManager.loadSettingsData()
        soundEnabled = settings.soundEnabled
        musicEnabled = settings.musicEnabled
        volume = settings.volume
        updateMenuOptions()
    }

    private fun updateMenuOptions() {
        menuOptions[0] = "SOUND: ${if (soundEnabled) "ON" else "OFF"}"
        menuOptions[1] = "MUSIC: ${if (musicEnabled) "ON" else "OFF"}"
        menuOptions[2] = "VOLUME: $volume%"
    }

    override fun update(delta: Float) {
        handleInput()
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
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()

        // Title
        batch.begin()
        val title = "SETĂRI"
        val titleLayout = titleFont.draw(batch, title, 0f, 0f)
        val titleX = (width - titleLayout.width) / 2f
        titleFont.draw(batch, title, titleX, height - 100f)
        batch.end()

        // Menu buttons
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
            shapeRenderer.color = Color(1f, 0.84f, 0f, 1f)
            shapeRenderer.rect(x, y, buttonWidth, buttonHeight)
            shapeRenderer.end()

            batch.begin()
            buttonFont.color = if (isSelected) Color.WHITE else Color(0.69f, 0.57f, 0f, 1f)
            val textLayout = buttonFont.draw(batch, menuOptions[i], 0f, 0f)
            val textX = x + (buttonWidth - textLayout.width) / 2f
            val textY = y + (buttonHeight + textLayout.height) / 2f
            buttonFont.draw(batch, menuOptions[i], textX, textY)
            batch.end()
        }
    }

    private fun handleInput() {
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            saveAndReturn()
        }

        if (selectedOption == 2) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                volume = (volume - 10).coerceAtLeast(0)
                updateMenuOptions()
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                volume = (volume + 10).coerceAtMost(100)
                updateMenuOptions()
            }
        }
    }

    private fun executeSelectedOption() {
        when (selectedOption) {
            0 -> {
                soundEnabled = !soundEnabled
                updateMenuOptions()
            }
            1 -> {
                musicEnabled = !musicEnabled
                updateMenuOptions()
            }
            2 -> {
                // Volumul se schimbă din taste stânga/dreapta sau poți adăuga logică touch
                volume = (volume + 10)
                if (volume > 100) volume = 0
                updateMenuOptions()
            }
            3 -> {
                saveAndReturn()
            }
        }
    }

    private fun saveAndReturn() {
        // Acum apelăm metoda corectă cu tipurile corecte (Boolean, Boolean, Int)
        refLink.databaseManager.saveSettingsData(soundEnabled, musicEnabled, volume)
        println("Settings saved")
        refLink.setState(MenuState(refLink))
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
