package com.lostexpedition.game.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 * Manages a spritesheet image.
 * This class holds a reference to a large image containing multiple smaller
 * images (tiles, animation frames) arranged in a grid. Provides a simple
 * `crop` method to extract a single image (tile) from this grid based on
 * its coordinates (row and column).
 */
class SpriteSheet(private val texture: Texture) {

    companion object {
        /** Fixed width and height of a tile in pixels */
        const val TILE_WIDTH = 48
        const val TILE_HEIGHT = 48
    }

    /**
     * Extracts a single image (tile) from the spritesheet
     * @param x Column index from where to crop the tile (starting from 0)
     * @param y Row index from where to crop the tile (starting from 0)
     * @return A TextureRegion containing the cropped tile, or null if cropping fails
     */
    fun crop(x: Int, y: Int): TextureRegion? {
        val cropX = x * TILE_WIDTH
        val cropY = y * TILE_HEIGHT

        if (cropX < 0 || cropY < 0 ||
            cropX + TILE_WIDTH > texture.width ||
            cropY + TILE_HEIGHT > texture.height) {
            System.err.println(
                "WARNING (SpriteSheet.crop): Crop area exceeds spritesheet bounds! " +
                    "x=$x, y=$y, cropX=$cropX, cropY=$cropY, " +
                    "Sheet Dimensions: ${texture.width}x${texture.height}"
            )
            return null
        }

        // Create a TextureRegion that represents a portion of the texture
        return TextureRegion(texture, cropX, cropY, TILE_WIDTH, TILE_HEIGHT)
    }

    /**
     * Returns reference to the entire spritesheet texture
     * @return The Texture object of the spritesheet
     */
    fun getSpriteSheet(): Texture = texture

    /**
     * Returns the standard width of a tile
     * @return Tile width in pixels
     */
    fun getTileWidth(): Int = TILE_WIDTH

    /**
     * Returns the standard height of a tile
     * @return Tile height in pixels
     */
    fun getTileHeight(): Int = TILE_HEIGHT
}
