package com.lostexpedition.game.entities

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.utils.RefLinks

/**
 * Entity - Clasa de bază pentru toate entitățile din joc
 */
abstract class Entity(
    protected val refLink: RefLinks,
    var x: Float,
    var y: Float,
    val width: Int,
    val height: Int
) {
    // ✅ open var pentru ca Player și Agent să poată override
    open var health: Int = 100

    // ✅ FIX: NU ABSTRACT, cu valoare default
    // Doar Key și Talisman o vor override cu implementare proprie
    open val isCollected: Boolean = false

    abstract fun update()
    abstract fun render(batch: SpriteBatch)

    open fun dispose() {
        // Default implementation - clasele pot override dacă au resurse de eliberat
    }

    // ✅ FIX: bounds ca PROPERTY, nu metodă
    val bounds: Bounds
        get() = Bounds(x, y, width.toFloat(), height.toFloat())

    // ✅ Metodă pentru setarea poziției
    open fun setPosition(newX: Float, newY: Float) {
        this.x = newX
        this.y = newY
    }

    // ✅ Metodă pentru verificare dacă entitatea e pe ecran
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

    // ✅ Conversie la LibGDX Rectangle (pentru compatibilitate)
    fun toRectangle(): Rectangle {
        return Rectangle(x, y, width.toFloat(), height.toFloat())
    }

    data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float) {
        // ✅ Metoda overlaps pentru Entity.Bounds
        fun overlaps(other: Bounds): Boolean {
            return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y
        }

        // ✅ Overload pentru LibGDX Rectangle
        fun overlaps(rect: com.badlogic.gdx.math.Rectangle): Boolean {
            return x < rect.x + rect.width &&
                x + width > rect.x &&
                y < rect.y + rect.height &&
                y + height > rect.y
        }

        // ✅ Conversie la Rectangle
        fun toRectangle(): Rectangle {
            return Rectangle(x, y, width, height)
        }
    }
}
