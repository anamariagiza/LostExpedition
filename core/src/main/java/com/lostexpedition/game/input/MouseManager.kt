package com.lostexpedition.game.input

/**
 * Manages and processes mouse input for the game.
 * This class captures all relevant mouse events such as button presses
 * and cursor movement. Provides "just clicked" logic to detect single clicks,
 * essential for interaction with menu buttons.
 */
class MouseManager {

    /** Flag indicating if left/right button is held down */
    private var leftPressed = false
    private var rightPressed = false

    /** Current cursor coordinates */
    private var mouseX = -1
    private var mouseY = -1

    /** Flags to manage "single click" logic */
    private var justLeftPressed = false
    private var cantLeftClick = false

    /**
     * Called by system when a mouse button is pressed
     */
    fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == 0) { // Left button (Input.Buttons.LEFT)
            leftPressed = true
        } else if (button == 1) { // Right button (Input.Buttons.RIGHT)
            rightPressed = true
        }
        return true
    }

    /**
     * Called by system when a mouse button is released
     */
    fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == 0) {
            leftPressed = false
        } else if (button == 1) {
            rightPressed = false
        }
        return true
    }

    /**
     * Called by system when mouse is moved with a button pressed
     */
    fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        mouseX = screenX
        mouseY = screenY
        return true
    }

    /**
     * Called by system when mouse is moved without buttons pressed
     */
    fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        mouseX = screenX
        mouseY = screenY
        return true
    }

    /**
     * Updates the mouse manager state. MUST be called once per frame.
     * This method implements "just clicked" logic. Functions as a
     * debounce mechanism to ensure a single mouse press is registered
     * as a single click event in game logic.
     */
    fun update() {
        if (cantLeftClick && !leftPressed) {
            cantLeftClick = false
        } else if (justLeftPressed) {
            cantLeftClick = true
            justLeftPressed = false
        }
        if (leftPressed && !cantLeftClick) {
            justLeftPressed = true
        }
    }

    /**
     * Checks if left mouse button is held down
     */
    fun isLeftPressed(): Boolean = leftPressed

    /**
     * Checks if right mouse button is held down
     */
    fun isRightPressed(): Boolean = rightPressed

    /**
     * Returns current X coordinate of mouse
     */
    fun getMouseX(): Int = mouseX

    /**
     * Returns current Y coordinate of mouse
     */
    fun getMouseY(): Int = mouseY

    /**
     * Checks if left mouse button was just clicked in this frame
     * @return True if it was a new click, false otherwise
     */
    fun isMouseJustClicked(): Boolean = justLeftPressed
}
