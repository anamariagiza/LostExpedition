package com.lostexpedition.game.entities

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.utils.RefLinks

// ==================== ANIMAL ====================
class Animal(
    refLink: RefLinks,
    startX: Float,
    startY: Float,
    private val leftBound: Float,
    private val rightBound: Float,
    private val type: AnimalType
) : Entity(refLink, startX, startY, 48, 48) {

    enum class AnimalType {
        JAGUAR, MONKEY, BAT
    }

    val damage = when (type) {
        AnimalType.JAGUAR -> 30
        AnimalType.MONKEY -> 15
        AnimalType.BAT -> 20
    }

    private var velocityX = 2f
    private val animation: Animation<TextureRegion>?
    private var stateTime = 0f

    init {
        animation = when (type) {
            AnimalType.JAGUAR -> Assets.jaguarAnimation
            AnimalType.MONKEY -> Assets.monkeyAnimation
            AnimalType.BAT -> Assets.batAnimation
        }
    }

    override fun update() {
        stateTime += com.badlogic.gdx.Gdx.graphics.deltaTime

        x += velocityX

        if (x <= leftBound || x >= rightBound) {
            velocityX = -velocityX
        }
        // ✅ FIX: bounds.setPosition(x, y) șters
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        val currentFrame = animation?.getKeyFrame(stateTime, true)
        currentFrame?.let {
            // INVERSAT: Animalele se uită la stânga default. Le dăm flip DOAR când merg spre dreapta.
            val flipX = velocityX > 0f

            batch.draw(
                it,
                if (flipX) x + width else x,
                y,
                if (flipX) -width.toFloat() else width.toFloat(),
                height.toFloat()
            )
        }
    }

    fun takeDamage(amount: Int) {
        // Animals don't have health in this implementation
    }
}

// ==================== KEY ====================
class Key(
    refLink: RefLinks,
    x: Float,
    y: Float,
    private val image: TextureRegion?,
    val associatedPuzzleId: Int
) : Entity(refLink, x, y, 32, 32) {

    private val bobSpeed = 2f
    private val bobAmount = 5f
    private var bobTime = 0f
    private val originalY = y

    override fun update() {
        bobTime += com.badlogic.gdx.Gdx.graphics.deltaTime
        y = originalY + kotlin.math.sin(bobTime * bobSpeed) * bobAmount
        // ✅ FIX: bounds.setPosition(x, y) șters
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        image?.let {
            batch.draw(it, x, y, width.toFloat(), height.toFloat())
        }
    }
}

// ==================== TALISMAN ====================
class Talisman(
    refLink: RefLinks,
    x: Float,
    y: Float,
    private val image: TextureRegion?
) : Entity(refLink, x, y, 48, 48) {

    private val bobSpeed = 2f
    private val bobAmount = 5f
    private var bobTime = 0f
    private val originalY = y
    private val glowSpeed = 3f
    private var glowTime = 0f

    override fun update() {
        bobTime += com.badlogic.gdx.Gdx.graphics.deltaTime
        glowTime += com.badlogic.gdx.Gdx.graphics.deltaTime

        y = originalY + kotlin.math.sin(bobTime * bobSpeed) * bobAmount
        // ✅ FIX: bounds.setPosition(x, y) șters
    }

    override fun render(batch: SpriteBatch) {
        if (!isOnScreen()) return

        image?.let {
            val glow = 0.7f + kotlin.math.sin(glowTime * glowSpeed) * 0.3f
            val oldColor = batch.color.cpy()
            batch.setColor(glow, glow, glow, 1f)
            batch.draw(it, x, y, width.toFloat(), height.toFloat())
            batch.color = oldColor
        }
    }
}
