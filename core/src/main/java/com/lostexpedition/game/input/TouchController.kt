package com.lostexpedition.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

/**
 * TouchController - Gestionează input-ul tactil pentru Android.
 * Implementează InputProcessor pentru a intercepta evenimentele de touch de la sistem.
 */
class TouchController(private var screenWidth: Int, private var screenHeight: Int) : InputProcessor {

    // Joystick stânga-jos (fix)
    private val joystick = VirtualJoystick(200f, 200f, 120f, 60f)

    // Butoane dreapta-jos (Attack și Interact)
    private val attackButton = VirtualButton(screenWidth - 250f, 200f, 90f)
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

    var isAttackJustPressed = false
        private set
    var isInteractJustPressed = false
        private set

    private var wasAttackPressed = false
    private var wasInteractPressed = false

    // Helpers pentru direcțiile de mișcare folosite în clasa Player
    fun isUpPressed(): Boolean = joystickDeltaY > 0.5f
    fun isDownPressed(): Boolean = joystickDeltaY < -0.5f
    fun isLeftPressed(): Boolean = joystickDeltaX < -0.5f
    fun isRightPressed(): Boolean = joystickDeltaX > 0.5f

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

        // Detecție pentru apăsare singulară (trigger)
        isAttackJustPressed = isAttackPressed && !wasAttackPressed
        isInteractJustPressed = isInteractPressed && !wasInteractPressed

        wasAttackPressed = isAttackPressed
        wasInteractPressed = isInteractPressed
    }

    // --- Implementare obligatorie InputProcessor ---

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // Conversie coordonate: Android are 0 la marginea de sus, ShapeRenderer are 0 jos
        val actualY = Gdx.graphics.height - screenY.toFloat()
        val actualX = screenX.toFloat()

        var handled = false
        if (joystick.handleTouchDown(actualX, actualY, pointer)) handled = true
        if (attackButton.handleTouchDown(actualX, actualY, pointer)) handled = true
        if (interactButton.handleTouchDown(actualX, actualY, pointer)) handled = true
        return handled
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val actualY = Gdx.graphics.height - screenY.toFloat()
        val actualX = screenX.toFloat()

        var handled = false
        if (joystick.handleTouchUp(pointer)) handled = true
        if (attackButton.handleTouchUp(actualX, actualY, pointer)) handled = true
        if (interactButton.handleTouchUp(actualX, actualY, pointer)) handled = true
        return handled
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val actualY = Gdx.graphics.height - screenY.toFloat()
        val actualX = screenX.toFloat()
        return joystick.handleTouchDragged(actualX, actualY, pointer)
    }

    // ✅ REZOLVARE EROARE: Metoda nouă adăugată în versiunile recente de libGDX
    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return touchUp(screenX, screenY, pointer, button)
    }

    // Metode de tastatură nefolosite pe Android
    override fun keyDown(keycode: Int): Boolean = false
    override fun keyUp(keycode: Int): Boolean = false
    override fun keyTyped(character: Char): Boolean = false
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean = false
    override fun scrolled(amountX: Float, amountY: Float): Boolean = false

    fun draw() {
        // Setăm proiecția pentru UI (ecran static)
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
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

        fun getDirection(): Vector2 {
            if (!isActive) return Vector2(0f, 0f)
            val dx = touchX - x
            val dy = touchY - y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            return if (distance > 0) {
                Vector2(dx / outerRadius, dy / outerRadius)
            } else {
                Vector2(0f, 0f)
            }
        }

        fun handleTouchDown(screenX: Float, screenY: Float, pointer: Int): Boolean {
            val dx = screenX - x
            val dy = screenY - y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)

            if (distance <= outerRadius * 1.5f) {
                isActive = true
                activePointer = pointer
                updatePosition(screenX, screenY)
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
                updatePosition(screenX, screenY)
                return true
            }
            return false
        }

        private fun updatePosition(screenX: Float, screenY: Float) {
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
        }

        fun draw(renderer: ShapeRenderer) {
            renderer.color = Color(1f, 1f, 1f, 0.3f)
            renderer.circle(x, y, outerRadius)
            renderer.color = Color(1f, 1f, 1f, 0.6f)
            renderer.circle(touchX, touchY, innerRadius)
        }
    }

    private class VirtualButton(private val x: Float, private val y: Float, private val radius: Float) {
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
