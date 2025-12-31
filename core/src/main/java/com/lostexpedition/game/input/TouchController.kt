package com.lostexpedition.game.input

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.sqrt

/**
 * Represents a single touch button on screen
 */
class TouchButton(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val label: String
) {

    var isPressed: Boolean = false
        private set
    private var pointerId: Int = -1

    fun contains(touchX: Float, touchY: Float): Boolean {
        return touchX >= x && touchX <= x + width &&
            touchY >= y && touchY <= y + height
    }

    fun handleTouchDown(touchX: Float, touchY: Float, pointer: Int): Boolean {
        if (contains(touchX, touchY)) {
            isPressed = true
            pointerId = pointer
            return true
        }
        return false
    }

    fun handleTouchUp(pointer: Int): Boolean {
        if (pointerId == pointer) {
            isPressed = false
            pointerId = -1
            return true
        }
        return false
    }

    fun draw(shapeRenderer: ShapeRenderer) {
        // Draw button background
        shapeRenderer.color = if (isPressed) Color.GRAY else Color.DARK_GRAY
        shapeRenderer.rect(x, y, width, height)

        // Draw button border
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(x, y, width, height)
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }
}

/**
 * Virtual joystick for movement control
 */
class VirtualJoystick(
    val centerX: Float,
    val centerY: Float,
    val outerRadius: Float,
    val innerRadius: Float
) {
    private var knobX: Float = centerX
    private var knobY: Float = centerY
    var isActive: Boolean = false
        private set
    private var pointerId: Int = -1

    fun handleTouchDown(touchX: Float, touchY: Float, pointer: Int): Boolean {
        val distance = getDistance(touchX, touchY, centerX, centerY)
        if (distance <= outerRadius) {
            isActive = true
            pointerId = pointer
            updateKnobPosition(touchX, touchY)
            return true
        }
        return false
    }

    fun handleTouchDragged(touchX: Float, touchY: Float, pointer: Int): Boolean {
        if (isActive && pointerId == pointer) {
            updateKnobPosition(touchX, touchY)
            return true
        }
        return false
    }

    fun handleTouchUp(pointer: Int): Boolean {
        if (pointerId == pointer) {
            reset()
            return true
        }
        return false
    }

    private fun updateKnobPosition(touchX: Float, touchY: Float) {
        val dx = touchX - centerX
        val dy = touchY - centerY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance <= outerRadius) {
            knobX = touchX
            knobY = touchY
        } else {
            // Constrain knob to outer circle
            val angle = Math.atan2(dy.toDouble(), dx.toDouble())
            knobX = centerX + (outerRadius * Math.cos(angle)).toFloat()
            knobY = centerY + (outerRadius * Math.sin(angle)).toFloat()
        }
    }

    fun getDirection(): Vector2 {
        if (!isActive) return Vector2.Zero

        val dx = knobX - centerX
        val dy = knobY - centerY
        val distance = sqrt(dx * dx + dy * dy)

        return if (distance > 10f) { // Dead zone
            Vector2(dx / outerRadius, dy / outerRadius)
        } else {
            Vector2.Zero
        }
    }

    fun reset() {
        knobX = centerX
        knobY = centerY
        isActive = false
        pointerId = -1
    }

    fun draw(shapeRenderer: ShapeRenderer) {
        // Draw outer circle
        shapeRenderer.color = Color(0.3f, 0.3f, 0.3f, 0.5f)
        shapeRenderer.circle(centerX, centerY, outerRadius)

        // Draw outer circle border
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(centerX, centerY, outerRadius)
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw inner knob
        shapeRenderer.color = if (isActive) Color.LIGHT_GRAY else Color.GRAY
        shapeRenderer.circle(knobX, knobY, innerRadius)

        // Draw knob border
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(knobX, knobY, innerRadius)
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    }

    private fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
}

/**
 * Main touch controller that manages all touch inputs for mobile
 */
