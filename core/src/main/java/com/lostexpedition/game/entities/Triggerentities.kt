package com.lostexpedition.game.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.states.EndGameState
import com.lostexpedition.game.states.PuzzleState
import com.lostexpedition.game.utils.RefLinks

// ==================== CHEST ====================
class Chest(
    refLink: RefLinks,
    x: Float,
    y: Float,
    width: Int,
    height: Int
) : Entity(refLink, x, y, width, height) {

    private var canInteract = false
    private val closedImage: TextureRegion? = Assets.chestClosed
    private val openImage: TextureRegion? = Assets.chestOpened
    private var isOpen = false

    // Zona de interacțiune extinsă
    private val interactBounds = Rectangle(x - 20f, y - 20f, width + 40f, height + 40f)

    fun setCanInteract(interact: Boolean) {
        canInteract = interact
    }

    override fun update() {
        if (canInteract && !isOpen) {
            val player = refLink.player

            if (player != null && interactBounds.overlaps(player.bounds.toRectangle())) {
                val interactPressed = Gdx.input.isKeyJustPressed(Input.Keys.E) ||
                    (refLink.touchController != null && refLink.touchController.isInteractJustPressed)

                if (interactPressed) {
                    isOpen = true
                    refLink.setState(EndGameState(refLink))
                }
            }
        }
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        val image = if (isOpen) openImage else closedImage
        image?.let {
            batch.draw(it, x, y, width.toFloat(), height.toFloat())
        }
    }
}

// ==================== CAVE ENTRANCE ====================
class CaveEntrance(
    refLink: RefLinks,
    x: Float,
    y: Float,
    width: Int,
    height: Int
) : Entity(refLink, x, y, width, height) {

    override fun update() {
        // Invisible trigger - logica e în GameState
    }

    override fun render(batch: SpriteBatch) {
        // Invisible trigger - part of map
    }
}

// ==================== LEVEL EXIT ====================
class LevelExit(
    refLink: RefLinks,
    x: Float,
    y: Float,
    width: Int,
    height: Int
) : Entity(refLink, x, y, width, height) {

    override fun update() {
        // Invisible trigger - logica e în GameState
    }

    override fun render(batch: SpriteBatch) {
        // Invisible trigger - part of map
    }
}

// ==================== PUZZLE TRIGGER ====================
class PuzzleTrigger(
    refLink: RefLinks,
    x: Float,
    y: Float,
    width: Int,
    height: Int,
    private val puzzleId: Int
) : Entity(refLink, x, y, width, height) {

    // Zona de interacțiune extinsă
    private val interactBounds = Rectangle(x - 30f, y - 30f, width + 60f, height + 60f)

    override fun update() {
        val player = refLink.player

        // ✅ FIX: Am adăugat .toRectangle()
        if (player != null && interactBounds.overlaps(player.bounds.toRectangle())) {

            val interactPressed = Gdx.input.isKeyJustPressed(Input.Keys.E) ||
                (refLink.touchController != null && refLink.touchController.isInteractJustPressed)

            if (interactPressed) {
                val gameState = refLink.gameState
                if (gameState != null && gameState.isWoodSignMessageShowing()) {
                    return
                }
                refLink.setState(PuzzleState(refLink, puzzleId))
            }
        }
    }

    override fun render(batch: SpriteBatch) {
        // Invisible trigger
    }

    fun getPuzzleId(): Int = puzzleId
}
