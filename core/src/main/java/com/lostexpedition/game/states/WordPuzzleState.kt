package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.entities.Key
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.tiles.Tile


class WordPuzzleState(refLink: RefLinks) : State(refLink) {

    private val hintText = "Călătorie neașteptată."
    private val targetWordDisplay = "LOST EXPEDITION"
    private val targetWordLogic = "LOSTEXPEDITION"

    private val currentInput = StringBuilder()
    private var solved = false

    private val puzzleStartTime = System.currentTimeMillis()
    private val timeLimitMs = 100000L
    private val damagePenalty = 20

    private var lastClickTime = 0L
    private val clickCooldown = 200L

    private data class Letter(
        val character: Char,
        val bounds: Rectangle,
        var isVisible: Boolean = true
    )

    private val letters = mutableListOf<Letter>()

    private val relativePositions = arrayOf(
        0.15f to 0.30f, 0.23f to 0.50f, 0.31f to 0.28f, 0.39f to 0.48f,
        0.47f to 0.26f, 0.55f to 0.51f, 0.63f to 0.31f, 0.71f to 0.52f,
        0.79f to 0.29f, 0.87f to 0.49f, 0.28f to 0.65f, 0.43f to 0.66f,
        0.58f to 0.64f, 0.73f to 0.65f
    )

    private val shapeRenderer = ShapeRenderer()
    private val titleFont = BitmapFont().apply {
        data.setScale(2f)
        color = Color.WHITE
    }
    private val letterFont = BitmapFont().apply {
        data.setScale(1.8f)
        color = Color.BLACK
    }
    private val inputFont = BitmapFont().apply {
        data.setScale(2.5f)
        color = Color.WHITE
    }
    private val timerFont = BitmapFont().apply {
        data.setScale(1.5f)
        color = Color.RED
    }

    init {
        val charsToPlace = targetWordLogic.toList().shuffled()
        charsToPlace.forEach { char ->
            letters.add(Letter(char, Rectangle(0f, 0f, 40f, 40f)))
        }

        println("WordPuzzleState initialized")
    }

    override fun update(delta: Float) {
        if (solved) return

        if (System.currentTimeMillis() - puzzleStartTime > timeLimitMs) {
            println("Time expired! Puzzle failed.")
            refLink.player?.takeDamage(damagePenalty)
            State.getPreviousState()?.let { refLink.setState(it) }
            return
        }

        if (Gdx.input.justTouched() &&
            System.currentTimeMillis() - lastClickTime > clickCooldown) {

            lastClickTime = System.currentTimeMillis()
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            for (letter in letters) {
                if (letter.isVisible && letter.bounds.contains(touchX, touchY)) {
                    currentInput.append(letter.character)
                    letter.isVisible = false

                    if (currentInput.toString() == "LOST") {
                        currentInput.append(" ")
                    }
                    break
                }
            }
        }

        if (!targetWordDisplay.startsWith(currentInput.toString())) {
            currentInput.clear()
            letters.forEach { it.isVisible = true }
        }

        if (currentInput.toString() == targetWordDisplay) {
            solved = true

            val prevState = State.getPreviousState()
            if (prevState is GameState) {
                val finalKey = Key(
                    refLink,
                    77 * Tile.TILE_WIDTH.toFloat(),
                    31 * Tile.TILE_HEIGHT.toFloat(),
                    Assets.keyImage,
                    6  // puzzleId
                )
                prevState.addEntity(finalKey)
            }

            refLink.setState(prevState!!)
        }
    }

    override fun render(batch: SpriteBatch) {
        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        State.getPreviousState()?.render(batch)

        if (batch.isDrawing) batch.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)
        shapeRenderer.rect(0f, 0f, screenWidth, screenHeight)
        shapeRenderer.end()

        batch.begin()

        val titleLayout = titleFont.draw(batch, hintText, 0f, 0f)
        val titleX = (screenWidth - titleLayout.width) / 2f
        titleFont.draw(batch, hintText, titleX, screenHeight - 80f)

        val timeLeftMs = timeLimitMs - (System.currentTimeMillis() - puzzleStartTime)
        val timeLeftSec = (timeLeftMs / 1000f).coerceAtLeast(0f)
        val timerStr = String.format("Timp: %.1f", timeLeftSec)
        val timerLayout = timerFont.draw(batch, timerStr, 0f, 0f)
        timerFont.draw(batch, timerStr, screenWidth - timerLayout.width - 20f, screenHeight - 40f)

        batch.end()

        val boxWidth = (screenWidth * 0.6f).toInt()
        val boxHeight = 60
        val boxX = ((screenWidth - boxWidth) / 2f).toInt()
        val boxY = (screenHeight * 0.8f).toInt()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(boxX.toFloat(), boxY.toFloat(), boxWidth.toFloat(), boxHeight.toFloat())
        shapeRenderer.end()

        batch.begin()
        val inputLayout = inputFont.draw(batch, currentInput.toString(), 0f, 0f)
        val inputX = boxX + (boxWidth - inputLayout.width) / 2f
        val inputY = boxY + (boxHeight + inputLayout.height) / 2f
        inputFont.draw(batch, currentInput.toString(), inputX, inputY)
        batch.end()

        letters.forEachIndexed { index, letter ->
            if (letter.isVisible && index < relativePositions.size) {
                val (relX, relY) = relativePositions[index]
                letter.bounds.x = screenWidth * relX
                letter.bounds.y = screenHeight * relY

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.color = Color.YELLOW
                shapeRenderer.rect(
                    letter.bounds.x,
                    letter.bounds.y,
                    letter.bounds.width,
                    letter.bounds.height
                )
                shapeRenderer.end()

                batch.begin()
                val charStr = letter.character.toString()
                val charLayout = letterFont.draw(batch, charStr, 0f, 0f)
                val charX = letter.bounds.x + (letter.bounds.width - charLayout.width) / 2f
                val charY = letter.bounds.y + (letter.bounds.height + charLayout.height) / 2f
                letterFont.draw(batch, charStr, charX, charY)
                batch.end()
            }
        }
    }

    override fun dispose() {
        shapeRenderer.dispose()
        titleFont.dispose()
        letterFont.dispose()
        inputFont.dispose()
        timerFont.dispose()
    }
}
