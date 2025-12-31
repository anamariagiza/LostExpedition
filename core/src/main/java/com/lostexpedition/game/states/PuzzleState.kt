package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
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

    // Puzzle 1
    private var grid1 = Array(3) { Array(3) { "" } }
    private var playerChoice1 = ""
    private val symbols = arrayOf("SUN", "MOON", "STAR", "BOLT")

    // Puzzle 2
    private var correctOrder2 = listOf<String>()
    private val playerOrder2 = mutableListOf<String>()
    private var clue2 = ""
    private val gems = arrayOf("SAPPHIRE", "EMERALD", "RUBY", "DIAMOND")
    private var wrongAttempts2 = 0

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
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (puzzleSolved) {
                    handlePuzzleSuccess()
                } else {
                    handlePuzzleFailure()
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

        batch.end()

        // Overlay
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, width, height)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)
        shapeRenderer.rect(0f, 0f, width, height)
        shapeRenderer.end()

        batch.begin()
        batch.projectionMatrix.setToOrtho2D(0f, 0f, width, height)

        val centerX = width / 2f
        val centerY = height / 2f

        // Title
        font.color = Color.YELLOW
        font.draw(batch, currentPuzzleTitle, centerX - 100f, centerY + 200f)
        font.draw(batch, currentObjective, centerX - 150f, centerY + 150f)

        // Timer
        if (puzzleActive) {
            val timeLeft = TIME_LIMIT_MS - (System.currentTimeMillis() - puzzleStartTime)
            font.color = Color.RED
            font.draw(batch, "Time: %.1f s".format(timeLeft / 1000f), 10f, height - 10f)
        }

        // Draw puzzle-specific content
        when (puzzleId) {
            1 -> drawPuzzle1(batch, centerX, centerY)
            2 -> drawPuzzle2(batch, centerX, centerY)
            3 -> drawPuzzle3(batch, centerX, centerY)
            4 -> drawPuzzle4(batch, centerX, centerY)
            5 -> drawPuzzle5(batch, centerX, centerY)
        }

        // Result messages
        if (puzzleSolved) {
            font.color = Color.GREEN
            font.draw(batch, "PUZZLE SOLVED!", centerX - 80f, centerY)
            font.color = Color.WHITE
            font.draw(batch, "Press ENTER to continue", centerX - 100f, centerY - 50f)
        } else if (puzzleFailed) {
            font.color = Color.RED
            font.draw(batch, "PUZZLE FAILED!", centerX - 100f, centerY)
            font.color = Color.WHITE
            font.draw(batch, "Press ENTER to continue", centerX - 100f, centerY - 50f)
        }
    }

    private fun generatePuzzle() {
        puzzleActive = true
        puzzleStartTime = System.currentTimeMillis()

        when (puzzleId) {
            1 -> {
                currentPuzzleTitle = "Symbol Matching"
                currentObjective = "Choose the missing symbol"
                grid1[0][0] = symbols[0]; grid1[0][1] = symbols[1]; grid1[0][2] = symbols[2]
                grid1[2][0] = symbols[0]; grid1[2][1] = symbols[1]; grid1[2][2] = symbols[2]
                grid1[1][0] = symbols[3]; grid1[1][2] = symbols[3]
                grid1[1][1] = "?"
            }
            2 -> {
                currentPuzzleTitle = "Gem Ordering"
                currentObjective = "Place gems in order"
                correctOrder2 = listOf(gems[0], gems[1], gems[2], gems[3])
                playerOrder2.clear()
                repeat(4) { playerOrder2.add("?") }
                clue2 = "Order: Sapphire, Emerald, Ruby, Diamond"
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
        // Simplified - just solve on any click for now
        puzzleSolved = true
        puzzleActive = false
    }

    private fun checkGemClick(mouseX: Float, mouseY: Float) {
        // Simplified
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

    private fun drawPuzzle1(batch: SpriteBatch, centerX: Float, centerY: Float) {
        font.color = Color.WHITE
        font.draw(batch, "Click to solve puzzle 1", centerX - 100f, centerY)
    }

    private fun drawPuzzle2(batch: SpriteBatch, centerX: Float, centerY: Float) {
        font.color = Color.WHITE
        font.draw(batch, clue2, centerX - 150f, centerY)
    }

    private fun drawPuzzle3(batch: SpriteBatch, centerX: Float, centerY: Float) {
        font.color = Color.WHITE
        font.draw(batch, riddle3, centerX - 200f, centerY + 50f)
        answers3.forEachIndexed { i, answer ->
            font.draw(batch, "${i + 1}. $answer", centerX - 100f, centerY - i * 30f)
        }
    }

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

    private fun drawPuzzle5(batch: SpriteBatch, centerX: Float, centerY: Float) {
        font.color = Color.WHITE
        font.draw(batch, "Pairs found: $pairsFound5/8", centerX - 100f, centerY)
    }

    override fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
    }
}
