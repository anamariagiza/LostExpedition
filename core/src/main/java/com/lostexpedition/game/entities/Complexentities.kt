package com.lostexpedition.game.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks

// ==================== NPC ====================
class NPC(
    refLink: RefLinks,
    x: Float,
    y: Float
) : Entity(refLink, x, y, 64, 64) {

    private val idleAnimation: Animation<TextureRegion>? = Assets.npcIdle
    private var stateTime = 0f

    override fun update() {
        stateTime += Gdx.graphics.deltaTime
        // ✅ FIX: bounds.setPosition(x, y) a fost șters deoarece bounds se calculează automat
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        val currentFrame = idleAnimation?.getKeyFrame(stateTime, true)
        currentFrame?.let {
            batch.draw(it, x, y, width.toFloat(), height.toFloat())
        }
    }
}

// ==================== TRAP ====================
class Trap(
    refLink: RefLinks,
    x: Float,
    y: Float,
    private val image: TextureRegion?
) : Entity(refLink, x, y, 32, 32) {

    val damage = 30
    private var active = false
    private var activationTime = 0f
    private var blinkTime = 0f
    private val blinkSpeed = 10f

    fun setActive(isActive: Boolean) {
        active = isActive
        if (isActive) {
            activationTime = 0f
        }
    }

    fun isActive(): Boolean = active

    override fun update() {
        if (active) {
            activationTime += Gdx.graphics.deltaTime
            blinkTime += Gdx.graphics.deltaTime
        }
        // ✅ FIX: bounds.setPosition(x, y) șters
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        image?.let {
            if (active) {
                val alpha = if (kotlin.math.sin(blinkTime * blinkSpeed).toFloat() > 0) 1f else 0.3f
                val oldColor = batch.color.cpy()
                batch.setColor(1f, 1f, 1f, alpha)
                batch.draw(it, x, y, width.toFloat(), height.toFloat())
                batch.color = oldColor
            } else {
                batch.draw(it, x, y, width.toFloat(), height.toFloat())
            }
        }
    }
}

// ==================== TRAP TRIGGER ====================
class TrapTrigger(
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
        // Invisible trigger
    }
}

// ==================== DECORATIVE OBJECT ====================
class DecorativeObject(
    refLink: RefLinks,
    x: Float,
    y: Float,
    width: Int,
    height: Int,
    private val image: TextureRegion?,
    private val hasInteraction: Boolean = false
) : Entity(refLink, x, y, width, height) {

    private var dialogueMessage: String? = null

    fun setDialogueMessage(message: String) {
        dialogueMessage = message
    }

    override fun update() {
        if (hasInteraction) {
            val player = refLink.player
            if (player != null && bounds.overlaps(player.bounds)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    val gameState = refLink.gameState
                    if (gameState != null && dialogueMessage != null) {
                        if (gameState.isWoodSignMessageShowing()) {
                            gameState.showWoodSignMessage(null)
                        } else {
                            gameState.showWoodSignMessage(dialogueMessage)
                        }
                    }
                }
            }
        }
        // ✅ FIX: bounds.setPosition(x, y) șters
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        image?.let {
            batch.draw(it, x, y, width.toFloat(), height.toFloat())
        }
    }
}
