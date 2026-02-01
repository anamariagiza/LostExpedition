package com.lostexpedition.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array as GdxArray
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.DebugLogger

/**
 * Assets - Centralized asset loading and management
 *
 * Handles loading of all game textures, animations, and sprites.
 * Includes sprite sheet boundary checking (like Java version).
 *
 * @author LostExpedition Team
 */
object Assets {
    // ==================== PLAYER ANIMATIONS ====================
    var playerIdleDown: Animation<TextureRegion>? = null
    var playerIdleUp: Animation<TextureRegion>? = null
    var playerIdleLeft: Animation<TextureRegion>? = null
    var playerIdleRight: Animation<TextureRegion>? = null

    var playerWalkDown: Animation<TextureRegion>? = null
    var playerWalkUp: Animation<TextureRegion>? = null
    var playerWalkLeft: Animation<TextureRegion>? = null
    var playerWalkRight: Animation<TextureRegion>? = null

    var playerRunDown: Animation<TextureRegion>? = null
    var playerRunUp: Animation<TextureRegion>? = null
    var playerRunLeft: Animation<TextureRegion>? = null
    var playerRunRight: Animation<TextureRegion>? = null

    var playerAttackLeft: Animation<TextureRegion>? = null
    var playerAttackRight: Animation<TextureRegion>? = null

    var playerJumpLeft: Animation<TextureRegion>? = null
    var playerJumpRight: Animation<TextureRegion>? = null

    var playerHurt: Animation<TextureRegion>? = null

    // ==================== ENEMY ANIMATIONS ====================
    var agentAnimation: Animation<TextureRegion>? = null
    var enemyAnimation: Animation<TextureRegion>? = null

    var jaguarAnimation: Animation<TextureRegion>? = null
    var monkeyAnimation: Animation<TextureRegion>? = null
    var batAnimation: Animation<TextureRegion>? = null

    // ==================== NPC ANIMATIONS ====================
    var npcIdle: Animation<TextureRegion>? = null

    // ==================== OBJECTS ====================
    var keyImage: TextureRegion? = null
    var talismanImage: TextureRegion? = null
    var chestClosed: TextureRegion? = null
    var chestOpen: TextureRegion? = null
    var spikeTrapImage: TextureRegion? = null
    var puzzleTableImage: TextureRegion? = null
    var woodSignImage: TextureRegion? = null

    // ==================== UI ====================
    var backgroundMenu: Texture? = null

    // ==================== HELPER ====================
    var placeholderTexture: Texture? = null
    var placeholderRegion: TextureRegion? = null

    // ==================== LOADING ====================
    fun load() {
        println("Loading assets...")

        // Create placeholder first
        placeholderTexture = createColorTexture(64, 64, 0.8f, 0.8f, 0.8f)
        placeholderRegion = TextureRegion(placeholderTexture)

        try {
            loadPlayerAssets()
            loadEnemyAssets()
            loadObjectAssets()
            loadUIAssets()

            println("✓ All assets loaded successfully!")
        } catch (e: Exception) {
            println("✗ Error loading assets: ${e.message}")
            e.printStackTrace()
            createPlaceholderAssets()
        }
    }

    private fun loadPlayerAssets() {
        // Idle animations
        playerIdleDown = loadAnimation("textures/player/idle.png", 1, 1, 0.2f)
        playerIdleUp = playerIdleDown
        playerIdleLeft = playerIdleDown
        playerIdleRight = playerIdleDown

        // Walk animations
        playerWalkDown = loadAnimation("textures/player/walk.png", 1, 1, 0.15f)
        playerWalkUp = playerWalkDown
        playerWalkLeft = playerWalkDown
        playerWalkRight = playerWalkDown

        // Run animations
        playerRunDown = loadAnimation("textures/player/run.png", 1, 1, 0.1f)
        playerRunUp = playerRunDown
        playerRunLeft = playerRunDown
        playerRunRight = playerRunDown

        // Attack animations
        playerAttackLeft = loadAnimation("textures/player/slash.png", 1, 1, 0.1f)
        playerAttackRight = playerAttackLeft

        // Jump animations
        playerJumpLeft = loadAnimation("textures/player/jump.png", 1, 1, 0.15f)
        playerJumpRight = playerJumpLeft

        // Hurt animation
        playerHurt = loadAnimation("textures/player/hurt.png", 1, 1, 0.1f)
    }

