package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.entities.Key
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.graphics.UiFont
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.utils.SoundManager

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

    /** Factor de scalare UI: 1.0 la 720p, ~1.5 pe un telefon 1080p. */
    private val s: Float = UiFont.scale()

    /** Dimensiunea casetelor cu litere (mai mari pe mobil, ca sa fie usor de atins). */
    private val letterSize = 64f * s

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
    private val uiMatrix = Matrix4()

    private val titleFont: BitmapFont = UiFont.make((34 * s).toInt(), 3f * s, Color.WHITE)
    private val letterFont: BitmapFont = UiFont.make((30 * s).toInt(), 0f, Color.BLACK)
    private val inputFont: BitmapFont = UiFont.make((38 * s).toInt(), 2f * s, Color.WHITE)
    private val timerFont: BitmapFont = UiFont.make((24 * s).toInt(), 2f * s, Color.RED)

    init {
        val charsToPlace = targetWordLogic.toList().shuffled()
        charsToPlace.forEach { char ->
            letters.add(Letter(char, Rectangle(0f, 0f, letterSize, letterSize)))
        }
    }

    override fun update(delta: Float) {
        if (solved) return

        if (System.currentTimeMillis() - puzzleStartTime > timeLimitMs) {
            SoundManager.playSfx(SoundManager.SFX_PUZZLE_FAIL)
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
            SoundManager.playSfx(SoundManager.SFX_PUZZLE_SUCCESS)

            val prevState = State.getPreviousState()
            if (prevState is GameState) {
                // Cheia finala (id 6) apare la (77, 31) in coordonate de grila.
                // Atentie: harta Kotlin foloseste y-in-sus, deci convertim ca in topDownY().
                val ts = TileConstants.TILE_SIZE
                val keyY = (prevState.getMap().height - 1 - 31) * ts
                val finalKey = Key(refLink, 77f * ts, keyY, Assets.keyImage, 6)
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

        uiMatrix.setToOrtho2D(0f, 0f, screenWidth, screenHeight)
        shapeRenderer.projectionMatrix = uiMatrix
        batch.projectionMatrix = uiMatrix

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)
        shapeRenderer.rect(0f, 0f, screenWidth, screenHeight)
        shapeRenderer.end()

        batch.begin()

        // Indiciul (titlul)
        val titleLayout = GlyphLayout(titleFont, hintText)
        titleFont.draw(batch, hintText, (screenWidth - titleLayout.width) / 2f, screenHeight - 40f * s)

        // Timerul
        val timeLeftMs = timeLimitMs - (System.currentTimeMillis() - puzzleStartTime)
        val timeLeftSec = (timeLeftMs / 1000f).coerceAtLeast(0f)
        val timerStr = "Timp: %.1f".format(timeLeftSec)
        val timerLayout = GlyphLayout(timerFont, timerStr)
        timerFont.draw(batch, timerStr, screenWidth - timerLayout.width - 24f * s, screenHeight - 30f * s)

        batch.end()

        // Caseta de input
        val boxWidth = screenWidth * 0.5f
        val boxHeight = 76f * s
        val boxX = (screenWidth - boxWidth) / 2f
        val boxY = screenHeight * 0.76f

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.6f)
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight)
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight)
        shapeRenderer.end()

        batch.begin()
        val inputStr = currentInput.toString()
        if (inputStr.isNotEmpty()) {
            val inputLayout = GlyphLayout(inputFont, inputStr)
            inputFont.draw(
                batch, inputStr,
                boxX + (boxWidth - inputLayout.width) / 2f,
                boxY + (boxHeight + inputLayout.height) / 2f
            )
        }
        batch.end()

        // Literele imprastiate pe ecran
        letters.forEachIndexed { index, letter ->
            if (letter.isVisible && index < relativePositions.size) {
                val (relX, relY) = relativePositions[index]
                letter.bounds.x = screenWidth * relX
                letter.bounds.y = screenHeight * relY
                letter.bounds.width = letterSize
                letter.bounds.height = letterSize

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.color = Color.YELLOW
                shapeRenderer.rect(letter.bounds.x, letter.bounds.y, letter.bounds.width, letter.bounds.height)
                shapeRenderer.end()
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
                shapeRenderer.color = Color(0.5f, 0.4f, 0f, 1f)
                shapeRenderer.rect(letter.bounds.x, letter.bounds.y, letter.bounds.width, letter.bounds.height)
                shapeRenderer.end()

                batch.begin()
                val charStr = letter.character.toString()
                val charLayout = GlyphLayout(letterFont, charStr)
                letterFont.draw(
                    batch, charStr,
                    letter.bounds.x + (letter.bounds.width - charLayout.width) / 2f,
                    letter.bounds.y + (letter.bounds.height + charLayout.height) / 2f
                )
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
