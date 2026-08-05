package com.lostexpedition.game.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.input.TouchController
import com.lostexpedition.game.utils.RefLinks

//NPC
class NPC(
    refLink: RefLinks,
    x: Float,
    y: Float
) : Entity(refLink, x, y, 64, 64) {

    private val idleAnimation: Animation<TextureRegion>? = Assets.npcIdle
    private var stateTime = 0f

    private var hasGivenTalisman = false
    private var dialogueMessage: String = ""
    private var showDialogue = false
    private var dialogueTimer = 0f

    private val font = BitmapFont()

    fun checkPlayerInteraction(player: Player, touchController: TouchController, hasTalisman: Boolean) {
        if (!player.bounds.overlaps(bounds)) return

        if (touchController.isInteractJustPressed || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            dialogueMessage = when {
                hasGivenTalisman -> "Drum bun, explorator."
                hasTalisman -> {
                    hasGivenTalisman = true
                    "Mulțumesc! Calea înainte este acum deschisă."
                }
                else -> "Găsește talismanul antic și adu-mi-l."
            }
            showDialogue = true
            dialogueTimer = 3f
        }
    }

    override fun update() {
        stateTime += Gdx.graphics.deltaTime
        if (showDialogue) {
            dialogueTimer -= Gdx.graphics.deltaTime
            if (dialogueTimer <= 0f) {
                showDialogue = false
            }
        }
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        val currentFrame = idleAnimation?.getKeyFrame(stateTime, true)
        currentFrame?.let {
            batch.draw(it, x, y, width.toFloat(), height.toFloat())
        }

        if (showDialogue && dialogueMessage.isNotEmpty()) {
            font.draw(batch, dialogueMessage, x - 50f, y + height + 20f)
        }
    }
}

//TRAP
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
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        image?.let {
            if (active) {
                val alpha = if (kotlin.math.sin(blinkTime.toDouble() * blinkSpeed).toFloat() > 0) 1f else 0.3f
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

//TRAP TRIGGER
class TrapTrigger(
    refLink: RefLinks,
    x: Float,
    y: Float,
    width: Int,
    height: Int
) : Entity(refLink, x, y, width, height) {

    override fun update() {
    }

    override fun render(batch: SpriteBatch) {
        // Trigger invizibil, nu se desenează
    }
}

//DECORATIVE OBJECT
class DecorativeObject(
    refLink: RefLinks,
    x: Float,
    y: Float,
    width: Int,
    height: Int,
    private val image: TextureRegion?,
    val isInteractable: Boolean = false
) : Entity(refLink, x, y, width, height) {

    private var dialogueMessage: String? = null

    fun setDialogueMessage(message: String) {
        dialogueMessage = message
    }

    fun getDialogueMessage(): String? {
        return dialogueMessage
    }

    override fun update() {
        // Logică mutată în GameState
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        image?.let {
            batch.draw(it, x, y, width.toFloat(), height.toFloat())
        }
    }
}
