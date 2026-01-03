package com.lostexpedition.game.input

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class TouchController(screenWidth: Int, screenHeight: Int) {

    // Joystick stânga-jos (fix)
    private val joystick = VirtualJoystick(200f, 200f, 120f, 60f)

    // Butoane dreapta-jos (calculate dinamic în funcție de ecran)
    // Attack (Roșu) - mai mare
    private val attackButton = VirtualButton(screenWidth - 250f, 200f, 90f)
    // Interact/Jump (Albastru) - mai mic, lângă attack
    private val interactButton = VirtualButton(screenWidth - 450f, 150f, 70f)

    private val shapeRenderer = ShapeRenderer()

    var isJoystickActive = false
        private set
    var joystickDeltaX = 0f
        private set
    var joystickDeltaY = 0f
        private set
    var isAttackPressed = false
        private set
    var isInteractPressed = false
        private set

    // Helpers pentru TestScreen
    fun getMoveDirection(): Vector2 {
        return Vector2(joystickDeltaX, joystickDeltaY)
    }

    // Mapăm Jump pe butonul de Interact pentru acest exemplu (sau poți adăuga un buton separat)
    fun isJumpPressed(): Boolean = isInteractPressed

    fun update() {
        if (joystick.isActive) {
            val dir = joystick.getDirection()
            joystickDeltaX = dir.x
            joystickDeltaY = dir.y
            isJoystickActive = true
        } else {
            joystickDeltaX = 0f
            joystickDeltaY = 0f
            isJoystickActive = false
        }

        isAttackPressed = attackButton.isPressed
        isInteractPressed = interactButton.isPressed
    }

    fun touchDown(screenX: Float, screenY: Float, pointer: Int): Boolean {
        var handled = false

        // Verificăm joystick
        if (joystick.handleTouchDown(screenX, screenY, pointer)) {
            isJoystickActive = true
            update()
            handled = true
        }

        // Verificăm butoane
        if (attackButton.handleTouchDown(screenX, screenY, pointer)) {
            isAttackPressed = true
            handled = true
        }

        if (interactButton.handleTouchDown(screenX, screenY, pointer)) {
            isInteractPressed = true
            handled = true
        }

        return handled
    }

    fun touchUp(screenX: Float, screenY: Float, pointer: Int): Boolean {
        var handled = false

        if (joystick.handleTouchUp(pointer)) {
            isJoystickActive = false
            joystickDeltaX = 0f
            joystickDeltaY = 0f
            update()
            handled = true
        }

        if (attackButton.handleTouchUp(screenX, screenY, pointer)) {
            isAttackPressed = false
            handled = true
        }

        if (interactButton.handleTouchUp(screenX, screenY, pointer)) {
            isInteractPressed = false
            handled = true
        }

        return handled
    }

    fun touchDragged(screenX: Float, screenY: Float, pointer: Int): Boolean {
        if (joystick.handleTouchDragged(screenX, screenY, pointer)) {
            update()
            return true
        }
        return false
    }

    fun draw() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Desenare Joystick
        joystick.draw(shapeRenderer)

        // Desenare Butoane
        attackButton.draw(shapeRenderer, Color.RED)
        interactButton.draw(shapeRenderer, Color.BLUE)

        shapeRenderer.end()
    }

    fun dispose() {
        shapeRenderer.dispose()
    }

    // --- Clase interne ---

    private class VirtualJoystick(
        private val x: Float,
        private val y: Float,
        private val outerRadius: Float,
        private val innerRadius: Float
    ) {
        var isActive = false
            private set
        private var touchX = x
        private var touchY = y
        private var activePointer = -1

        fun getDirection(): Direction {
            if (!isActive) return Direction(0f, 0f)
            val dx = touchX - x
            val dy = touchY - y
            // Normalizăm vectorul
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            return if (distance > 0) {
                // Putem ajusta sensibilitatea aici
                Direction(dx / outerRadius, dy / outerRadius)
            } else {
                Direction(0f, 0f)
            }
        }

        fun handleTouchDown(screenX: Float, screenY: Float, pointer: Int): Boolean {
            val dx = screenX - x
            val dy = screenY - y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)

            if (distance <= outerRadius * 1.5f) { // Zonă de atingere puțin mai mare
                isActive = true
                activePointer = pointer
                touchX = screenX
                touchY = screenY
                // Clamp stick inside circle
                if (distance > outerRadius) {
                    val angle = kotlin.math.atan2(dy, dx)
                    touchX = x + kotlin.math.cos(angle) * outerRadius
                    touchY = y + kotlin.math.sin(angle) * outerRadius
                }
                return true
            }
            return false
        }

        fun handleTouchUp(pointer: Int): Boolean {
            if (pointer == activePointer) {
                isActive = false
                activePointer = -1
                touchX = x
                touchY = y
                return true
            }
            return false
        }

        fun handleTouchDragged(screenX: Float, screenY: Float, pointer: Int): Boolean {
            if (pointer == activePointer && isActive) {
                val dx = screenX - x
                val dy = screenY - y
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                if (distance <= outerRadius) {
                    touchX = screenX
                    touchY = screenY
                } else {
                    val angle = kotlin.math.atan2(dy, dx)
                    touchX = x + kotlin.math.cos(angle) * outerRadius
                    touchY = y + kotlin.math.sin(angle) * outerRadius
                }
                return true
            }
            return false
        }

        fun draw(renderer: ShapeRenderer) {
            renderer.color = Color(1f, 1f, 1f, 0.3f)
            renderer.circle(x, y, outerRadius)
            renderer.color = Color(1f, 1f, 1f, 0.6f)
            renderer.circle(touchX, touchY, innerRadius)
        }

        data class Direction(val x: Float, val y: Float)
    }

    private class VirtualButton(
        private val x: Float,
        private val y: Float,
        private val radius: Float
    ) {
        var isPressed = false
            private set
        private var activePointer = -1

        fun handleTouchDown(screenX: Float, screenY: Float, pointer: Int): Boolean {
            val dx = screenX - x
            val dy = screenY - y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)

            if (distance <= radius * 1.2f) {
                isPressed = true
                activePointer = pointer
                return true
            }
            return false
        }

        fun handleTouchUp(screenX: Float, screenY: Float, pointer: Int): Boolean {
            if (pointer == activePointer) {
                isPressed = false
                activePointer = -1
                return true
            }
            return false
        }

        fun draw(renderer: ShapeRenderer, color: Color) {
            renderer.color = if (isPressed) {
                Color(color.r, color.g, color.b, 0.8f)
            } else {
                Color(color.r, color.g, color.b, 0.4f)
            }
            renderer.circle(x, y, radius)
        }
    }
}
