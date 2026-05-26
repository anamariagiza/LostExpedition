package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks
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

    private var puzzleStartTime = 0L
    private var puzzleActive = false
    private var puzzleSolved = false
    private var puzzleFailed = false
    private var currentPuzzleTitle = ""
    private var currentObjective = ""

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

    // Puzzle 3
    private var riddle3 = ""
    private var answers3 = listOf<String>()
    private var correctAnswerIndex3 = 0
    private var selectedAnswerIndex3 = -1
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

    // Puzzle 5
    private val cardLayout5 = mutableListOf<Int>()
    private val revealedCards5 = BooleanArray(16)
    private var firstCardIndex5 = -1
    private var secondCardIndex5 = -1
    private var pairsFound5 = 0
    private var cardRevealTime5 = 0L

    private val font = BitmapFont()
    private val shapeRenderer = ShapeRenderer()

    init {
        generatePuzzle()
    }

    override fun update(delta: Float) {
        if (puzzleSolved || puzzleFailed) {
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

            // Auto-validation for puzzle 2
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

            // Auto-validation for puzzle 4
            if (puzzleId == 4 && currentQuestionIndex4 >= TOTAL_QUESTIONS_4) {
                puzzleSolved = true
                puzzleActive = false
            }

            if (puzzleId == 4 && lastAnswerStatus4.isNotEmpty() &&
                System.currentTimeMillis() - lastStatusTime4 > MESSAGE_DURATION_MS) {
                lastAnswerStatus4 = ""
            }

            // Card matching for puzzle 5
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

        // Overlay negru
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()

        batch.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        batch.begin()

        val centerX = width / 2f
        val centerY = height / 2f

        // Title + Objective
        font.color = Color.YELLOW
        val titleLayout = GlyphLayout(font, currentPuzzleTitle)
        font.draw(batch, currentPuzzleTitle, centerX - titleLayout.width / 2f, height - 40f)

        font.color = Color.WHITE
        val objLayout = GlyphLayout(font, currentObjective)
        font.draw(batch, currentObjective, centerX - objLayout.width / 2f, height - 70f)

        // Timer
        if (puzzleActive) {
            val timeLeft = TIME_LIMIT_MS - (System.currentTimeMillis() - puzzleStartTime)
            font.color = Color.RED
            font.draw(batch, "Time: %.1f s".format(timeLeft / 1000f), 10f, height - 10f)
        }

        // Puzzle specific
        when (puzzleId) {
            1 -> drawPuzzle1(batch, centerX, centerY)
            2 -> drawPuzzle2(batch, centerX, centerY)
            3 -> drawPuzzle3(batch, centerX, centerY)
            4 -> drawPuzzle4(batch, centerX, centerY)
            5 -> drawPuzzle5(batch, centerX, centerY)
        }

        // Rezultate cu butoane
        if (puzzleSolved) {
            batch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.85f)
            shapeRenderer.rect(centerX - 280f, centerY - 70f, 560f, 170f)
            shapeRenderer.end()
            batch.begin()

            font.color = Color.GREEN
            val msgLayout = GlyphLayout(font, "PUZZLE SOLVED!")
            font.draw(batch, "PUZZLE SOLVED!", centerX - msgLayout.width / 2f, centerY + 70f)

            val btnWidth = 480f
            val btnHeight = 70f
            val btnX = centerX - btnWidth / 2f
            val btnY = centerY - 50f
            nextPuzzleButtonBounds.set(btnX, btnY, btnWidth, btnHeight)

            batch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0.5f, 0f, 1f)
            shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight)
            shapeRenderer.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.WHITE
            shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight)
            shapeRenderer.end()
            batch.begin()

            font.color = Color.WHITE
            val btnLayout = GlyphLayout(font, "Rezolva urmatorul puzzle")
            font.draw(
                batch, "Rezolva urmatorul puzzle",
                btnX + (btnWidth - btnLayout.width) / 2f,
                btnY + (btnHeight + btnLayout.height) / 2f
            )

        } else if (puzzleFailed) {
            batch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.85f)
            shapeRenderer.rect(centerX - 280f, centerY - 70f, 560f, 170f)
            shapeRenderer.end()
            batch.begin()

            font.color = Color.RED
            val msgLayout = GlyphLayout(font, "GRESIT! Incearca din nou.")
            font.draw(batch, "GRESIT! Incearca din nou.", centerX - msgLayout.width / 2f, centerY + 70f)

            val btnWidth = 340f
            val btnHeight = 70f
            val btnX = centerX - btnWidth / 2f
            val btnY = centerY - 50f
            retryButtonBounds.set(btnX, btnY, btnWidth, btnHeight)

            batch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0.6f, 0f, 0f, 1f)
            shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight)
            shapeRenderer.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.WHITE
            shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight)
            shapeRenderer.end()
            batch.begin()

            font.color = Color.WHITE
            val btnLayout = GlyphLayout(font, "Incearca din nou")
            font.draw(
                batch, "Incearca din nou",
                btnX + (btnWidth - btnLayout.width) / 2f,
                btnY + (btnHeight + btnLayout.height) / 2f
            )
        }

        batch.end()
    }

    private fun generatePuzzle() {
        puzzleActive = true
        puzzleStartTime = System.currentTimeMillis()

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
            }
            4 -> {
                currentPuzzleTitle = "Math Game"
                currentObjective = "Solve 6 problems in 60 seconds"
                questions4.clear()
                answers4.clear()
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
            }
        }
    }

    private fun handleInput() {
        if (puzzleId == 4) {
            handleMathInput()
            return
        }

        if (Gdx.input.justTouched()) {
            val mouseX = Gdx.input.x.toFloat()
            val mouseY = Gdx.graphics.height - Gdx.input.y.toFloat()

            when (puzzleId) {
                1 -> checkSymbolClick(mouseX, mouseY)
                2 -> checkGemClick(mouseX, mouseY)
                3 -> checkAnswerClick(mouseX, mouseY)
                5 -> checkCardClick(mouseX, mouseY)
            }
        }
    }

    private fun checkSymbolClick(mouseX: Float, mouseY: Float) {
        for (i in optionBounds1.indices) {
            if (optionBounds1[i].contains(mouseX, mouseY)) {
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

    private fun checkGemClick(mouseX: Float, mouseY: Float) {
        // Click pe o zonă de drop
        for (i in dropZoneBounds2.indices) {
            if (dropZoneBounds2[i].contains(mouseX, mouseY)) {
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

        // Click pe o piatră disponibilă
        val availableGems = gems.filterNot { playerOrder2.contains(it) }
        for (i in gemBounds2.indices) {
            if (i < availableGems.size && gemBounds2[i].contains(mouseX, mouseY)) {
                val gemIndex = gems.indexOf(availableGems[i])
                selectedGemIndex2 = if (selectedGemIndex2 == gemIndex) -1 else gemIndex
                return
            }
        }
    }

    private fun checkAnswerClick(mouseX: Float, mouseY: Float) {
        // Simplified
        puzzleSolved = true
        puzzleActive = false
    }

    private fun checkCardClick(mouseX: Float, mouseY: Float) {
        // Simplified
    }

    private fun handleMathInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            try {
                val playerAnswer = playerInput4.toInt()
                val correctAnswer = answers4[currentQuestionIndex4]

                if (playerAnswer == correctAnswer) {
                    correctAnswersCount4++
                    lastAnswerStatus4 = "CORRECT!"
                } else {
                    lastAnswerStatus4 = "WRONG!"
                }

                lastStatusTime4 = System.currentTimeMillis()
                currentQuestionIndex4++
                playerInput4 = ""
            } catch (e: NumberFormatException) {
                playerInput4 = ""
            }
        } else {
            for (i in 0..9) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i)) {
                    playerInput4 += i.toString()
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && playerInput4.isNotEmpty()) {
                playerInput4 = playerInput4.dropLast(1)
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

    // ==================== DRAW PUZZLE 1 ====================
    private fun drawPuzzle1(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val cellSize = 120f
        val gridStartX = centerX - cellSize * 1.5f
        val gridStartY = centerY + cellSize * 1.5f

        val symbolImages = mapOf(
            "SUN"  to Assets.puzzle1Sun,
            "MOON" to Assets.puzzle1Moon,
            "STAR" to Assets.puzzle1Star,
            "BOLT" to Assets.puzzle1Bolt
        )

        for (row in 0..2) {
            for (col in 0..2) {
                val cellX = gridStartX + col * cellSize
                val cellY = gridStartY - row * cellSize

                batch.end()
                shapeRenderer.projectionMatrix.setToOrtho2D(
                    0f, 0f,
                    Gdx.graphics.width.toFloat(),
                    Gdx.graphics.height.toFloat()
                )
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
                shapeRenderer.color = Color.WHITE
                shapeRenderer.rect(cellX, cellY, cellSize, cellSize)
                shapeRenderer.end()
                batch.begin()

                val symbol = grid1[row][col]
                if (symbol == "?") {
                    font.color = Color.YELLOW
                    font.draw(batch, "?", cellX + cellSize / 2f - 8f, cellY + cellSize / 2f + 8f)
                } else {
                    val img = symbolImages[symbol]
                    img?.let {
                        batch.draw(it, cellX + 8f, cellY + 8f, cellSize - 16f, cellSize - 16f)
                    }
                }
            }
        }

        val optionY = gridStartY - 3 * cellSize - 30f
        val optionStartX = centerX - symbols.size / 2f * (cellSize + 10f)
        optionBounds1.clear()

        font.color = Color.WHITE
        font.draw(batch, "Choose:", optionStartX - 10f, optionY + cellSize + 20f)

        for (i in symbols.indices) {
            val optX = optionStartX + i * (cellSize + 10f)
            val optY = optionY

            optionBounds1.add(Rectangle(optX, optY, cellSize, cellSize))

            batch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0.2f, 0.2f, 0.5f, 1f)
            shapeRenderer.rect(optX, optY, cellSize, cellSize)
            shapeRenderer.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.GOLD
            shapeRenderer.rect(optX, optY, cellSize, cellSize)
            shapeRenderer.end()
            batch.begin()

            val img = symbolImages[symbols[i]]
            img?.let {
                batch.draw(it, optX + 8f, optY + 8f, cellSize - 16f, cellSize - 16f)
            }
        }
    }

    // ==================== DRAW PUZZLE 2 ====================
    private fun drawPuzzle2(batch: SpriteBatch, centerX: Float, centerY: Float) {
        val gemSize = 100f
        val gap = 20f
        val totalWidth = 4 * gemSize + 3 * gap
        val startX = centerX - totalWidth / 2f

        val gemImages = arrayOf(
            Assets.puzzle2Gems?.let { extractGem(it, 0) },
            Assets.puzzle2Gems?.let { extractGem(it, 1) },
            Assets.puzzle2Gems?.let { extractGem(it, 2) },
            Assets.puzzle2Gems?.let { extractGem(it, 3) }
        )

        // Indiciu
        font.color = Color.YELLOW
        font.draw(batch, "Clue: Emerald is between Ruby and Diamond", startX, centerY + 200f)

        // --- ZONA DE DROP (sus) ---
        font.color = Color.WHITE
        font.draw(batch, "Drop here:", startX, centerY + 150f)

        dropZoneBounds2.clear()
        for (i in 0..3) {
            val x = startX + i * (gemSize + gap)
            val y = centerY + 60f
            dropZoneBounds2.add(Rectangle(x, y, gemSize, gemSize))

            batch.end()
            shapeRenderer.projectionMatrix.setToOrtho2D(
                0f, 0f,
                Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat()
            )
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = if (selectedGemIndex2 >= 0) Color(0.3f, 0.3f, 0.1f, 1f) else Color(0.15f, 0.15f, 0.15f, 1f)
            shapeRenderer.rect(x, y, gemSize, gemSize)
            shapeRenderer.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.GOLD
            shapeRenderer.rect(x, y, gemSize, gemSize)
            shapeRenderer.end()
            batch.begin()

            val placed = playerOrder2[i]
            if (placed != "?") {
                val placedIndex = gems.indexOf(placed)
                if (placedIndex >= 0) {
                    gemImages[placedIndex]?.let {
                        batch.draw(it, x + 8f, y + 8f, gemSize - 16f, gemSize - 16f)
                    }
                }
                font.color = Color.WHITE
                font.draw(batch, placed.take(3), x + 8f, y + 20f)
            } else {
                font.color = Color(0.5f, 0.5f, 0.5f, 1f)
                font.draw(batch, "${i + 1}", x + gemSize / 2f - 8f, y + gemSize / 2f + 8f)
            }
        }

        // --- PIETRE DISPONIBILE (jos) ---
        font.color = Color.WHITE
        font.draw(batch, "Gems:", startX, centerY - 20f)

        gemBounds2.clear()
        val availableGems = gems.filterNot { playerOrder2.contains(it) }

        for (i in availableGems.indices) {
            val x = startX + i * (gemSize + gap)
            val y = centerY - 120f
            gemBounds2.add(Rectangle(x, y, gemSize, gemSize))

            val isSelected = gems.indexOf(availableGems[i]) == selectedGemIndex2

            batch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = if (isSelected) Color(0.5f, 0.5f, 0f, 1f) else Color(0.2f, 0.2f, 0.5f, 1f)
            shapeRenderer.rect(x, y, gemSize, gemSize)
            shapeRenderer.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = if (isSelected) Color.YELLOW else Color.GOLD
            shapeRenderer.rect(x, y, gemSize, gemSize)
            shapeRenderer.end()
            batch.begin()

            gemImages[gems.indexOf(availableGems[i])]?.let {
                batch.draw(it, x + 8f, y + 8f, gemSize - 16f, gemSize - 16f)
            }
            font.color = Color.WHITE
            font.draw(batch, availableGems[i].take(3), x + 8f, y + 20f)
        }

        // Greșeli rămase
        font.color = Color.RED
        font.draw(batch, "Attempts left: ${MAX_WRONG_ATTEMPTS - wrongAttempts2}", startX, centerY - 160f)
    }

    // ==================== DRAW PUZZLE 3 ====================
    private fun drawPuzzle3(batch: SpriteBatch, centerX: Float, centerY: Float) {
        font.color = Color.WHITE
        font.draw(batch, riddle3, centerX - 200f, centerY + 50f)
        answers3.forEachIndexed { i, answer ->
            font.draw(batch, "${i + 1}. $answer", centerX - 100f, centerY - i * 30f)
        }
    }

    // ==================== DRAW PUZZLE 4 ====================
    private fun drawPuzzle4(batch: SpriteBatch, centerX: Float, centerY: Float) {
        if (currentQuestionIndex4 >= TOTAL_QUESTIONS_4) return

        font.color = Color.WHITE
        font.draw(batch, "Problem: ${questions4[currentQuestionIndex4]}", centerX - 100f, centerY + 50f)
        font.draw(batch, "Answer: $playerInput4", centerX - 80f, centerY)

        if (lastAnswerStatus4.isNotEmpty()) {
            font.color = if (lastAnswerStatus4 == "CORRECT!") Color.GREEN else Color.RED
            font.draw(batch, lastAnswerStatus4, centerX - 50f, centerY - 100f)
        }
    }

    // ==================== DRAW PUZZLE 5 ====================
    private fun drawPuzzle5(batch: SpriteBatch, centerX: Float, centerY: Float) {
        font.color = Color.WHITE
        font.draw(batch, "Pairs found: $pairsFound5/8", centerX - 100f, centerY)
    }

    // ==================== HELPER ====================
    private fun extractGem(region: TextureRegion, index: Int): TextureRegion {
        val w = region.regionWidth / 2
        val h = region.regionHeight / 2
        val col = index % 2
        val row = index / 2
        return TextureRegion(region, col * w, row * h, w, h)
    }

    override fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
    }
}
