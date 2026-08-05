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
        private const val TOTAL_QUESTIONS_4 = 6
        private const val CARD_REVEAL_DURATION_MS = 1000L
    }

    /** Factor de scalare UI: 1.0 la 720p, ~1.5 pe un telefon 1080p. */
    private val s: Float = Gdx.graphics.height / 720f

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

    // Puzzle 4
    private val questions4 = mutableListOf<String>()
    private val answers4 = mutableListOf<Int>()
    private var playerInput4 = ""
    private var currentQuestionIndex4 = 0
    private var correctAnswersCount4 = 0
    private var lastAnswerStatus4 = ""
    private var lastStatusTime4 = 0L
    private val keypadLabels4 = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "DEL", "0", "OK")
    private val keypadBounds4 = List(keypadLabels4.size) { Rectangle() }

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
                    handlePuzzleSuccess()
                } else if (puzzleFailed && retryButtonBounds.contains(touchX, touchY)) {
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
            if (System.currentTimeMillis() - puzzleStartTime > TIME_LIMIT_MS) {
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

            // Auto-validare pentru puzzle 4
            if (puzzleId == 4 && currentQuestionIndex4 >= TOTAL_QUESTIONS_4) {
                puzzleSolved = true
                puzzleActive = false
            }

            if (puzzleId == 4 && lastAnswerStatus4.isNotEmpty() &&
                System.currentTimeMillis() - lastStatusTime4 > MESSAGE_DURATION_MS) {
                lastAnswerStatus4 = ""
            }

            // Potrivirea cartilor pentru puzzle 5
            if (puzzleId == 5 && cardRevealTime5 > 0 &&
                System.currentTimeMillis() - cardRevealTime5 > CARD_REVEAL_DURATION_MS) {
                if (cardLayout5[firstCardIndex5] == cardLayout5[secondCardIndex5]) {
                    pairsFound5++
                    if (pairsFound5 >= 8) {
                        puzzleSolved = true
                        puzzleActive = false
                    }
                } else {
                    revealedCards5[firstCardIndex5] = false
                    revealedCards5[secondCardIndex5] = false
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
            val timeLeft = TIME_LIMIT_MS - (System.currentTimeMillis() - puzzleStartTime)
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
                "Continuă", Color(0f, 0.5f, 0f, 1f),
                nextPuzzleButtonBounds
            )
        } else if (puzzleFailed) {
            drawResultPanel(
                batch, centerX, centerY,
                "GREȘIT! Încearcă din nou.", Color.RED,
                "Încearcă din nou", Color(0.6f, 0f, 0f, 1f),
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
        puzzleStartTime = System.currentTimeMillis()
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
                currentPuzzleTitle = "Math Game"
                currentObjective = "Solve 6 problems in 60 seconds"
                questions4.clear()
                answers4.clear()
                currentQuestionIndex4 = 0
                correctAnswersCount4 = 0
                playerInput4 = ""
                lastAnswerStatus4 = ""
                repeat(3) {
                    val a = Random.nextInt(1, 51)
                    val b = Random.nextInt(1, 51)
                    questions4.add("$a + $b = ?")
                    answers4.add(a + b)
                }
                repeat(3) {
                    val a = Random.nextInt(20, 51)
                    val b = Random.nextInt(1, a - 9)
                    questions4.add("$a - $b = ?")
                    answers4.add(a - b)
                }
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
            handleMathKeyboard()
        }

        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            when (puzzleId) {
                1 -> checkSymbolClick(touchX, touchY)
                2 -> checkGemClick(touchX, touchY)
                3 -> checkAnswerClick(touchX, touchY)
                4 -> checkKeypadClick(touchX, touchY)
                5 -> checkCardClick(touchX, touchY)
            }
        }
    }

    private fun checkSymbolClick(touchX: Float, touchY: Float) {
        for (i in optionBounds1.indices) {
            if (optionBounds1[i].contains(touchX, touchY)) {
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
                val gemIndex = gems.indexOf(availableGems[i])
                selectedGemIndex2 = if (selectedGemIndex2 == gemIndex) -1 else gemIndex
                return
            }
        }
    }

    private fun checkAnswerClick(touchX: Float, touchY: Float) {
        for (i in answerBounds3.indices) {
            if (answerBounds3[i].contains(touchX, touchY)) {
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
                if (firstCardIndex5 == -1) {
                    firstCardIndex5 = i
                } else if (secondCardIndex5 == -1 && i != firstCardIndex5) {
                    secondCardIndex5 = i
                    cardRevealTime5 = System.currentTimeMillis()
                }
                break
            }
        }
    }

    private fun checkKeypadClick(touchX: Float, touchY: Float) {
        for (i in keypadBounds4.indices) {
            if (keypadBounds4[i].contains(touchX, touchY)) {
                onKeypadPress(keypadLabels4[i])
                return
            }
        }
    }

    private fun onKeypadPress(label: String) {
        when (label) {
            "DEL" -> if (playerInput4.isNotEmpty()) playerInput4 = playerInput4.dropLast(1)
            "OK" -> submitMathAnswer()
            else -> if (playerInput4.length < 4) playerInput4 += label
        }
    }

    private fun submitMathAnswer() {
        if (currentQuestionIndex4 >= TOTAL_QUESTIONS_4) return
        val playerAnswer = playerInput4.toIntOrNull()
        if (playerAnswer == null) {
            playerInput4 = ""
            return
        }

        if (playerAnswer == answers4[currentQuestionIndex4]) {
            correctAnswersCount4++
            lastAnswerStatus4 = "CORRECT!"
            currentQuestionIndex4++
        } else {
            lastAnswerStatus4 = "WRONG!"
        }
        lastStatusTime4 = System.currentTimeMillis()
        playerInput4 = ""
    }

    /** Suport pastrat pentru tastatura fizica (varianta desktop). */
    private fun handleMathKeyboard() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            submitMathAnswer()
            return
        }
        for (i in 0..9) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i) ||
                Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0 + i)) {
                if (playerInput4.length < 4) playerInput4 += i.toString()
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && playerInput4.isNotEmpty()) {
            playerInput4 = playerInput4.dropLast(1)
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
        textFont.color = Color(0.15f, 0.08f, 0.02f, 1f)
        val textWidth = scrollW * 0.62f
        val layout = GlyphLayout(textFont, riddle3, textFont.color, textWidth, Align.center, true)
        textFont.draw(
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

    // ==================== PUZZLE 4: MATH GAME ====================
    private fun drawPuzzle4(batch: SpriteBatch, centerX: Float, centerY: Float) {
        if (currentQuestionIndex4 >= TOTAL_QUESTIONS_4) return

        val height = Gdx.graphics.height.toFloat()

        // Progres
        textFont.color = Color.WHITE
        drawCentered(batch, textFont, "Question ${currentQuestionIndex4 + 1}/$TOTAL_QUESTIONS_4", centerX, height - 120f * s)

        // Intrebarea
        bigFont.color = Color.WHITE
        drawCentered(batch, bigFont, questions4[currentQuestionIndex4], centerX, height - 180f * s)

        // Caseta de raspuns
        val boxW = 300f * s
        val boxH = 70f * s
        val boxX = centerX - boxW / 2f
        val boxY = height - 300f * s
        drawRectFilled(batch, boxX, boxY, boxW, boxH, Color(0.1f, 0.1f, 0.2f, 1f))
        drawRectLine(batch, boxX, boxY, boxW, boxH, Color.GOLD)

        bigFont.color = Color.YELLOW
        val inputText = if (playerInput4.isEmpty()) "_" else playerInput4
        val il = GlyphLayout(bigFont, inputText)
        bigFont.draw(batch, inputText, centerX - il.width / 2f, boxY + (boxH + il.height) / 2f)

        // Status CORRECT/WRONG
        if (lastAnswerStatus4.isNotEmpty()) {
            textFont.color = if (lastAnswerStatus4 == "CORRECT!") Color.GREEN else Color.RED
            drawCentered(batch, textFont, lastAnswerStatus4, centerX, boxY - 16f * s)
        }

        // Tastatura numerica on-screen
        val keySize = 92f * s
        val keyGap = 14f * s
        val keypadW = 3 * keySize + 2 * keyGap
        val keypadX = centerX - keypadW / 2f
        val keypadTopY = boxY - 60f * s

        for (i in keypadLabels4.indices) {
            val row = i / 3
            val col = i % 3
            val x = keypadX + col * (keySize + keyGap)
            val y = keypadTopY - (row + 1) * (keySize + keyGap)

            keypadBounds4[i].set(x, y, keySize, keySize)

            val label = keypadLabels4[i]
            val bg = when (label) {
                "OK" -> Color(0f, 0.45f, 0f, 1f)
                "DEL" -> Color(0.5f, 0.15f, 0.15f, 1f)
                else -> Color(0.2f, 0.2f, 0.45f, 1f)
            }
            drawRectFilled(batch, x, y, keySize, keySize, bg)
            drawRectLine(batch, x, y, keySize, keySize, Color.WHITE)

            val keyFont = if (label == "DEL" || label == "OK") textFont else bigFont
            keyFont.color = Color.WHITE
            val l = GlyphLayout(keyFont, label)
            keyFont.draw(batch, label, x + (keySize - l.width) / 2f, y + (keySize + l.height) / 2f)
        }
    }

    // ==================== PUZZLE 5: FIND THE PAIR ====================
    private fun drawPuzzle5(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val height = Gdx.graphics.height.toFloat()

        // Progres
        textFont.color = Color.WHITE
        textFont.draw(batch, "Pairs: $pairsFound5/8", 20f * s, height - 70f * s)

        // Grila de carti 4x4
        val cardW = 84f * s
        val cardH = cardW * 84f / 60f    // pastreaza proportiile cartii (60x84)
        val gap = 14f * s
        val gridW = 4 * cardW + 3 * gap
        val gridX = centerX - gridW / 2f
        val gridTopY = height - 110f * s

        for (i in 0 until 16) {
            val row = i / 4
            val col = i % 4
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
        shapeRenderer.dispose()
    }
}
