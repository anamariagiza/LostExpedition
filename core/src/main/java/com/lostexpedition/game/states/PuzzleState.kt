package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.utils.SoundManager
import kotlin.random.Random

class PuzzleState(
    refLink: RefLinks,
    private val puzzleId: Int
) : State(refLink) {

    companion object {
        private const val TIME_LIMIT_MS = 60000L
        private const val MESSAGE_DURATION_MS = 2000L
        private const val MAX_WRONG_ATTEMPTS = 3
        private const val SIMON_START_LENGTH = 3
        private const val SIMON_FINAL_LENGTH = 6      // 4 runde: 3, 4, 5, 6 simboluri
        private const val SIMON_ON = 0.55f            // cât stă aprins un simbol la afișare (s)
        private const val SIMON_OFF = 0.2f            // pauza dintre simboluri (s)
        private const val SIMON_START_DELAY = 0.8f
        private const val CARD_REVEAL_DURATION_MS = 1000L
    }

    /** Factor de scalare UI: 1.0 la 720p, ~1.5 pe un telefon 1080p. */
    private val s: Float = Gdx.graphics.height / 720f

    // Ceasul puzzle-ului (ms), avansat doar din update(delta): timpul nu curge cât timp
    // aplicația e în fundal (ex. un apel primit în timpul puzzle-ului).
    private var clockMs = 0L

    private var puzzleStartTime = 0L
    private var puzzleActive = false
    private var puzzleSolved = false
    private var puzzleFailed = false
    private var currentPuzzleTitle = ""
    private var currentObjective = ""
    private var resultSoundPlayed = false

    // Butoane rezultat
    private val nextPuzzleButtonBounds = Rectangle()
    private val retryButtonBounds = Rectangle()

    // Puzzle 1
    private val optionBounds1 = mutableListOf<Rectangle>()
    private var grid1 = Array(3) { Array(3) { "" } }
    private var playerChoice1 = ""
    private val symbols = arrayOf("SUN", "MOON", "STAR", "BOLT")

    // Puzzle 2
    private var correctOrder2 = listOf<String>()
    private val playerOrder2 = mutableListOf<String>()
    private var clue2 = ""
    private val gems = arrayOf("SAPPHIRE", "EMERALD", "RUBY", "DIAMOND")
    private var wrongAttempts2 = 0
    private val gemBounds2 = mutableListOf<Rectangle>()
    private val dropZoneBounds2 = mutableListOf<Rectangle>()
    private var selectedGemIndex2 = -1
    private val gemRegions: Array<TextureRegion?> = Array(4) { i ->
        Assets.puzzle2Gems?.let { gemsRegion -> extractGemRegion(gemsRegion, i) }
    }

    // Puzzle 3
    private var riddle3 = ""
    private var answers3 = listOf<String>()
    private var correctAnswerIndex3 = 0
    private var selectedAnswerIndex3 = -1
    private val answerBounds3 = mutableListOf<Rectangle>()
    private val riddles = arrayOf(
        "I have cities but no houses. I have forests but no trees. What am I?",
        "You can hold me without touching me. Break me with a word. What am I?"
    )
    private val riddleAnswers = arrayOf(
        arrayOf("A map", "An ocean", "A desert"),
        arrayOf("A bottle", "A promise", "A balloon")
    )
    private val correctRiddleAnswers = intArrayOf(0, 1)

    // Puzzle 4: secvența de simboluri (stil "Simon"). Se aprind simbolurile într-o ordine,
    // jucătorul o repetă; fiecare rundă adaugă un simbol (de la 3 la 6).
    private val simonSequence = mutableListOf<Int>()
    private var simonShowing = false        // true = jocul arată secvența, atingerile sunt ignorate
    private var simonShowTimer = 0f         // < 0 = scurtă pauză înainte de afișare
    private var simonLastShownStep = -1
    private var simonInputIndex = 0
    private var simonLitPad = -1            // simbolul aprins acum (-1 = niciunul)
    private var simonLitTimer = 0f
    private var simonWrong = 0
    private var simonStatus = ""
    private var simonStatusTime = 0L
    private val simonPadBounds = List(4) { Rectangle() }

    // Puzzle 5
    private val cardLayout5 = mutableListOf<Int>()
    private val revealedCards5 = BooleanArray(16)
    private var firstCardIndex5 = -1
    private var secondCardIndex5 = -1
    private var pairsFound5 = 0
    private var cardRevealTime5 = 0L
    private val cardBounds5 = List(16) { Rectangle() }

    // Fonturi scalate dupa rezolutia ecranului
    private val titleFont: BitmapFont = makeFont((36 * s).toInt(), 3f * s)
    private val textFont: BitmapFont = makeFont((26 * s).toInt(), 2f * s)
    private val bigFont: BitmapFont = makeFont((46 * s).toInt(), 3f * s)
    // Font fără contur pentru textul scris cu cerneală pe pergament (ghicitoarea): cu
    // conturul negru al textFont, literele închise la culoare se lipeau și nu se citeau.
    private val inkFontDelegate = lazy { makeFont((28 * s).toInt(), 0f) }
    private val inkFont: BitmapFont by inkFontDelegate

    private val shapeRenderer = ShapeRenderer()
    private val uiMatrix = Matrix4()

    init {
        generatePuzzle()
    }

    private fun makeFont(size: Int, borderWidth: Float): BitmapFont {
        return try {
            val generator = FreeTypeFontGenerator(Gdx.files.internal("font.ttf"))
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
            parameter.size = size
            parameter.borderWidth = borderWidth
            parameter.borderColor = Color.BLACK
            parameter.color = Color.WHITE
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ăâîșțĂÂÎȘȚ"
            parameter.minFilter = Texture.TextureFilter.Linear
            parameter.magFilter = Texture.TextureFilter.Linear
            val font = generator.generateFont(parameter)
            generator.dispose()
            font
        } catch (e: Exception) {
            Gdx.app.error("PuzzleState", "Nu s-a gasit 'font.ttf', folosesc fontul default scalat.")
            BitmapFont().apply {
                data.setScale(size / 15f)
                color = Color.WHITE
            }
        }
    }

    override fun update(delta: Float) {
        clockMs += (delta * 1000f).toLong()
        if (puzzleSolved || puzzleFailed) {
            if (!resultSoundPlayed) {
                resultSoundPlayed = true
                SoundManager.playSfx(
                    if (puzzleSolved) SoundManager.SFX_PUZZLE_SUCCESS else SoundManager.SFX_PUZZLE_FAIL
                )
            }
            if (Gdx.input.justTouched()) {
                val touchX = Gdx.input.x.toFloat()
                val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

                if (puzzleSolved && nextPuzzleButtonBounds.contains(touchX, touchY)) {
                    SoundManager.click()
                    handlePuzzleSuccess()
                } else if (puzzleFailed && retryButtonBounds.contains(touchX, touchY)) {
                    SoundManager.click()
                    puzzleSolved = false
                    puzzleFailed = false
                    puzzleActive = true
                    optionBounds1.clear()
                    gemBounds2.clear()
                    dropZoneBounds2.clear()
                    selectedGemIndex2 = -1
                    wrongAttempts2 = 0
                    generatePuzzle()
                }
            }
            return
        }

        if (puzzleActive) {
            // Cât timp jocul arată secvența, cronometrul stă pe loc.
            if (puzzleId == 4 && simonShowing) puzzleStartTime += (delta * 1000f).toLong()
            if (clockMs - puzzleStartTime > TIME_LIMIT_MS) {
                puzzleFailed = true
                puzzleActive = false
            } else {
                handleInput()
            }

            // Auto-validare pentru puzzle 2
            if (puzzleId == 2 && playerOrder2.none { it == "?" }) {
                if (checkOrder()) {
                    puzzleSolved = true
                    puzzleActive = false
                } else {
                    wrongAttempts2++
                    if (wrongAttempts2 >= MAX_WRONG_ATTEMPTS) {
                        puzzleFailed = true
                        puzzleActive = false
                    } else {
                        playerOrder2.clear()
                        repeat(4) { playerOrder2.add("?") }
                        selectedGemIndex2 = -1
                    }
                }
            }

            if (puzzleId == 4) updateSimon(delta)

            // Potrivirea cartilor pentru puzzle 5
            if (puzzleId == 5 && cardRevealTime5 > 0 &&
                clockMs - cardRevealTime5 > CARD_REVEAL_DURATION_MS) {
                if (cardLayout5[firstCardIndex5] == cardLayout5[secondCardIndex5]) {
                    pairsFound5++
                    if (pairsFound5 >= 8) {
                        puzzleSolved = true
                        puzzleActive = false
                    }
                } else {
                    revealedCards5[firstCardIndex5] = false
                    revealedCards5[secondCardIndex5] = false
                    SoundManager.playSfx(SoundManager.SFX_CARD_FLIP, 0.6f)  // cărțile se întorc la loc
                }
                firstCardIndex5 = -1
                secondCardIndex5 = -1
                cardRevealTime5 = 0
            }
        }
    }

    override fun render(batch: SpriteBatch) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        if (batch.isDrawing) batch.end()

        uiMatrix.setToOrtho2D(0f, 0f, width, height)
        shapeRenderer.projectionMatrix = uiMatrix
        batch.projectionMatrix = uiMatrix

        // Overlay negru semi-transparent
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.85f)
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()

        batch.begin()

        val centerX = width / 2f
        val centerY = height / 2f

        // Titlu + obiectiv
        titleFont.color = Color.YELLOW
        drawCentered(batch, titleFont, currentPuzzleTitle, centerX, height - 24f * s)

        textFont.color = Color.WHITE
        drawCentered(batch, textFont, currentObjective, centerX, height - 70f * s)

        // Timer
        if (puzzleActive) {
            val timeLeft = TIME_LIMIT_MS - (clockMs - puzzleStartTime)
            textFont.color = Color.RED
            textFont.draw(batch, "Time: %.1f s".format(timeLeft / 1000f), 20f * s, height - 24f * s)
        }

        // Continut specific puzzle-ului
        when (puzzleId) {
            1 -> drawPuzzle1(batch, centerX, centerY)
            2 -> drawPuzzle2(batch, centerX, centerY)
            3 -> drawPuzzle3(batch, centerX, centerY)
            4 -> drawPuzzle4(batch, centerX, centerY)
            5 -> drawPuzzle5(batch, centerX, centerY)
        }

        // Fereastra de rezultat
        if (puzzleSolved) {
            drawResultPanel(
                batch, centerX, centerY,
                "PUZZLE SOLVED!", Color.GREEN,
                "Continue", Color(0f, 0.5f, 0f, 1f),
                nextPuzzleButtonBounds
            )
        } else if (puzzleFailed) {
            drawResultPanel(
                batch, centerX, centerY,
                "WRONG! Try again.", Color.RED,
                "Try again", Color(0.6f, 0f, 0f, 1f),
                retryButtonBounds
            )
        }

        batch.end()
    }

    private fun drawResultPanel(
        batch: SpriteBatch, centerX: Float, centerY: Float,
        message: String, messageColor: Color,
        buttonText: String, buttonColor: Color,
        buttonBounds: Rectangle
    ) {
        val panelW = 640f * s
        val panelH = 220f * s
        drawRectFilled(batch, centerX - panelW / 2f, centerY - panelH / 2f, panelW, panelH, Color(0f, 0f, 0f, 0.9f))
        drawRectLine(batch, centerX - panelW / 2f, centerY - panelH / 2f, panelW, panelH, Color.GOLD)

        titleFont.color = messageColor
        drawCentered(batch, titleFont, message, centerX, centerY + 80f * s)

        val btnWidth = 480f * s
        val btnHeight = 76f * s
        val btnX = centerX - btnWidth / 2f
        val btnY = centerY - 80f * s
        buttonBounds.set(btnX, btnY, btnWidth, btnHeight)

        drawRectFilled(batch, btnX, btnY, btnWidth, btnHeight, buttonColor)
        drawRectLine(batch, btnX, btnY, btnWidth, btnHeight, Color.WHITE)

        textFont.color = Color.WHITE
        val btnLayout = GlyphLayout(textFont, buttonText)
        textFont.draw(
            batch, buttonText,
            btnX + (btnWidth - btnLayout.width) / 2f,
            btnY + (btnHeight + btnLayout.height) / 2f
        )
    }

    private fun generatePuzzle() {
        puzzleActive = true
        puzzleStartTime = clockMs
        resultSoundPlayed = false

        when (puzzleId) {
            1 -> {
                currentPuzzleTitle = "Symbol Matching"
                currentObjective = "Choose the missing symbol"
                grid1 = Array(3) { Array(3) { "" } }
                grid1[0][0] = symbols[0]; grid1[0][1] = symbols[1]; grid1[0][2] = symbols[2]
                grid1[2][0] = symbols[0]; grid1[2][1] = symbols[1]; grid1[2][2] = symbols[2]
                grid1[1][0] = symbols[3]; grid1[1][2] = symbols[3]
                grid1[1][1] = "?"
            }
            2 -> {
                currentPuzzleTitle = "Gem Ordering"
                currentObjective = "Place gems in correct order"
                correctOrder2 = listOf(gems[0], gems[1], gems[2], gems[3])
                playerOrder2.clear()
                repeat(4) { playerOrder2.add("?") }
                selectedGemIndex2 = -1
                wrongAttempts2 = 0
            }
            3 -> {
                currentPuzzleTitle = "Ancient Riddle"
                currentObjective = "Choose the correct answer"
                val riddleIndex = Random.nextInt(riddles.size)
                riddle3 = riddles[riddleIndex]
                answers3 = riddleAnswers[riddleIndex].toList()
                correctAnswerIndex3 = correctRiddleAnswers[riddleIndex]
                selectedAnswerIndex3 = -1
            }
            4 -> {
                currentPuzzleTitle = "Ancient Sequence"
                currentObjective = "Watch the symbols light up, then repeat the sequence"
                simonSequence.clear()
                repeat(SIMON_START_LENGTH) { simonSequence.add(Random.nextInt(4)) }
                simonWrong = 0
                simonStatus = ""
                startSimonPlayback()
            }
            5 -> {
                currentPuzzleTitle = "Find the Pair"
                currentObjective = "Find all pairs"
                val tempCardIds = mutableListOf<Int>()
                repeat(8) { i ->
                    tempCardIds.add(i)
                    tempCardIds.add(i)
                }
                tempCardIds.shuffle()
                cardLayout5.clear()
                cardLayout5.addAll(tempCardIds)
                revealedCards5.fill(false)
                firstCardIndex5 = -1
                secondCardIndex5 = -1
                pairsFound5 = 0
                cardRevealTime5 = 0L
            }
        }
    }

    private fun handleInput() {
        if (puzzleId == 4) {
            handleSimonKeyboard()
        }

        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            when (puzzleId) {
                1 -> checkSymbolClick(touchX, touchY)
                2 -> checkGemClick(touchX, touchY)
                3 -> checkAnswerClick(touchX, touchY)
                4 -> checkSimonClick(touchX, touchY)
                5 -> checkCardClick(touchX, touchY)
            }
        }
    }

    private fun checkSymbolClick(touchX: Float, touchY: Float) {
        for (i in optionBounds1.indices) {
            if (optionBounds1[i].contains(touchX, touchY)) {
                SoundManager.click()
                playerChoice1 = symbols[i]
                if (symbols[i] == "BOLT") {
                    puzzleSolved = true
                } else {
                    puzzleFailed = true
                }
                puzzleActive = false
                break
            }
        }
    }

    private fun checkGemClick(touchX: Float, touchY: Float) {
        // Click pe o zona de drop
        for (i in dropZoneBounds2.indices) {
            if (dropZoneBounds2[i].contains(touchX, touchY)) {
                SoundManager.click()
                if (selectedGemIndex2 >= 0 && playerOrder2[i] == "?") {
                    playerOrder2[i] = gems[selectedGemIndex2]
                    selectedGemIndex2 = -1
                } else if (playerOrder2[i] != "?") {
                    playerOrder2[i] = "?"
                    selectedGemIndex2 = -1
                }
                return
            }
        }

        // Click pe o piatra disponibila
        val availableGems = gems.filterNot { playerOrder2.contains(it) }
        for (i in gemBounds2.indices) {
            if (i < availableGems.size && gemBounds2[i].contains(touchX, touchY)) {
                SoundManager.click()
                val gemIndex = gems.indexOf(availableGems[i])
                selectedGemIndex2 = if (selectedGemIndex2 == gemIndex) -1 else gemIndex
                return
            }
        }
    }

    private fun checkAnswerClick(touchX: Float, touchY: Float) {
        for (i in answerBounds3.indices) {
            if (answerBounds3[i].contains(touchX, touchY)) {
                SoundManager.click()
                selectedAnswerIndex3 = i
                if (i == correctAnswerIndex3) {
                    puzzleSolved = true
                } else {
                    puzzleFailed = true
                }
                puzzleActive = false
                break
            }
        }
    }

    private fun checkCardClick(touchX: Float, touchY: Float) {
        // Nu accepta click-uri cat timp doua carti sunt intoarse si asteapta verificarea
        if (cardRevealTime5 > 0) return

        for (i in cardBounds5.indices) {
            if (cardBounds5[i].contains(touchX, touchY) && !revealedCards5[i]) {
                revealedCards5[i] = true
                SoundManager.playSfx(SoundManager.SFX_CARD_FLIP)
                if (firstCardIndex5 == -1) {
                    firstCardIndex5 = i
                } else if (secondCardIndex5 == -1 && i != firstCardIndex5) {
                    secondCardIndex5 = i
                    cardRevealTime5 = clockMs
                }
                break
            }
        }
    }

    private fun startSimonPlayback() {
        simonShowing = true
        simonShowTimer = -SIMON_START_DELAY
        simonLastShownStep = -1
        simonInputIndex = 0
        simonLitPad = -1
    }

    private fun updateSimon(delta: Float) {
        if (simonStatus.isNotEmpty() && clockMs - simonStatusTime > MESSAGE_DURATION_MS) simonStatus = ""

        if (!simonShowing) {
            if (simonLitTimer > 0f) {
                simonLitTimer -= delta
                if (simonLitTimer <= 0f) simonLitPad = -1
            }
            return
        }

        simonShowTimer += delta
        if (simonShowTimer < 0f) return
        val step = (simonShowTimer / (SIMON_ON + SIMON_OFF)).toInt()
        if (step >= simonSequence.size) {
            simonShowing = false
            simonLitPad = -1
            return
        }
        val inStep = simonShowTimer - step * (SIMON_ON + SIMON_OFF)
        simonLitPad = if (inStep < SIMON_ON) simonSequence[step] else -1
        if (step != simonLastShownStep) {
            simonLastShownStep = step
            SoundManager.playSfx(SoundManager.SFX_TONES[simonSequence[step]])
        }
    }

    private fun checkSimonClick(touchX: Float, touchY: Float) {
        if (simonShowing) return
        for (i in simonPadBounds.indices) {
            if (simonPadBounds[i].contains(touchX, touchY)) {
                onSimonPad(i)
                return
            }
        }
    }

    /** Suport pentru tastatura fizică (desktop): tastele 1-4. */
    private fun handleSimonKeyboard() {
        if (simonShowing) return
        for (i in 0..3) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) onSimonPad(i)
        }
    }

    private fun onSimonPad(pad: Int) {
        simonLitPad = pad
        simonLitTimer = 0.25f
        SoundManager.playSfx(SoundManager.SFX_TONES[pad])

        if (pad == simonSequence[simonInputIndex]) {
            simonInputIndex++
            if (simonInputIndex == simonSequence.size) {
                if (simonSequence.size >= SIMON_FINAL_LENGTH) {
                    puzzleSolved = true
                    puzzleActive = false
                } else {
                    simonStatus = "CORRECT!"
                    simonStatusTime = clockMs
                    simonSequence.add(Random.nextInt(4))
                    startSimonPlayback()
                }
            }
        } else {
            simonWrong++
            if (simonWrong >= MAX_WRONG_ATTEMPTS) {
                puzzleFailed = true
                puzzleActive = false
            } else {
                simonStatus = "WRONG! Watch again (${MAX_WRONG_ATTEMPTS - simonWrong} tries left)"
                simonStatusTime = clockMs
                startSimonPlayback()
            }
        }
    }

    private fun checkOrder(): Boolean = playerOrder2 == correctOrder2

    private fun handlePuzzleSuccess() {
        val gameState = refLink.gameState as? GameState
        gameState?.puzzleSolved(puzzleId)
        refLink.setState(gameState ?: GameOverState(refLink))
    }

    private fun handlePuzzleFailure() {
        val gameState = refLink.gameState as? GameState
        gameState?.onPuzzleFailure()
        refLink.setState(gameState ?: GameOverState(refLink))
    }

    // ==================== PUZZLE 1: SYMBOL MATCHING ====================
    private fun drawPuzzle1(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val cellSize = 100f * s
        val gridStartX = centerX - cellSize * 1.5f
        val gridTopY = Gdx.graphics.height - 120f * s

        val symbolImages = mapOf(
            "SUN"  to Assets.puzzle1Sun,
            "MOON" to Assets.puzzle1Moon,
            "STAR" to Assets.puzzle1Star,
            "BOLT" to Assets.puzzle1Bolt
        )

        for (row in 0..2) {
            for (col in 0..2) {
                val cellX = gridStartX + col * cellSize
                val cellY = gridTopY - (row + 1) * cellSize

                drawRectFilled(batch, cellX, cellY, cellSize, cellSize, Color(0.1f, 0.1f, 0.2f, 1f))
                drawRectLine(batch, cellX, cellY, cellSize, cellSize, Color.WHITE)

                val symbol = grid1[row][col]
                if (symbol == "?") {
                    bigFont.color = Color.YELLOW
                    val l = GlyphLayout(bigFont, "?")
                    bigFont.draw(batch, "?", cellX + (cellSize - l.width) / 2f, cellY + (cellSize + l.height) / 2f)
                } else {
                    symbolImages[symbol]?.let {
                        batch.draw(it, cellX + 10f * s, cellY + 10f * s, cellSize - 20f * s, cellSize - 20f * s)
                    }
                }
            }
        }

        val optionSize = 110f * s
        val gap = 16f * s
        val optionY = gridTopY - 3 * cellSize - optionSize - 60f * s
        val totalOptionsWidth = symbols.size * optionSize + (symbols.size - 1) * gap
        val optionStartX = centerX - totalOptionsWidth / 2f
        optionBounds1.clear()

        textFont.color = Color.WHITE
        drawCentered(batch, textFont, "Choose:", centerX, optionY + optionSize + 36f * s)

        for (i in symbols.indices) {
            val optX = optionStartX + i * (optionSize + gap)

            optionBounds1.add(Rectangle(optX, optionY, optionSize, optionSize))

            drawRectFilled(batch, optX, optionY, optionSize, optionSize, Color(0.2f, 0.2f, 0.5f, 1f))
            drawRectLine(batch, optX, optionY, optionSize, optionSize, Color.GOLD)

            symbolImages[symbols[i]]?.let {
                batch.draw(it, optX + 10f * s, optionY + 10f * s, optionSize - 20f * s, optionSize - 20f * s)
            }
        }
    }

    // ==================== PUZZLE 2: GEM ORDERING ====================
    private fun drawPuzzle2(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val gemSize = 96f * s
        val gap = 20f * s
        val totalWidth = 4 * gemSize + 3 * gap
        val startX = centerX - totalWidth / 2f
        val height = Gdx.graphics.height.toFloat()

        // Indiciu
        textFont.color = Color.YELLOW
        drawCentered(batch, textFont, "Clue: Emerald is between Sapphire and Ruby", centerX, height - 120f * s)

        // --- Zona de drop (sus) ---
        val dropY = height - 300f * s
        textFont.color = Color.WHITE
        textFont.draw(batch, "Drop here:", startX, dropY + gemSize + 34f * s)

        dropZoneBounds2.clear()
        for (i in 0..3) {
            val x = startX + i * (gemSize + gap)
            dropZoneBounds2.add(Rectangle(x, dropY, gemSize, gemSize))

            val bg = if (selectedGemIndex2 >= 0) Color(0.3f, 0.3f, 0.1f, 1f) else Color(0.15f, 0.15f, 0.15f, 1f)
            drawRectFilled(batch, x, dropY, gemSize, gemSize, bg)
            drawRectLine(batch, x, dropY, gemSize, gemSize, Color.GOLD)

            val placed = playerOrder2[i]
            if (placed != "?") {
                val placedIndex = gems.indexOf(placed)
                if (placedIndex >= 0) {
                    gemRegions[placedIndex]?.let {
                        batch.draw(it, x + 8f * s, dropY + 8f * s, gemSize - 16f * s, gemSize - 16f * s)
                    }
                }
                textFont.color = Color.WHITE
                textFont.draw(batch, placed.take(3), x + 8f * s, dropY + 28f * s)
            } else {
                textFont.color = Color(0.5f, 0.5f, 0.5f, 1f)
                val l = GlyphLayout(textFont, "${i + 1}")
                textFont.draw(batch, "${i + 1}", x + (gemSize - l.width) / 2f, dropY + (gemSize + l.height) / 2f)
            }
        }

        // --- Pietre disponibile (jos) ---
        val gemsY = height - 500f * s
        textFont.color = Color.WHITE
        textFont.draw(batch, "Gems:", startX, gemsY + gemSize + 34f * s)

        gemBounds2.clear()
        val availableGems = gems.filterNot { playerOrder2.contains(it) }

        for (i in availableGems.indices) {
            val x = startX + i * (gemSize + gap)
            gemBounds2.add(Rectangle(x, gemsY, gemSize, gemSize))

            val isSelected = gems.indexOf(availableGems[i]) == selectedGemIndex2

            drawRectFilled(batch, x, gemsY, gemSize, gemSize,
                if (isSelected) Color(0.5f, 0.5f, 0f, 1f) else Color(0.2f, 0.2f, 0.5f, 1f))
            drawRectLine(batch, x, gemsY, gemSize, gemSize,
                if (isSelected) Color.YELLOW else Color.GOLD)

            gemRegions[gems.indexOf(availableGems[i])]?.let {
                batch.draw(it, x + 8f * s, gemsY + 8f * s, gemSize - 16f * s, gemSize - 16f * s)
            }
            textFont.color = Color.WHITE
            textFont.draw(batch, availableGems[i].take(3), x + 8f * s, gemsY + 28f * s)
        }

        // Incercari ramase
        textFont.color = Color.RED
        textFont.draw(batch, "Attempts left: ${MAX_WRONG_ATTEMPTS - wrongAttempts2}", startX, gemsY - 24f * s)
    }

    // ==================== PUZZLE 3: ANCIENT RIDDLE ====================
    private fun drawPuzzle3(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val height = Gdx.graphics.height.toFloat()

        // Pergamentul antic ca fundal pentru ghicitoare
        val scrollW = 740f * s
        val scrollH = scrollW * 1100f / 2275f   // pastreaza proportiile imaginii
        val scrollX = centerX - scrollW / 2f
        val scrollY = height - 130f * s - scrollH

        val scroll = Assets.puzzle3Scroll
        if (scroll != null) {
            batch.draw(scroll, scrollX, scrollY, scrollW, scrollH)
        } else {
            drawRectFilled(batch, scrollX, scrollY, scrollW, scrollH, Color(0.35f, 0.25f, 0.12f, 1f))
            drawRectLine(batch, scrollX, scrollY, scrollW, scrollH, Color.GOLD)
        }

        // Textul ghicitorii, centrat pe pergament, cu word-wrap
        inkFont.color = Color(0.15f, 0.08f, 0.02f, 1f)
        val textWidth = scrollW * 0.62f
        val layout = GlyphLayout(inkFont, riddle3, inkFont.color, textWidth, Align.center, true)
        inkFont.draw(
            batch, riddle3,
            centerX - textWidth / 2f,
            scrollY + scrollH / 2f + layout.height / 2f,
            textWidth, Align.center, true
        )

        // Variante de raspuns: 3 butoane orizontale sub pergament
        val btnW = 280f * s
        val btnH = 80f * s
        val gap = 24f * s
        val totalW = answers3.size * btnW + (answers3.size - 1) * gap
        val btnStartX = centerX - totalW / 2f
        val btnY = scrollY - btnH - 30f * s

        answerBounds3.clear()
        for (i in answers3.indices) {
            val x = btnStartX + i * (btnW + gap)
            answerBounds3.add(Rectangle(x, btnY, btnW, btnH))

            drawRectFilled(batch, x, btnY, btnW, btnH, Color(0.2f, 0.2f, 0.5f, 1f))
            drawRectLine(batch, x, btnY, btnW, btnH, Color.GOLD)

            textFont.color = Color.WHITE
            val l = GlyphLayout(textFont, answers3[i])
            textFont.draw(batch, answers3[i], x + (btnW - l.width) / 2f, btnY + (btnH + l.height) / 2f)
        }
    }

    // ==================== PUZZLE 4: ANCIENT SEQUENCE ====================
    private fun drawPuzzle4(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val height = Gdx.graphics.height.toFloat()

        // Runda și ce are de făcut jucătorul
        val round = simonSequence.size - SIMON_START_LENGTH + 1
        val rounds = SIMON_FINAL_LENGTH - SIMON_START_LENGTH + 1
        textFont.color = Color.WHITE
        drawCentered(batch, textFont, "Round $round/$rounds", centerX, height - 115f * s)
        val hint = if (simonShowing) "Watch..." else "Your turn!  ${simonInputIndex}/${simonSequence.size}"
        bigFont.color = if (simonShowing) Color.LIGHT_GRAY else Color.YELLOW
        drawCentered(batch, bigFont, hint, centerX, height - 160f * s)

        // 4 plăci cu simboluri, pe un rând
        val symbolImages = listOf(Assets.puzzle1Sun, Assets.puzzle1Moon, Assets.puzzle1Star, Assets.puzzle1Bolt)
        val padSize = 170f * s
        val gap = 36f * s
        val totalW = 4 * padSize + 3 * gap
        val startX = centerX - totalW / 2f
        val padY = centerY - padSize / 2f - 50f * s

        for (i in 0 until 4) {
            val lit = i == simonLitPad
            val grow = if (lit) 10f * s else 0f
            val x = startX + i * (padSize + gap) - grow
            val y = padY - grow
            val size = padSize + grow * 2f
            simonPadBounds[i].set(startX + i * (padSize + gap), padY, padSize, padSize)

            drawRectFilled(batch, x, y, size, size, if (lit) Color(0.95f, 0.8f, 0.3f, 1f) else Color(0.15f, 0.15f, 0.35f, 1f))
            drawRectLine(batch, x, y, size, size, if (lit) Color.WHITE else Color.GOLD)
            batch.setColor(1f, 1f, 1f, if (lit || !simonShowing) 1f else 0.55f)
            symbolImages[i]?.let { batch.draw(it, x + 18f * s, y + 18f * s, size - 36f * s, size - 36f * s) }
            batch.setColor(1f, 1f, 1f, 1f)
        }

        if (simonStatus.isNotEmpty()) {
            textFont.color = if (simonStatus == "CORRECT!") Color.GREEN else Color.RED
            drawCentered(batch, textFont, simonStatus, centerX, padY - 30f * s)
        }
    }

    // ==================== PUZZLE 5: FIND THE PAIR ====================
    private fun drawPuzzle5(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val height = Gdx.graphics.height.toFloat()

        // Progres
        textFont.color = Color.WHITE
        textFont.draw(batch, "Pairs: $pairsFound5/8", 20f * s, height - 70f * s)

        // Grila de cărți 8 x 2, cât mai mari: lățimea e limitată de ecran (8 cărți pe rând),
        // înălțimea de spațiul de sub titlu (2 rânduri); păstrăm proporțiile cărții (60x84).
        val width = Gdx.graphics.width.toFloat()
        val gap = 16f * s
        val topMargin = 110f * s
        val bottomMargin = 30f * s
        val maxWByWidth = (width * 0.94f - 7 * gap) / 8f
        val maxHByHeight = (height - topMargin - bottomMargin - gap) / 2f
        val cardW = minOf(maxWByWidth, maxHByHeight * 60f / 84f)
        val cardH = cardW * 84f / 60f
        val gridW = 8 * cardW + 7 * gap
        val gridH = 2 * cardH + gap
        val gridX = centerX - gridW / 2f
        val gridTopY = height - topMargin - ((height - topMargin - bottomMargin) - gridH) / 2f

        for (i in 0 until 16) {
            val row = i / 8
            val col = i % 8
            val x = gridX + col * (cardW + gap)
            val y = gridTopY - (row + 1) * cardH - row * gap

            cardBounds5[i].set(x, y, cardW, cardH)

            if (revealedCards5[i]) {
                val faceIndex = cardLayout5[i]
                val face = Assets.puzzle5CardFaces?.getOrNull(faceIndex)
                if (face != null) {
                    batch.draw(face, x, y, cardW, cardH)
                } else {
                    // Fallback daca imaginea lipseste: caseta colorata cu numar
                    drawRectFilled(batch, x, y, cardW, cardH, Color(0.8f, 0.75f, 0.6f, 1f))
                    drawRectLine(batch, x, y, cardW, cardH, Color.GOLD)
                    textFont.color = Color.BLACK
                    val l = GlyphLayout(textFont, "${faceIndex + 1}")
                    textFont.draw(batch, "${faceIndex + 1}", x + (cardW - l.width) / 2f, y + (cardH + l.height) / 2f)
                }
            } else {
                val back = Assets.puzzle5CardBack
                if (back != null) {
                    batch.draw(back, x, y, cardW, cardH)
                } else {
                    drawRectFilled(batch, x, y, cardW, cardH, Color(0.15f, 0.2f, 0.45f, 1f))
                    drawRectLine(batch, x, y, cardW, cardH, Color.GOLD)
                }
            }
        }
    }

    // ==================== HELPERE ====================
    private fun extractGemRegion(region: TextureRegion, index: Int): TextureRegion {
        val w = region.regionWidth / 2
        val h = region.regionHeight / 2
        val col = index % 2
        val row = index / 2
        return TextureRegion(region, col * w, row * h, w, h)
    }

    private fun drawCentered(batch: SpriteBatch, font: BitmapFont, text: String, centerX: Float, y: Float) {
        val layout = GlyphLayout(font, text)
        font.draw(batch, text, centerX - layout.width / 2f, y)
    }

    private fun drawRectFilled(batch: SpriteBatch, x: Float, y: Float, w: Float, h: Float, color: Color) {
        batch.end()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = color
        shapeRenderer.rect(x, y, w, h)
        shapeRenderer.end()
        batch.begin()
    }

    private fun drawRectLine(batch: SpriteBatch, x: Float, y: Float, w: Float, h: Float, color: Color) {
        batch.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = color
        shapeRenderer.rect(x, y, w, h)
        shapeRenderer.end()
        batch.begin()
    }

    override fun dispose() {
        titleFont.dispose()
        textFont.dispose()
        bigFont.dispose()
        if (inkFontDelegate.isInitialized()) inkFont.dispose()
        shapeRenderer.dispose()
    }
}
