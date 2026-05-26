package com.lostexpedition.game.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.utils.RefLinks
import kotlin.math.abs

class Agent(
    refLink: RefLinks,
    startX: Float,
    startY: Float,
    private val leftBound: Float,
    private val rightBound: Float,
    private val isBoss: Boolean = false
) : Entity(refLink, startX, startY, 64, 64) {

    override var health: Int = if (isBoss) 150 else 75
    private val maxHealth = health

    private val normalSpeed = 1.5f
    private val chaseSpeed = 3f
    private var currentSpeed = normalSpeed
    private var velocityX = normalSpeed
    private var velocityY = 0f

    private enum class AIState {
        PATROL, CHASE, ATTACK
    }

    private var aiState = AIState.PATROL
    private var isChaseMode = false
    private var defeated = false              // NOU

    private enum class Direction {
        LEFT, RIGHT
    }

    private var direction = Direction.RIGHT

    private var stateTime = 0f
    private var currentAnimation: Animation<TextureRegion>
    private val walkAnimation: Animation<TextureRegion>
    private val attackAnimation: Animation<TextureRegion>

    private var attackCooldown = 0f
    private val attackCooldownDuration = 2f
    private val attackRange = 70f
    private val attackDamage = 25

    init {
        val defaultAnim = Assets.agentAnimation ?: Assets.enemyAnimation ?: createPlaceholderAnimation()
        walkAnimation = defaultAnim
        attackAnimation = defaultAnim
        currentAnimation = walkAnimation
    }

    private fun createPlaceholderAnimation(): Animation<TextureRegion> {
        val placeholderFrame = Assets.playerIdleDown?.getKeyFrame(0f) ?: TextureRegion()
        return Animation(0.1f, placeholderFrame)
    }

    fun setChaseMode(chase: Boolean) {
        isChaseMode = chase
        currentSpeed = if (chase) chaseSpeed else normalSpeed
    }

    fun isDefeated(): Boolean = defeated      // NOU

    override fun update() {
        if (defeated) return                  // NOU

        stateTime += Gdx.graphics.deltaTime

        when {
            isChaseMode -> updateChaseMode()
            else -> updatePatrolMode()
        }

        if (attackCooldown > 0) {
            attackCooldown -= Gdx.graphics.deltaTime
        }
    }

    private fun updatePatrolMode() {
        x += velocityX

        if (x <= leftBound || x >= rightBound) {
            velocityX = -velocityX
            direction = if (velocityX > 0) Direction.RIGHT else Direction.LEFT
        }

        currentAnimation = walkAnimation
    }

    private fun updateChaseMode() {
        val player = refLink.player ?: return

        val dx = player.x - x
        val dy = player.y - y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

        when {
            distance < attackRange -> {
                aiState = AIState.ATTACK
                performAttack()
            }
            distance < 300f -> {
                aiState = AIState.CHASE
                chasePlayer(dx, dy, distance)
            }
            else -> {
                aiState = AIState.PATROL
                updatePatrolMode()
            }
        }
    }

    private fun chasePlayer(dx: Float, dy: Float, distance: Float) {
        val directionX = dx / distance
        val directionY = dy / distance

        velocityX = directionX * chaseSpeed
        velocityY = directionY * chaseSpeed

        direction = if (velocityX > 0) Direction.RIGHT else Direction.LEFT

        val newX = x + velocityX
        val newY = y + velocityY

        if (!isColliding(newX, y)) x = newX
        if (!isColliding(x, newY)) y = newY

        currentAnimation = walkAnimation
    }

    private fun isColliding(testX: Float, testY: Float): Boolean {
        val map = refLink.map ?: return false

        val tileXLeft = (testX / Tile.TILE_WIDTH).toInt()
        val tileXRight = ((testX + width - 1) / Tile.TILE_WIDTH).toInt()
        val tileYBottom = (testY / Tile.TILE_HEIGHT).toInt()
        val tileYTop = ((testY + height - 1) / Tile.TILE_HEIGHT).toInt()

        for (tileY in tileYBottom..tileYTop) {
            for (tileX in tileXLeft..tileXRight) {
                if (map.getTile(tileX, tileY).isSolid) return true
            }
        }

        val gameState = refLink.gameState
        if (gameState != null) {
            for (entity in gameState.getEntities()) {
                if (entity === this) continue
                val testBounds = com.badlogic.gdx.math.Rectangle(testX, testY, width.toFloat(), height.toFloat())
                if (testBounds.overlaps(entity.bounds.toRectangle())) return true
            }
        }

        return false
    }

    private fun performAttack() {
        if (attackCooldown > 0) return

        currentAnimation = attackAnimation
        attackCooldown = attackCooldownDuration

        val player = refLink.player ?: return

        val attackBounds = com.badlogic.gdx.math.Rectangle(
            if (direction == Direction.RIGHT) x + width else x - attackRange,
            y, attackRange, height.toFloat()
        )

        if (attackBounds.overlaps(player.bounds.toRectangle())) {
            player.takeDamage(attackDamage)
        }
    }

    fun takeDamage(damage: Int) {
        if (defeated) return                  // NOU
        health -= damage
        health = health.coerceAtLeast(0)
        if (health <= 0) onDeath()
    }

    private fun onDeath() {
        defeated = true                       // NOU
        println("Agent defeated!")
    }

    fun getMaxHealth(): Int = maxHealth

    fun distanceToPlayer(): Float {
        val player = refLink.player ?: return Float.MAX_VALUE
        val dx = player.x - x
        val dy = player.y - y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    override fun render(batch: SpriteBatch) {
        if (defeated) return                  // NOU
        if (!isOnScreen()) return

        val currentFrame = currentAnimation.getKeyFrame(stateTime, true)
        val flipX = direction == Direction.LEFT

        batch.draw(
            currentFrame,
            if (flipX) x + width else x,
            y,
            if (flipX) -width.toFloat() else width.toFloat(),
            height.toFloat()
        )

        if (health < maxHealth) {
            drawHealthBar(batch, health, maxHealth)
        }
    }

    override fun dispose() {}
}
