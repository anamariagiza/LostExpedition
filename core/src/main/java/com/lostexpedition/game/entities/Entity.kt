package com.lostexpedition.game.entities

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.utils.RefLinks

abstract class Entity(
    protected val refLink: RefLinks,
    var x: Float,
    var y: Float,
    var width: Int,
    var height: Int
) {
    var bounds: Rectangle = Rectangle(x, y, width.toFloat(), height.toFloat())

    abstract fun update()
    abstract fun render(batch: SpriteBatch)

    open fun dispose() {}

    fun checkEntityCollisions(xOffset: Float, yOffset: Float): Boolean {
        val player = refLink.player ?: return false

        for (entity in refLink.gameState?.getEntities() ?: emptyList()) {
            if (entity === this) continue

            val tempBounds = Rectangle(
                x + xOffset,
                y + yOffset,
                width.toFloat(),
                height.toFloat()
            )

            if (tempBounds.overlaps(entity.bounds)) {
                return true
            }
        }

        return false
    }

    protected fun isOnScreen(): Boolean {
        val camera = refLink.gameCamera
        val camX = camera.position.x - camera.viewportWidth / 2
        val camY = camera.position.y - camera.viewportHeight / 2

        return x + width > camX &&
            x < camX + camera.viewportWidth &&
            y + height > camY &&
            y < camY + camera.viewportHeight
    }
}
