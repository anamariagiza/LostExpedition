package com.lostexpedition.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2

class TouchController(private var screenWidth: Int, private var screenHeight: Int) : InputProcessor {

    // Dimensiunile sunt raportate la INALTIMEA ecranului, ca sa arate la fel
    // indiferent de aspect ratio (pe ecran lat nu mai ies controale uriase).
    private val h = screenHeight.toFloat()

    // Joystick stanga-jos
    private val joystick = VirtualJoystick(
        h * 0.24f, h * 0.24f,
        h * 0.105f, h * 0.048f
    )

    // Butoane dreapta-jos
    private val attackButton = VirtualButton(screenWidth - h * 0.34f, h * 0.27f, h * 0.062f)
    private val interactButton = VirtualButton(screenWidth - h * 0.16f, h * 0.18f, h * 0.062f)

    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val uiMatrix = Matrix4()

    // Texturile controalelor (cu fallback la cercuri simple daca lipsesc)
    private val joystickBaseTex = loadTexture("textures/ui_controls/joystick_base.png")
    private val joystickKnobTex = loadTexture("textures/ui_controls/joystick_knob.png")
    private val attackTex = loadTexture("textures/ui_controls/btn_attack.png")
    private val interactTex = loadTexture("textures/ui_controls/btn_interact.png")

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

    private fun loadTexture(path: String): Texture? {
        return try {
            Texture(Gdx.files.internal(path)).apply {
                setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
        } catch (e: Exception) {
            Gdx.app.error("TouchController", "Nu am putut incarca $path, folosesc fallback.")
            null
        }
    }

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

        isAttackJustPressed = isAttackPressed && !wasAttackPressed
        isInteractJustPressed = isInteractPressed && !wasInteractPressed

        wasAttackPressed = isAttackPressed
        wasInteractPressed = isInteractPressed
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
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

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return touchUp(screenX, screenY, pointer, button)
    }

    override fun keyDown(keycode: Int): Boolean = false
    override fun keyUp(keycode: Int): Boolean = false
    override fun keyTyped(character: Char): Boolean = false
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean = false
    override fun scrolled(amountX: Float, amountY: Float): Boolean = false

    fun draw() {
        uiMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        val hasTextures = joystickBaseTex != null && joystickKnobTex != null &&
                attackTex != null && interactTex != null

        if (hasTextures) {
            batch.projectionMatrix = uiMatrix
            batch.begin()

            // Joystick: baza semi-transparenta, mai opaca atunci cand e folosit
            batch.setColor(1f, 1f, 1f, if (joystick.isActive) 0.85f else 0.55f)
            batch.draw(
                joystickBaseTex,
                joystick.x - joystick.outerRadius, joystick.y - joystick.outerRadius,
                joystick.outerRadius * 2f, joystick.outerRadius * 2f
            )
            batch.setColor(1f, 1f, 1f, if (joystick.isActive) 0.95f else 0.65f)
            batch.draw(
                joystickKnobTex,
                joystick.touchX - joystick.innerRadius, joystick.touchY - joystick.innerRadius,
                joystick.innerRadius * 2f, joystick.innerRadius * 2f
            )

            // Butoane
            batch.setColor(1f, 1f, 1f, if (attackButton.isPressed) 0.95f else 0.55f)
            batch.draw(
                attackTex,
                attackButton.x - attackButton.radius, attackButton.y - attackButton.radius,
                attackButton.radius * 2f, attackButton.radius * 2f
            )
            batch.setColor(1f, 1f, 1f, if (interactButton.isPressed) 0.95f else 0.55f)
            batch.draw(
                interactTex,
                interactButton.x - interactButton.radius, interactButton.y - interactButton.radius,
                interactButton.radius * 2f, interactButton.radius * 2f
            )

            batch.setColor(1f, 1f, 1f, 1f)
            batch.end()
        } else {
            // Fallback: cercuri simple (comportamentul vechi)
            shapeRenderer.projectionMatrix = uiMatrix
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            joystick.draw(shapeRenderer)
            attackButton.draw(shapeRenderer, Color.RED)
            interactButton.draw(shapeRenderer, Color.BLUE)
            shapeRenderer.end()
        }
    }

    fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        joystickBaseTex?.dispose()
        joystickKnobTex?.dispose()
        attackTex?.dispose()
        interactTex?.dispose()
    }

    private class VirtualJoystick(
        val x: Float,
        val y: Float,
        val outerRadius: Float,
        val innerRadius: Float
    ) {
        var isActive = false
            private set
        var touchX = x
            private set
        var touchY = y
            private set
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

    private class VirtualButton(val x: Float, val y: Float, val radius: Float) {
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