    private fun loadEnemyAssets() {
        // Agent - try multiple file names
        agentAnimation = loadAnimation("textures/agent/combat_idle.png", 1, 1, 0.15f)
        if (agentAnimation == null) {
            agentAnimation = loadAnimation("textures/agent/idle.png", 1, 1, 0.15f)
        }
        if (agentAnimation == null) {
            agentAnimation = Animation(0.15f, placeholderRegion)
        }

        enemyAnimation = agentAnimation

        // Animals
        jaguarAnimation = loadAnimation("textures/animals/jaguar.png", 1, 1, 0.15f)
        monkeyAnimation = loadAnimation("textures/animals/monkey.png", 1, 1, 0.15f)
        batAnimation = loadAnimation("textures/animals/bat.png", 1, 1, 0.15f)

        // NPC
        npcIdle = loadAnimation("textures/objects/old_man.png", 1, 1, 0.2f)
        if (npcIdle == null) {
            npcIdle = Animation(0.2f, placeholderRegion)
        }
    }

    private fun loadObjectAssets() {
        // Key
        keyImage = loadTextureRegion("textures/objects/key.png")

        // Talisman
        talismanImage = loadTextureRegion("textures/puzzles/moon.png")
        if (talismanImage == null) {
            talismanImage = loadTextureRegion("textures/objects/talisman.png")
        }

        // Chest (no texture in your assets, use placeholder)
        chestClosed = placeholderRegion
        chestOpen = placeholderRegion

        // Spike Trap
        spikeTrapImage = loadTextureRegion("textures/traps/spikes.png")

        // Puzzle Table
        puzzleTableImage = loadTextureRegion("textures/objects/table.png")

        // Wood Sign
        woodSignImage = loadTextureRegion("textures/objects/wood_sign.png")
    }

    private fun loadUIAssets() {
        // Try different locations for menu background
        backgroundMenu = try {
            Texture(Gdx.files.internal("textures/menu_background.jpg"))
        } catch (e: Exception) {
            try {
                Texture(Gdx.files.internal("menu_background.jpg"))
            } catch (e2: Exception) {
                try {
                    // Try using gentle forest as background
                    Texture(Gdx.files.internal("textures/gentle forest.png"))
                } catch (e3: Exception) {
                    println("⚠️ Menu background not found, using placeholder")
                    createColorTexture(1920, 1080, 0.1f, 0.3f, 0.2f)
                }
            }
        }
    }

    /**
     * Load a texture and create an animation from it
     * For sprite sheets, specify rows and cols
     *
     * @param path Path to the texture file
     * @param rows Number of rows in the sprite sheet
     * @param cols Number of columns in the sprite sheet
     * @param frameDuration Duration of each frame in seconds
     * @return Animation or null if loading fails
     */
    private fun loadAnimation(
        path: String,
        rows: Int = 1,
        cols: Int = 1,
        frameDuration: Float = 0.15f
    ): Animation<TextureRegion>? {
        return try {
            val texture = Texture(Gdx.files.internal(path))

            // Sprite sheet boundary checking (like Java version)
            val frameWidth = texture.width / cols
            val frameHeight = texture.height / rows

            if (!checkSpriteSheetBounds(texture, frameWidth, frameHeight, rows, cols)) {
                DebugLogger.warn("Assets", "Sprite sheet bounds issue in: $path")
            }

            val frames = TextureRegion.split(texture, frameWidth, frameHeight)

            val frameArray = GdxArray<TextureRegion>()
            for (row in frames) {
                for (frame in row) {
                    frameArray.add(frame)
                }
            }

            Animation(frameDuration, frameArray, Animation.PlayMode.LOOP)
        } catch (e: Exception) {
            DebugLogger.warn("Assets", "Could not load animation: $path")
            null
        }
    }

    /**
     * Load animation with variable rectangle frame cropping (like Java version)
     *
     * Allows specifying exact frame dimensions and positions for non-uniform sprite sheets.
     *
     * @param path Path to the texture file
     * @param frameX Starting X position of the first frame
     * @param frameY Starting Y position of the first frame
     * @param frameWidth Width of each frame
     * @param frameHeight Height of each frame
     * @param frameCount Number of frames to extract
     * @param frameDuration Duration of each frame in seconds
     * @return Animation or null if loading fails
     */
    private fun loadAnimationCropped(
        path: String,
        frameX: Int,
        frameY: Int,
        frameWidth: Int,
        frameHeight: Int,
        frameCount: Int,
        frameDuration: Float = 0.15f
    ): Animation<TextureRegion>? {
        return try {
            val texture = Texture(Gdx.files.internal(path))

            // Boundary checking
            if (frameX + frameWidth * frameCount > texture.width) {
                DebugLogger.warn("Assets", "Frame width exceeds texture bounds in: $path")
                return null
            }
            if (frameY + frameHeight > texture.height) {
                DebugLogger.warn("Assets", "Frame height exceeds texture bounds in: $path")
                return null
            }

            val frameArray = GdxArray<TextureRegion>()
            for (i in 0 until frameCount) {
                val region = TextureRegion(
                    texture,
                    frameX + i * frameWidth,
                    frameY,
                    frameWidth,
                    frameHeight
                )
                frameArray.add(region)
            }

            Animation(frameDuration, frameArray, Animation.PlayMode.LOOP)
        } catch (e: Exception) {
            DebugLogger.warn("Assets", "Could not load cropped animation: $path")
            null
        }
    }

