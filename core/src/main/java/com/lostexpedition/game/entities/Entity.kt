package com.lostexpedition.game.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.utils.RefLinks

/**
 * Entity - Base class for all game entities
 *
 * Provides common functionality for all entities including:
 * - Position and bounds management
 * - Health system
 * - Interaction popup drawing (like Java version)
 * - Screen visibility checking
 *
 * @param refLink Reference to game utilities
 * @param x Initial X position
 * @param y Initial Y position
 * @param width Entity width in pixels
 * @param height Entity height in pixels
 * @author LostExpedition Team
 */
abstract class Entity(
    protected val refLink: RefLinks,
    var x: Float,
    var y: Float,
    val width: Int,
    val height: Int
) {
    /** Entity health - can be overridden by subclasses */
    open var health: Int = 100

    /** Whether this entity has been collected (for collectibles) */
    open val isCollected: Boolean = false

    /** Font for interaction popups (lazy initialized) */
    private var popupFont: BitmapFont? = null

    /** Layout helper for text measurement */
    private var glyphLayout: GlyphLayout? = null

    /**
     * Updates the entity state. Must be implemented by subclasses.
     */
    abstract fun update()

    /**
     * Renders the entity. Must be implemented by subclasses.
     * @param batch The SpriteBatch to render with
     */
    abstract fun render(batch: SpriteBatch)

    /**
     * Disposes entity resources. Override if entity has resources to free.
     */
    open fun dispose() {
        popupFont?.dispose()
    }

    /** Bounds property for collision detection */
    val bounds: Bounds
        get() = Bounds(x, y, width.toFloat(), height.toFloat())

    /**
     * Sets the entity position
     * @param newX New X coordinate
     * @param newY New Y coordinate
     */
    open fun setPosition(newX: Float, newY: Float) {
        this.x = newX
        this.y = newY
    }

    /**
     * Updates the entity bounds (useful after position changes)
     */
    open fun updateBounds() {
        // Bounds are calculated dynamically via the property getter
        // This method can be overridden for custom behavior
    }

    /**
     * Checks if entity is visible on screen
     * @return True if any part of entity is visible
     */
    fun isOnScreen(): Boolean {
        val camera = refLink.gameCamera
        val camX = camera.position.x - camera.viewportWidth / 2
        val camY = camera.position.y - camera.viewportHeight / 2
        val camWidth = camera.viewportWidth
        val camHeight = camera.viewportHeight

        return x + width > camX &&
            x < camX + camWidth &&
            y + height > camY &&
            y < camY + camHeight
    }

    /**
     * Converts bounds to LibGDX Rectangle
     * @return LibGDX Rectangle representation of bounds
     */
    fun toRectangle(): Rectangle {
        return Rectangle(x, y, width.toFloat(), height.toFloat())
    }

    /**
     * Draws an interaction popup above the entity (like Java version)
     *
     * @param batch The SpriteBatch to render with
     * @param text The text to display
     * @param backgroundColor Background color for the popup
     * @param textColor Text color
     */
    fun drawInteractionPopup(
        batch: SpriteBatch,
        text: String,
        backgroundColor: Color = Color(0f, 0f, 0f, 0.7f),
        textColor: Color = Color.WHITE
    ) {
        // Initialize font if needed
        if (popupFont == null) {
            popupFont = BitmapFont()
            popupFont?.color = textColor
            glyphLayout = GlyphLayout()
        }

        val font = popupFont ?: return
        val layout = glyphLayout ?: return

        // Calculate text dimensions
        layout.setText(font, text)
        val textWidth = layout.width
        val textHeight = layout.height

        // Popup position (centered above entity)
        val padding = 8f
        val popupWidth = textWidth + padding * 2
        val popupHeight = textHeight + padding * 2
        val popupX = x + (width - popupWidth) / 2
        val popupY = y + height + 10f

        // We need to end the batch to draw shapes
        batch.end()

        // Draw background using ShapeRenderer
        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = backgroundColor
        shapeRenderer.rect(popupX, popupY, popupWidth, popupHeight)
        shapeRenderer.end()

        // Draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(popupX, popupY, popupWidth, popupHeight)
        shapeRenderer.end()
        shapeRenderer.dispose()

        // Resume batch and draw text
        batch.begin()
        font.color = textColor
        font.draw(batch, text, popupX + padding, popupY + popupHeight - padding)
    }

    /**
     * Draws a health bar above the entity
     *
     * @param batch The SpriteBatch to render with
     * @param currentHealth Current health value
     * @param maxHealth Maximum health value
     * @param barWidth Width of the health bar
     * @param barHeight Height of the health bar
     */
    fun drawHealthBar(
        batch: SpriteBatch,
        currentHealth: Int,
        maxHealth: Int,
        barWidth: Float = width.toFloat(),
        barHeight: Float = 6f
    ) {
        if (currentHealth <= 0 || maxHealth <= 0) return

        val healthPercent = currentHealth.toFloat() / maxHealth.toFloat()
        val barX = x
        val barY = y + height + 4f

        batch.end()

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix

        // Background (red)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(barX, barY, barWidth, barHeight)
        shapeRenderer.end()

        // Health (green to yellow to red based on percentage)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        val healthColor = when {
            healthPercent > 0.6f -> Color.GREEN
            healthPercent > 0.3f -> Color.YELLOW
            else -> Color.RED
        }
        shapeRenderer.color = healthColor
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight)
        shapeRenderer.end()

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(barX, barY, barWidth, barHeight)
        shapeRenderer.end()

        shapeRenderer.dispose()
        batch.begin()
    }

    /**
     * Bounds data class for collision detection
     */
    data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float) {
        /**
         * Checks if this bounds overlaps with another
         * @param other The other Bounds to check
         * @return True if bounds overlap
         */
        fun overlaps(other: Bounds): Boolean {
            return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y
        }

        /**
         * Checks if this bounds overlaps with a LibGDX Rectangle
         * @param rect The Rectangle to check
         * @return True if bounds overlap
         */
        fun overlaps(rect: com.badlogic.gdx.math.Rectangle): Boolean {
            return x < rect.x + rect.width &&
                x + width > rect.x &&
                y < rect.y + rect.height &&
                y + height > rect.y
        }

        /**
         * Converts to LibGDX Rectangle
         * @return LibGDX Rectangle representation
         */
        fun toRectangle(): Rectangle {
            return Rectangle(x, y, width, height)
        }
    }
}
