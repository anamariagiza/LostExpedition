package com.lostexpedition.game.graphics

import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 * Manages a sequence of images to create an animation.
 * This class receives a set of images (frames) and a speed, and handles
 * the logic to display the correct frame at the right time, creating the
 * illusion of movement. Can handle both looping and non-looping animations.
 */
class Animation(
    /** Animation speed (duration in milliseconds between frames) */
    private val speed: Int,
    /** Array of texture regions (frames) that compose the animation */
    private val frames: Array<TextureRegion>,
    /** Flag indicating if animation loops (true) or stops at end (false) */
    private val loops: Boolean = true
) {
    /** Current frame index in the animation sequence */
    private var index = 0

    /** Variables to manage frame change timing */
    private var lastTime = System.currentTimeMillis()
    private var timer = 0L

    /**
     * Updates the animation logic. Must be called every game frame.
     */
    fun update() {
        if (frames.isEmpty() || isFinished()) {
            return
        }

        timer += System.currentTimeMillis() - lastTime
        lastTime = System.currentTimeMillis()

        if (timer > speed) {
            index++
            timer = 0
            if (index >= frames.size) {
                if (loops) {
                    index = 0 // Return to first frame to create a loop
                } else {
                    index = frames.size - 1
                }
            }
        }
    }

    /**
     * Resets the animation to the first frame
     */
    fun reset() {
        index = 0
        timer = 0
        lastTime = System.currentTimeMillis()
    }

    /**
     * Checks if a non-looping animation has reached its end
     * @return True if animation is finished, false otherwise
     */
    fun isFinished(): Boolean {
        return !loops && index >= frames.size - 1
    }

    /**
     * Returns the current frame of the animation
     * @return A TextureRegion representing the current frame
     */
    fun getCurrentFrame(): TextureRegion? {
        if (frames.isEmpty()) return null
        return frames[index]
    }

    /**
     * Returns the index of the current frame
     * @return Numeric index of the frame
     */
    fun getIndex(): Int = index

    /**
     * Returns the total number of frames in the animation
     * @return Number of frames
     */
    fun getFramesLength(): Int = frames.size
}