    /**
     * Checks if sprite sheet dimensions are valid
     *
     * @param texture The texture to check
     * @param frameWidth Width of each frame
     * @param frameHeight Height of each frame
     * @param rows Number of rows
     * @param cols Number of columns
     * @return True if bounds are valid
     */
    private fun checkSpriteSheetBounds(
        texture: Texture,
        frameWidth: Int,
        frameHeight: Int,
        rows: Int,
        cols: Int
    ): Boolean {
        val expectedWidth = frameWidth * cols
        val expectedHeight = frameHeight * rows

        if (expectedWidth > texture.width) {
            DebugLogger.error("Assets",
                "Sprite sheet width overflow: expected $expectedWidth, got ${texture.width}")
            return false
        }

        if (expectedHeight > texture.height) {
            DebugLogger.error("Assets",
                "Sprite sheet height overflow: expected $expectedHeight, got ${texture.height}")
            return false
        }

        // Check for remainder (non-even division)
        if (texture.width % cols != 0) {
            DebugLogger.verbose("Assets",
                "Texture width ${texture.width} does not divide evenly by $cols columns")
        }

        if (texture.height % rows != 0) {
            DebugLogger.verbose("Assets",
                "Texture height ${texture.height} does not divide evenly by $rows rows")
        }

        return true
    }

    /**
     * Crop a single frame from a texture (like Java version's crop method)
     *
     * @param texture The source texture
     * @param x Starting X position
     * @param y Starting Y position
     * @param width Frame width
     * @param height Frame height
     * @return TextureRegion of the cropped area
     */
    fun cropFrame(texture: Texture, x: Int, y: Int, width: Int, height: Int): TextureRegion {
        // Boundary checking
        val safeX = x.coerceIn(0, texture.width - 1)
        val safeY = y.coerceIn(0, texture.height - 1)
        val safeWidth = width.coerceAtMost(texture.width - safeX)
        val safeHeight = height.coerceAtMost(texture.height - safeY)

        if (safeX != x || safeY != y || safeWidth != width || safeHeight != height) {
            DebugLogger.warn("Assets",
                "Crop bounds adjusted: requested ($x,$y,$width,$height), " +
                "using ($safeX,$safeY,$safeWidth,$safeHeight)")
        }

        return TextureRegion(texture, safeX, safeY, safeWidth, safeHeight)
    }

    /**
     * Load a single texture region
     */
    private fun loadTextureRegion(path: String): TextureRegion? {
        return try {
            val texture = Texture(Gdx.files.internal(path))
            TextureRegion(texture)
        } catch (e: Exception) {
            println("⚠️ Could not load texture: $path")
            placeholderRegion
        }
    }

    private fun createPlaceholderAssets() {
        println("Creating placeholder assets...")

        placeholderTexture = createColorTexture(64, 64, 1f, 1f, 1f)
        placeholderRegion = TextureRegion(placeholderTexture)

        val region = placeholderRegion!!

        playerIdleDown = Animation(0.2f, region)
        playerIdleUp = playerIdleDown
        playerIdleLeft = playerIdleDown
        playerIdleRight = playerIdleDown

        playerWalkDown = Animation(0.15f, region)
        playerWalkUp = playerWalkDown
        playerWalkLeft = playerWalkDown
        playerWalkRight = playerWalkDown

        playerRunDown = playerWalkDown
        playerRunUp = playerWalkDown
        playerRunLeft = playerWalkDown
        playerRunRight = playerWalkDown

        playerAttackLeft = playerIdleDown
        playerAttackRight = playerIdleDown

        playerJumpLeft = playerIdleDown
        playerJumpRight = playerIdleDown

        playerHurt = playerIdleDown

        agentAnimation = Animation(0.15f, region)
        enemyAnimation = agentAnimation
        jaguarAnimation = agentAnimation
        monkeyAnimation = agentAnimation
        batAnimation = agentAnimation
        npcIdle = Animation(0.2f, region)

        keyImage = region
        talismanImage = region
        chestClosed = region
        chestOpen = region
        spikeTrapImage = region
        puzzleTableImage = region
        woodSignImage = region

        backgroundMenu = placeholderTexture
    }

    private fun createColorTexture(width: Int, height: Int, r: Float, g: Float, b: Float): Texture {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.setColor(r, g, b, 1f)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }

    fun dispose() {
        println("Disposing assets...")
        placeholderTexture?.dispose()
        backgroundMenu?.dispose()
        println("✓ Assets disposed")
    }
}
