package com.lostexpedition.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

/**
 * Helper centralizat pentru fonturi UI scalate dupa rezolutia ecranului.
 * Foloseste font.ttf prin FreeType; cade pe fontul default daca lipseste.
 */
object UiFont {

    /** Factor de scalare UI: 1.0 la 720p, ~1.5 pe un telefon 1080p. */
    fun scale(): Float = Gdx.graphics.height / 720f

    fun make(size: Int, borderWidth: Float = 2f, color: Color = Color.WHITE): BitmapFont {
        return try {
            val generator = FreeTypeFontGenerator(Gdx.files.internal("font.ttf"))
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
            parameter.size = size
            parameter.borderWidth = borderWidth
            parameter.borderColor = Color.BLACK
            parameter.color = color
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ăâîșțĂÂÎȘȚ"
            parameter.minFilter = Texture.TextureFilter.Linear
            parameter.magFilter = Texture.TextureFilter.Linear
            val font = generator.generateFont(parameter)
            generator.dispose()
            font
        } catch (e: Exception) {
            Gdx.app.error("UiFont", "Nu s-a gasit 'font.ttf', folosesc fontul default scalat.")
            BitmapFont().apply {
                data.setScale(size / 15f)
                this.color = color
            }
        }
    }
}
