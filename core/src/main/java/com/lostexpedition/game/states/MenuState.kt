package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks
import kotlin.math.sin

class MenuState(refLink: RefLinks) : State(refLink) {

    private val menuOptions = arrayOf(
        "NEW GAME",
        "LOAD GAME",
        "SETTINGS",
        "ABOUT",  // Am adăugat și meniul About
        "EXIT"
    )

    private var selectedOption = 0
    private val buttonBounds = mutableListOf<Rectangle>()

    private val titleFont = BitmapFont().apply {
        data.setScale(3f)
        color = Color.YELLOW
    }

    private val buttonFont = BitmapFont().apply {
        data.setScale(2f)
    }

    private val shapeRenderer = ShapeRenderer()
    private val selectedColor = Color(0.2f, 0.2f, 0.8f, 0.9f)
    private val unselectedColor = Color(0.1f, 0.1f, 0.3f, 0.7f)

    init {
        println("MenuState initialized")
    }

    override fun update(delta: Float) {
        handleInput()
    }

    private fun handleInput() {
        // Navigare tastatură
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption = (selectedOption - 1 + menuOptions.size) % menuOptions.size
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % menuOptions.size
        }

        // Selecție Touch/Mouse
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            buttonBounds.forEachIndexed { index, bounds ->
                if (bounds.contains(touchX, touchY)) {
                    selectedOption = index
                    handleSelection()
                }
            }
        }

        // Tasta Enter
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            handleSelection()
        }
    }

    private fun handleSelection() {
        when (selectedOption) {
            0 -> startNewGame()
            1 -> loadGame()
            2 -> openSettings()
            3 -> openAbout()
            4 -> exitGame()
        }
    }

    private fun startNewGame() {
        // Pornim jocul de la zero (isLoadingFromSave = false)
        refLink.setState(GameState(refLink, 1, false))
    }

    private fun loadGame() {
        // Verificăm dacă există o salvare înainte să încercăm încărcarea
        val savedData = refLink.databaseManager.loadGameData()
        if (savedData.isNotEmpty()) {
            println("Loading game...")
            // Pornim jocul cu flag-ul de încărcare (isLoadingFromSave = true)
            refLink.setState(GameState(refLink, 0, true))
        } else {
            println("No saved game found!")
            // Aici ai putea adăuga un feedback vizual (ex: sunet de eroare sau text roșu)
        }
    }

    private fun openSettings() {
        refLink.setState(SettingsState(refLink))
    }

    private fun openAbout() {
        refLink.setState(AboutState(refLink))
    }

    private fun exitGame() {
        Gdx.app.exit()
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        // Curățare ecran
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // ===== DESENARE FUNDAL =====
        batch.begin()
        Assets.backgroundMenu?.let {
            batch.draw(it, 0f, 0f, width, height)
        }

        // Titlu
        val title = "LOST EXPEDITION"
        val titleLayout = GlyphLayout(titleFont, title)
        val titleX = (width - titleLayout.width) / 2f
        titleFont.draw(batch, title, titleX, height - 100f)
        batch.end()

        // ===== DESENARE BUTOANE (Forme) =====
        val buttonWidth = 400f
        val buttonHeight = 60f
        val startY = height / 2f + 50f
        val gap = 80f

        buttonBounds.clear()

        Gdx.gl.glEnable(GL20.GL_BLEND)
        shapeRenderer.projectionMatrix = batch.projectionMatrix

        for (i in menuOptions.indices) {
            val x = (width - buttonWidth) / 2f
            val y = startY - i * gap

            buttonBounds.add(Rectangle(x, y, buttonWidth, buttonHeight))

            val isSelected = (i == selectedOption)

            // Fundal buton
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            if (isSelected) {
                val pulse = (sin(System.currentTimeMillis() * 0.007) * 5).toFloat()
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

            // Contur buton
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color(1f, 0.84f, 0f, 1f)
            shapeRenderer.rect(x, y, buttonWidth, buttonHeight)
            shapeRenderer.end()
        }

        // ===== DESENARE TEXT BUTOANE =====
        batch.begin()
        for (i in menuOptions.indices) {
            val x = (width - buttonWidth) / 2f
            val y = startY - i * gap
            val isSelected = (i == selectedOption)

            buttonFont.color = if (isSelected) Color.WHITE else Color(0.69f, 0.57f, 0f, 1f)
            val textLayout = GlyphLayout(buttonFont, menuOptions[i])
            val textX = x + (buttonWidth - textLayout.width) / 2f
            val textY = y + (buttonHeight + textLayout.height) / 2f
            buttonFont.draw(batch, menuOptions[i], textX, textY)
        }
        batch.end()
    }

    override fun dispose() {
        titleFont.dispose()
        buttonFont.dispose()
        shapeRenderer.dispose()
    }
}
