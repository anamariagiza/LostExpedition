package com.lostexpedition.game.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
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

    fun setCanInteract(interact: Boolean) {
        canInteract = interact
    }

    override fun update() {
        if (canInteract && !isOpen) {
            val player = refLink.player
            if (player != null && bounds.overlaps(player.bounds)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    isOpen = true
                    refLink.setState(EndGameState(refLink))
                }
            }
        }
        // ✅ FIX: bounds.setPosition(x, y) șters
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
        // ✅ FIX: bounds.setPosition(x, y) șters
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
        // ✅ FIX: bounds.setPosition(x, y) șters
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

    override fun update() {
        val player = refLink.player
        if (player != null && bounds.overlaps(player.bounds)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                val gameState = refLink.gameState
                if (gameState != null && gameState.isWoodSignMessageShowing()) {
                    return
                }
                refLink.setState(PuzzleState(refLink, puzzleId))
            }
        }
        // ✅ FIX: bounds.setPosition(x, y) șters
    }

    override fun render(batch: SpriteBatch) {
        // Invisible trigger
    }

    fun getPuzzleId(): Int = puzzleId
}
