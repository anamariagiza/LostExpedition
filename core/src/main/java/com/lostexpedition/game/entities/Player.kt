package com.lostexpedition.game.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.RefLinks
import kotlin.math.abs

enum class Direction {
    UP, DOWN, LEFT, RIGHT;
}

class Player(
    refLink: RefLinks,
    startX: Float,
    startY: Float
) : Entity(refLink, startX, startY, 64, 64) {

    override var health: Int = 100
    private val maxHealth = 100
    private var isHurt: Boolean = false

    private val normalSpeed = 3f
    private val runSpeed = 5f
    private var currentSpeed = normalSpeed
    private var xMove = 0f
    private var yMove = 0f

    private var stateTime = 0f
    private lateinit var currentAnimation: Animation<TextureRegion>
    // ✅ NEW: Ținem minte ultima animație pentru a reseta timer-ul
    private var previousAnimation: Animation<TextureRegion>? = null
    private var currentFrame: TextureRegion

    var direction: Direction = Direction.DOWN
        private set

    private var facingRight = true
    private var facingDown = true

    private var isAttacking = false
    private var attackTime = 0f
    private val attackDuration = 0.5f
    private val attackRange = 80f
    private val attackDamage = 25

    private var isJumping = false
    private var jumpTime = 0f
    private val jumpDuration = 0.5f

    init {
        currentAnimation = Assets.playerIdleDown ?: createDefaultAnimation()
        currentFrame = currentAnimation.getKeyFrame(0f)
    }

    private fun createDefaultAnimation(): Animation<TextureRegion> {
        val placeholderRegion = TextureRegion()
        return Animation(0.1f, placeholderRegion)
    }

    override fun update() {
        handleInput()
        move()
        updateAnimations()

        if (isAttacking) {
            attackTime += Gdx.graphics.deltaTime
            if (attackTime >= attackDuration) {
                isAttacking = false
                attackTime = 0f
            }
        }

        if (isJumping) {
            jumpTime += Gdx.graphics.deltaTime
            if (jumpTime >= jumpDuration) {
                isJumping = false
                jumpTime = 0f
            }
        }
    }

    private fun handleInput() {
        xMove = 0f
        yMove = 0f
        currentSpeed = normalSpeed

        val touchController = refLink.touchController

        // Touch Controls
        if (touchController != null) {
            if (touchController.isJoystickActive) {
                val joyX = touchController.joystickDeltaX
                val joyY = touchController.joystickDeltaY

                xMove = joyX * currentSpeed
                yMove = joyY * currentSpeed

                if (abs(joyX) > 0.1f) facingRight = joyX > 0
                if (abs(joyY) > 0.1f) facingDown = joyY < 0
            }

            if (touchController.isAttackJustPressed && !isAttacking) {
                performAttack()
            }
        }

        // Keyboard Controls
        if (Gdx.input.isKeyPressed(Input.Keys.W)) yMove = currentSpeed
        if (Gdx.input.isKeyPressed(Input.Keys.S)) yMove = -currentSpeed
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            xMove = -currentSpeed
            facingRight = false
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            xMove = currentSpeed
            facingRight = true
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            currentSpeed = runSpeed
            if (xMove != 0f) xMove = (xMove / normalSpeed) * runSpeed
            if (yMove != 0f) yMove = (yMove / normalSpeed) * runSpeed
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isAttacking) {
            performAttack()
        }

        updateDirection()
    }

    private fun performAttack() {
        isAttacking = true
        attackTime = 0f

        val attackBounds = Rectangle(
            if (facingRight) x + width else x - attackRange,
            y,
            attackRange,
            height.toFloat()
        )

        val gameState = refLink.gameState
        gameState?.let { state ->
            for (entity in state.getEntities()) {
                val entityRect = entity.bounds.toRectangle()
                if ((entity is Agent || entity is Animal) && attackBounds.overlaps(entityRect)) {
                    // Verificăm să nu ne lovim singuri (deși lista entităților nu include player-ul de obicei)
                    if (entity != this) {
                        // Presupunând că Agent/Animal au metoda takeDamage
                        if (entity is Agent) entity.takeDamage(attackDamage)
                        if (entity is Animal) entity.takeDamage(attackDamage)
                    }
                }
            }
        }
    }

    private fun move() {
        moveX()
        moveY()
    }

    private fun moveX() {
        if (xMove == 0f) return
        val newX = x + xMove
        val map = refLink.map ?: return
        val TS = TileConstants.TILE_SIZE

        val tileXLeft = ((newX) / TS).toInt()
        val tileXRight = ((newX + width - 1) / TS).toInt()
        val tileYBottom = (y / TS).toInt()
        val tileYTop = ((y + height - 1) / TS).toInt()

        var canMove = true
        for (tileY in tileYBottom..tileYTop) {
            if (xMove > 0) {
                if (map.getTile(tileXRight, tileY).isSolid) { canMove = false; break }
            } else {
                if (map.getTile(tileXLeft, tileY).isSolid) { canMove = false; break }
            }
        }
        if (canMove) x = newX
    }

    private fun moveY() {
        if (yMove == 0f) return
        val newY = y + yMove
        val map = refLink.map ?: return
        val TS = TileConstants.TILE_SIZE

        val tileXLeft = (x / TS).toInt()
        val tileXRight = ((x + width - 1) / TS).toInt()
        val tileYBottom = ((newY) / TS).toInt()
        val tileYTop = ((newY + height - 1) / TS).toInt()

        var canMove = true
        for (tileX in tileXLeft..tileXRight) {
            if (yMove > 0) {
                if (map.getTile(tileX, tileYTop).isSolid) { canMove = false; break }
            } else {
                if (map.getTile(tileX, tileYBottom).isSolid) { canMove = false; break }
            }
        }
        if (canMove) y = newY
    }

    private fun updateAnimations() {
        stateTime += Gdx.graphics.deltaTime

        // Selectăm animația potrivită
        val nextAnimation = when {
            isHurt -> Assets.playerHurt ?: createDefaultAnimation()
            isAttacking -> when (direction) {
                Direction.UP -> Assets.playerHalfslashUp ?: Assets.playerIdleUp
                Direction.DOWN -> Assets.playerHalfslashDown ?: Assets.playerIdleDown
                Direction.LEFT -> Assets.playerHalfslashLeft ?: Assets.playerIdleLeft
                Direction.RIGHT -> Assets.playerHalfslashRight ?: Assets.playerIdleRight
            } ?: createDefaultAnimation()

            isJumping -> when (direction) {
                Direction.UP -> Assets.playerJumpUp ?: Assets.playerIdleUp
                Direction.DOWN -> Assets.playerJumpDown ?: Assets.playerIdleDown
                Direction.LEFT -> Assets.playerJumpLeft ?: Assets.playerIdleLeft
                Direction.RIGHT -> Assets.playerJumpRight ?: Assets.playerIdleRight
            } ?: createDefaultAnimation()

            xMove != 0f || yMove != 0f -> {
                if (currentSpeed > normalSpeed) {
                    when (direction) {
                        Direction.UP -> Assets.playerRunUp
                        Direction.DOWN -> Assets.playerRunDown
                        Direction.LEFT -> Assets.playerRunLeft
                        Direction.RIGHT -> Assets.playerRunRight
                    } ?: createDefaultAnimation()
                } else {
                    when (direction) {
                        Direction.UP -> Assets.playerWalkUp
                        Direction.DOWN -> Assets.playerWalkDown
                        Direction.LEFT -> Assets.playerWalkLeft
                        Direction.RIGHT -> Assets.playerWalkRight
                    } ?: createDefaultAnimation()
                }
            }
            else -> {
                // IDLE: Folosim direcția curentă pentru a rămâne cu fața unde trebuie
                when (direction) {
                    Direction.UP -> Assets.playerIdleUp
                    Direction.DOWN -> Assets.playerIdleDown
                    Direction.LEFT -> Assets.playerIdleLeft
                    Direction.RIGHT -> Assets.playerIdleRight
                } ?: createDefaultAnimation()
            }
        }

        // ✅ FIX: Resetăm stateTime dacă s-a schimbat animația (ex: Run -> Idle)
        // Asta previne ca jucătorul să arate "înghețat" într-o poziție de mers
        if (nextAnimation != previousAnimation) {
            stateTime = 0f
            previousAnimation = nextAnimation
        }

        currentAnimation = nextAnimation
        currentFrame = currentAnimation.getKeyFrame(stateTime, true)
    }

    fun takeDamage(damage: Int) {
        if (isHurt) return
        health = (health - damage).coerceAtLeast(0)
        if (health <= 0) {
            isHurt = true
            stateTime = 0f
        }
    }

    fun heal(amount: Int) {
        health = (health + amount).coerceAtMost(maxHealth)
    }

    fun resetHealth() {
        health = maxHealth
        isHurt = false
    }

    override fun setPosition(newX: Float, newY: Float) {
        super.setPosition(newX, newY)
        updateBounds()
    }

    private fun updateDirection() {
        if (xMove != 0f || yMove != 0f) {
            direction = when {
                abs(xMove) > abs(yMove) -> if (facingRight) Direction.RIGHT else Direction.LEFT
                yMove > 0 -> Direction.UP
                yMove < 0 -> Direction.DOWN
                else -> direction
            }
        }
    }

    fun getMaxHealth(): Int = maxHealth
    fun isPlayerHurt(): Boolean = isHurt
    fun getFacingDirection(): Direction = direction

    override fun render(batch: SpriteBatch) {
        batch.draw(currentFrame, x, y, width.toFloat(), height.toFloat())
    }
}
