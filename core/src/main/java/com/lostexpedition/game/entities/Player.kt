package com.lostexpedition.game.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.utils.RefLinks
import kotlin.math.abs

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
    private var currentFrame: TextureRegion

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
        // ✅ FIX: Update TouchController FIRST
        refLink.touchController?.update()

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

        // ✅ Android touch controls
        if (touchController != null) {
            if (touchController.isJoystickActive) {
                val joyX = touchController.joystickDeltaX
                val joyY = touchController.joystickDeltaY

                xMove = joyX * currentSpeed
                yMove = joyY * currentSpeed

                if (kotlin.math.abs(joyX).compareTo(0.1f) > 0) {
                    facingRight = joyX > 0
                }
                if (kotlin.math.abs(joyY).compareTo(0.1f) > 0) {
                    facingDown = joyY < 0
                }
            }

            // ✅ FIX: Attack button - folosește isAttackPressed
            if (touchController.isAttackPressed && !isAttacking) {
                performAttack()
            }

            // ✅ FIX: Interact button poate fi folosit pentru jump (opțional)
            // Decomentează dacă vrei jump cu interact button:
            // if (touchController.isInteractPressed && !isJumping) {
            //     performJump()
            // }
        }

        // ✅ Keyboard controls (pentru desktop testing)
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

        // Run cu SHIFT (doar pentru keyboard)
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            currentSpeed = runSpeed
            xMove = if (xMove != 0f) (xMove / normalSpeed) * runSpeed else 0f
            yMove = if (yMove != 0f) (yMove / normalSpeed) * runSpeed else 0f
        }

        // Attack cu SPACE (doar pentru keyboard)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isAttacking) {
            performAttack()
        }
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
                // ✅ FIX: Convertim bounds la Rectangle pentru comparație
                val entityRect = entity.bounds.toRectangle()

                if (entity is Agent && attackBounds.overlaps(entityRect)) {
                    entity.takeDamage(attackDamage)
                }
                if (entity is Animal && attackBounds.overlaps(entityRect)) {
                    entity.takeDamage(attackDamage)
                }
            }
        }
    }

    private fun performJump() {
        isJumping = true
        jumpTime = 0f
    }

    private fun move() {
        moveX()
        moveY()
    }

    private fun moveX() {
        if (xMove == 0f) return

        val newX = x + xMove
        val map = refLink.map ?: return

        val tileXLeft = ((newX) / Tile.TILE_WIDTH).toInt()
        val tileXRight = ((newX + width - 1) / Tile.TILE_WIDTH).toInt()
        val tileYBottom = (y / Tile.TILE_HEIGHT).toInt()
        val tileYTop = ((y + height - 1) / Tile.TILE_HEIGHT).toInt()

        var canMove = true
        for (tileY in tileYBottom..tileYTop) {
            if (xMove > 0) {
                if (map.getTile(tileXRight, tileY).isSolid) {
                    canMove = false
                    break
                }
            } else {
                if (map.getTile(tileXLeft, tileY).isSolid) {
                    canMove = false
                    break
                }
            }
        }

        if (canMove) {
            x = newX
        }
    }

    private fun moveY() {
        if (yMove == 0f) return

        val newY = y + yMove
        val map = refLink.map ?: return

        val tileXLeft = (x / Tile.TILE_WIDTH).toInt()
        val tileXRight = ((x + width - 1) / Tile.TILE_WIDTH).toInt()
        val tileYBottom = ((newY) / Tile.TILE_HEIGHT).toInt()
        val tileYTop = ((newY + height - 1) / Tile.TILE_HEIGHT).toInt()

        var canMove = true
        for (tileX in tileXLeft..tileXRight) {
            if (yMove > 0) {
                if (map.getTile(tileX, tileYTop).isSolid) {
                    canMove = false
                    break
                }
            } else {
                if (map.getTile(tileX, tileYBottom).isSolid) {
                    canMove = false
                    break
                }
            }
        }

        if (canMove) {
            y = newY
        }
    }

    private fun updateAnimations() {
        stateTime += Gdx.graphics.deltaTime

        currentAnimation = when {
            isHurt -> Assets.playerHurt ?: createDefaultAnimation()
            isAttacking -> {
                if (facingRight)
                    Assets.playerAttackRight ?: Assets.playerIdleRight ?: createDefaultAnimation()
                else
                    Assets.playerAttackLeft ?: Assets.playerIdleLeft ?: createDefaultAnimation()
            }
            isJumping -> {
                if (facingRight)
                    Assets.playerJumpRight ?: Assets.playerIdleRight ?: createDefaultAnimation()
                else
                    Assets.playerJumpLeft ?: Assets.playerIdleLeft ?: createDefaultAnimation()
            }
            xMove != 0f || yMove != 0f -> {
                if (currentSpeed > normalSpeed) {
                    when {
                        abs(xMove) > abs(yMove) -> {
                            if (facingRight)
                                Assets.playerRunRight ?: Assets.playerIdleRight ?: createDefaultAnimation()
                            else
                                Assets.playerRunLeft ?: Assets.playerIdleLeft ?: createDefaultAnimation()
                        }
                        yMove > 0 -> Assets.playerRunUp ?: Assets.playerIdleUp ?: createDefaultAnimation()
                        else -> Assets.playerRunDown ?: Assets.playerIdleDown ?: createDefaultAnimation()
                    }
                } else {
                    when {
                        abs(xMove) > abs(yMove) -> {
                            if (facingRight)
                                Assets.playerWalkRight ?: Assets.playerIdleRight ?: createDefaultAnimation()
                            else
                                Assets.playerWalkLeft ?: Assets.playerIdleLeft ?: createDefaultAnimation()
                        }
                        yMove > 0 -> Assets.playerWalkUp ?: Assets.playerIdleUp ?: createDefaultAnimation()
                        else -> Assets.playerWalkDown ?: Assets.playerIdleDown ?: createDefaultAnimation()
                    }
                }
            }
            else -> {
                when {
                    !facingDown -> Assets.playerIdleUp ?: createDefaultAnimation()
                    facingRight -> Assets.playerIdleRight ?: createDefaultAnimation()
                    else -> Assets.playerIdleLeft ?: createDefaultAnimation()
                }
            }
        }

        currentFrame = currentAnimation.getKeyFrame(stateTime, true)
    }

    fun takeDamage(damage: Int) {
        if (isHurt) return

        health -= damage
        health = health.coerceAtLeast(0)

        if (health <= 0) {
            isHurt = true
            stateTime = 0f
        }
    }

    fun heal(amount: Int) {
        health += amount
        health = health.coerceAtMost(maxHealth)
    }

    fun resetHealth() {
        health = maxHealth
        isHurt = false
    }

    // ✅ FIX: Poate folosi setPosition din Entity (care acum e open)
    // SAU poate override dacă ai logică extra:
    override fun setPosition(newX: Float, newY: Float) {
        super.setPosition(newX, newY)
        // Aici poți adăuga logică extra dacă e nevoie
    }

    fun getMaxHealth(): Int = maxHealth
    fun isPlayerHurt(): Boolean = isHurt

    override fun render(batch: SpriteBatch) {
        batch.draw(currentFrame, x, y, width.toFloat(), height.toFloat())
    }
}