class TouchController(private val screenWidth: Int, private val screenHeight: Int) {
    private val shapeRenderer = ShapeRenderer()

    // Joystick for movement (left side)
    private val joystick: VirtualJoystick

    // Action buttons (right side)
    val jumpButton: TouchButton      // Space
    val attackButton: TouchButton    // K
    val interactButton: TouchButton  // E

    init {
        // Joystick setup (bottom-left)
        val joystickSize = 120f
        val joystickPadding = 50f
        joystick = VirtualJoystick(
            centerX = joystickPadding + joystickSize,
            centerY = joystickPadding + joystickSize,
            outerRadius = joystickSize,
            innerRadius = joystickSize * 0.4f
        )

        // Button dimensions
        val buttonSize = 80f
        val buttonSpacing = 20f
        val rightPadding = 50f

        // Action buttons setup (bottom-right, stacked vertically)
        val buttonX = screenWidth - rightPadding - buttonSize

        jumpButton = TouchButton(
            x = buttonX,
            y = joystickPadding,
            width = buttonSize,
            height = buttonSize,
            label = "JUMP"
        )

        attackButton = TouchButton(
            x = buttonX,
            y = jumpButton.y + buttonSize + buttonSpacing,
            width = buttonSize,
            height = buttonSize,
            label = "ATTACK"
        )

        interactButton = TouchButton(
            x = buttonX,
            y = attackButton.y + buttonSize + buttonSpacing,
            width = buttonSize,
            height = buttonSize,
            label = "INTERACT"
        )
    }

    /**
     * Handle touch down events
     */
    fun touchDown(screenX: Float, screenY: Float, pointer: Int): Boolean {
        // Convert screen coordinates (Y is flipped in LibGDX)
        val touchY = screenHeight - screenY

        // Check joystick first
        if (joystick.handleTouchDown(screenX, touchY, pointer)) return true

        // Check buttons
        if (jumpButton.handleTouchDown(screenX, touchY, pointer)) return true
        if (attackButton.handleTouchDown(screenX, touchY, pointer)) return true
        if (interactButton.handleTouchDown(screenX, touchY, pointer)) return true

        return false
    }

    /**
     * Handle touch dragged events (for joystick)
     */
    fun touchDragged(screenX: Float, screenY: Float, pointer: Int): Boolean {
        val touchY = screenHeight - screenY
        return joystick.handleTouchDragged(screenX, touchY, pointer)
    }

    /**
     * Handle touch up events
     */
    fun touchUp(screenX: Float, screenY: Float, pointer: Int): Boolean {
        joystick.handleTouchUp(pointer)
        jumpButton.handleTouchUp(pointer)
        attackButton.handleTouchUp(pointer)
        interactButton.handleTouchUp(pointer)
        return true
    }

    /**
     * Get movement direction from joystick
     * Returns normalized Vector2 (x: -1 to 1, y: -1 to 1)
     */
    fun getMoveDirection(): Vector2 {
        return joystick.getDirection()
    }

    /**
     * Check if jump button is pressed
     */
    fun isJumpPressed(): Boolean = jumpButton.isPressed

    /**
     * Check if attack button is pressed
     */
    fun isAttackPressed(): Boolean = attackButton.isPressed

    /**
     * Check if interact button is pressed
     */
    fun isInteractPressed(): Boolean = interactButton.isPressed

    /**
     * Draw all touch controls on screen
     */
    fun draw() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw joystick
        joystick.draw(shapeRenderer)

        // Draw buttons
        jumpButton.draw(shapeRenderer)
        attackButton.draw(shapeRenderer)
        interactButton.draw(shapeRenderer)

        shapeRenderer.end()
    }

    /**
     * Dispose of resources
     */
    fun dispose() {
        shapeRenderer.dispose()
    }
    var isJoystickActive = false  // nu private
    var joystickDeltaX = 0f
    var joystickDeltaY = 0f
    var runButtonPressed = false
    var attackButtonPressed = false
    var jumpButtonPressed = false

}
