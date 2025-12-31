package com.lostexpedition.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture

/**
 * A utility class containing a static method for loading an image into memory.
 * The role of this class is to abstract the process of reading an image file
 * from project resources and converting it into a Texture object.
 */
object ImageLoader {

    /**
     * Loads an image from a file and returns a Texture object.
     * @param path Relative path to the image file, starting from the assets root (e.g., "textures/logo.png")
     * @return A Texture object containing the loaded image, or null in case of error
     */
    fun loadImage(path: String): Texture? {
        return try {
            println("DEBUG: Attempting to load resource from: $path")

            // Check if file exists
            val fileHandle = Gdx.files.internal(path)
            if (!fileHandle.exists()) {
                System.err.println("DEBUG: Resource NOT found at path: $path")
                return null
            }

            val texture = Texture(fileHandle)
            println("DEBUG: Successfully loaded texture from $path (${texture.width}x${texture.height})")
            texture

        } catch (e: Exception) {
            System.err.println("Error loading image: $path. Message: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
