package com.lostexpedition.game.input

import com.badlogic.gdx.Input

/**
 * Manages and processes keyboard input for the game.
 * This class provides logic to detect both held keys (e.g., for movement)
 * and single key presses (e.g., for interactions) through the "just pressed" mechanism.
 */
class KeyManager {

    /** Public flags for quick access to main control key states */
    var up = false
    var down = false
    var left = false
    var right = false
    var enter = false
    var space = false
    var escape = false
    var z = false
    var shift = false
    var eKey = false
    var pKey = false
    var kKey = false  // Attack key

    /** Raw state vector for all keys (true = pressed, false = released) */
    private val keys = BooleanArray(256)

    /** Flag vector; true only in the frame where a key was just pressed */
    private val justPressed = BooleanArray(256)

    /** Helper vector for "just pressed" logic to prevent multiple presses */
    private val cantPress = BooleanArray(256)

    /**
     * Called by system when a key is pressed
     */
    fun keyDown(keycode: Int): Boolean {
        if (keycode < 0 || keycode >= keys.size) return false
        keys[keycode] = true
        return true
    }

    /**
     * Called by system when a key is released
     */
    fun keyUp(keycode: Int): Boolean {
        if (keycode < 0 || keycode >= keys.size) return false
        keys[keycode] = false
        return true
    }

    /**
     * Updates the key manager state. MUST be called once per frame.
     * This method processes raw input from the `keys` vector.
     * First, it implements "just pressed" logic to detect single presses.
     * Then, it updates public flags (up, down, etc.) for use in the game.
     */
    fun update() {
        // Logic for justPressed / cantPress
        for (i in keys.indices) {
            if (cantPress[i] && !keys[i]) {
                cantPress[i] = false
            } else if (justPressed[i]) {
                cantPress[i] = true
                justPressed[i] = false
            }
            if (keys[i] && !cantPress[i]) {
                justPressed[i] = true
            }
        }

        // Update specific flags
        up = keys[Input.Keys.W]
        down = keys[Input.Keys.S]
        left = keys[Input.Keys.A]
        right = keys[Input.Keys.D]
        enter = keys[Input.Keys.ENTER]
        space = keys[Input.Keys.SPACE]
        escape = keys[Input.Keys.ESCAPE]
        z = keys[Input.Keys.Z]
        shift = keys[Input.Keys.SHIFT_LEFT] || keys[Input.Keys.SHIFT_RIGHT]
        eKey = keys[Input.Keys.E]
        pKey = keys[Input.Keys.P]
        kKey = keys[Input.Keys.K]
    }

    /**
     * Completely resets the state of all keys.
     * Useful when transitioning between game states (e.g., from GameState to MenuState)
     * to prevent a key press from one state affecting the next state.
     */
    fun clearKeys() {
        for (i in keys.indices) {
            justPressed[i] = false
            cantPress[i] = false
            keys[i] = false
        }
        up = false
        down = false
        left = false
        right = false
        enter = false
        space = false
        escape = false
        z = false
        shift = false
        eKey = false
        pKey = false
        kKey = false
    }

    /**
     * Checks if a key was just pressed in this frame
     * @param keyCode The key code to check
     * @return True if the key was just pressed, false otherwise
     */
    fun isKeyJustPressed(keyCode: Int): Boolean {
        if (keyCode < 0 || keyCode >= keys.size) return false
        return justPressed[keyCode]
    }
}
