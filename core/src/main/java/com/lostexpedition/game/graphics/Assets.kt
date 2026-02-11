package com.lostexpedition.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array as GdxArray
import com.lostexpedition.game.utils.DebugLogger

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

    var playerJumpDown: Animation<TextureRegion>? = null
    var playerJumpUp: Animation<TextureRegion>? = null
    var playerJumpLeft: Animation<TextureRegion>? = null
    var playerJumpRight: Animation<TextureRegion>? = null

    var playerHurt: Animation<TextureRegion>? = null

    // --- ACTIVITATI SPECIALE PLAYER (DIN JAVA) ---
    var playerThrust: Animation<TextureRegion>? = null
    var playerHalfslash: Animation<TextureRegion>? = null
    var playerBackslash: Animation<TextureRegion>? = null
    var playerSpellcast: Animation<TextureRegion>? = null
    var playerShoot: Animation<TextureRegion>? = null
    var playerSlash: Animation<TextureRegion>? = null
    var playerSit: Animation<TextureRegion>? = null
    var playerClimb: Animation<TextureRegion>? = null

    // ==================== ENEMY & NPC ANIMATIONS ====================
    var agentAnimation: Animation<TextureRegion>? = null
    var enemyAnimation: Animation<TextureRegion>? = null // Referinta ceruta in Agent.kt
    var jaguarAnimation: Animation<TextureRegion>? = null
    var monkeyAnimation: Animation<TextureRegion>? = null
    var batAnimation: Animation<TextureRegion>? = null
    var npcIdle: Animation<TextureRegion>? = null

    // ==================== OBJECTS (TEXTURE REGIONS) ====================
    var keyImage: TextureRegion? = null
    var talismanImage: TextureRegion? = null
    var chestClosed: TextureRegion? = null
    var chestOpen: TextureRegion? = null
    var spikeTrapImage: TextureRegion? = null
    var puzzleTableImage: TextureRegion? = null
    var woodSignImage: TextureRegion? = null

    // ==================== UI & BACKGROUNDS ====================
    var backgroundMenu: Texture? = null
    var placeholderTexture: Texture? = null
    var placeholderRegion: TextureRegion? = null

    fun load() {
        // Creare placeholder prima data pentru siguranta
        placeholderTexture = Texture(Gdx.files.internal("libgdx.png"))
        placeholderRegion = TextureRegion(placeholderTexture)

        loadPlayerAssets()
        loadEnemyAssets()
        loadObjectAssets()
        loadUIAssets()
    }

    private fun loadPlayerAssets() {
        // IDLE: Java folosea 4 randuri si 2 coloane [cite: 1, 270-271]
        playerIdleUp = loadAnimation("textures/player/idle.png", 4, 2, 0, 0.2f)
        playerIdleLeft = loadAnimation("textures/player/idle.png", 4, 2, 1, 0.2f)
        playerIdleDown = loadAnimation("textures/player/idle.png", 4, 2, 2, 0.2f)
        playerIdleRight = loadAnimation("textures/player/idle.png", 4, 2, 3, 0.2f)

        // WALK: Java folosea 4 randuri si 9 coloane [cite: 1, 264-265]
        playerWalkUp = loadAnimation("textures/player/walk.png", 4, 9, 0, 0.15f)
        playerWalkLeft = loadAnimation("textures/player/walk.png", 4, 9, 1, 0.15f)
        playerWalkDown = loadAnimation("textures/player/walk.png", 4, 9, 2, 0.15f)
        playerWalkRight = loadAnimation("textures/player/walk.png", 4, 9, 3, 0.15f)

        // RUN: Java folosea 4 randuri si 8 coloane [cite: 1, 278-280]
        playerRunUp = loadAnimation("textures/player/run.png", 4, 8, 0, 0.1f)
        playerRunLeft = loadAnimation("textures/player/run.png", 4, 8, 1, 0.1f)
        playerRunDown = loadAnimation("textures/player/run.png", 4, 8, 2, 0.1f)
        playerRunRight = loadAnimation("textures/player/run.png", 4, 8, 3, 0.1f)

        // JUMP: Java folosea 4 randuri si 5 coloane [cite: 1, 281-284]
        playerJumpUp = loadAnimation("textures/player/jump.png", 4, 5, 0, 0.15f)
        playerJumpLeft = loadAnimation("textures/player/jump.png", 4, 5, 1, 0.15f)
        playerJumpDown = loadAnimation("textures/player/jump.png", 4, 5, 2, 0.15f)
        playerJumpRight = loadAnimation("textures/player/jump.png", 4, 5, 3, 0.15f)

        // HURT & CLIMB: 1 rand [cite: 1, 285-287]
        playerHurt = loadAnimation("textures/player/hurt.png", 1, 6, 0, 0.1f)
        playerClimb = loadAnimation("textures/player/climb.png", 1, 6, 0, 0.1f)

        // SPECIAL ATTACKS (REPLICATE DIN JAVA [cite: 1, 291-301])
        playerThrust = loadAnimation("textures/player/thrust.png", 4, 8, 2, 0.1f)
        playerHalfslash = loadAnimation("textures/player/halfslash.png", 4, 7, 2, 0.1f)
        playerBackslash = loadAnimation("textures/player/backslash.png", 4, 13, 2, 0.1f)
        playerSpellcast = loadAnimation("textures/player/spellcast.png", 4, 7, 2, 0.1f)
        playerShoot = loadAnimation("textures/player/shoot.png", 4, 13, 2, 0.1f)
        playerSlash = loadAnimation("textures/player/slash.png", 4, 6, 2, 0.1f)
        playerSit = loadAnimation("textures/player/sit.png", 4, 3, 2, 0.15f)

        playerAttackLeft = playerSlash
        playerAttackRight = playerSlash
    }

    private fun loadEnemyAssets() {
        // Agent (Combat Idle): Java specifica 4 randuri x 2 coloane [cite: 1, 299]
        agentAnimation = loadAnimation("textures/agent/combat_idle.png", 4, 2, 2, 0.15f)
        enemyAnimation = agentAnimation

        // ANIMALE: Folosim grila de 4 randuri x 4 coloane (standard) pentru a evita imaginea completa
        jaguarAnimation = loadAnimation("textures/animals/jaguar.png", 4, 4, 2, 0.15f)
        monkeyAnimation = loadAnimation("textures/animals/monkey.png", 4, 4, 2, 0.15f)
        batAnimation = loadAnimation("textures/animals/bat.png", 4, 4, 2, 0.15f)

        // NPC: 4 randuri x 2 coloane [cite: 1, 270]
        npcIdle = loadAnimation("textures/objects/old_man.png", 4, 2, 2, 0.2f)    }

    private fun loadObjectAssets() {
        keyImage = loadTextureRegion("textures/objects/key.png")
        talismanImage = loadTextureRegion("textures/objects/talisman.png")
        if (talismanImage == null) talismanImage = loadTextureRegion("textures/puzzles/moon.png")

        spikeTrapImage = loadTextureRegion("textures/traps/spikes.png")
        woodSignImage = loadTextureRegion("textures/objects/wood_sign.png")
        puzzleTableImage = loadTextureRegion("textures/objects/table.png")

        // Chest placeholders (sau texturi daca exista)
        chestClosed = placeholderRegion
        chestOpen = placeholderRegion
    }

    private fun loadUIAssets() {
        backgroundMenu = try {
            Texture(Gdx.files.internal("textures/menu_background.jpg"))
        } catch (e: Exception) {
            placeholderTexture
        }
    }

    // Helper pentru a replica crop-ul din Java [cite: 1, 302-328]
    private fun loadAnimation(
        path: String,
        rows: Int,
        cols: Int,
        rowToCrop: Int,
        frameDuration: Float
    ): Animation<TextureRegion>? {
        return try {
            val handle = Gdx.files.internal(path)
            if (!handle.exists()) return null

            val texture = Texture(handle)

            // Calculăm dimensiunea fiecărui pătrățel (frame)
            val frameWidth = texture.width / cols
            val frameHeight = texture.height / rows

            val tempFrames = TextureRegion.split(texture, frameWidth, frameHeight)
            val animationFrames = GdxArray<TextureRegion>()

            // Ne asigurăm că rândul cerut există în imagine
            val safeRow = if (rowToCrop < tempFrames.size) rowToCrop else 0

            for (c in 0 until cols) {
                animationFrames.add(tempFrames[safeRow][c])
            }

            Animation(frameDuration, animationFrames, Animation.PlayMode.LOOP)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadTextureRegion(path: String): TextureRegion? {
        return try {
            TextureRegion(Texture(Gdx.files.internal(path)))
        } catch (e: Exception) {
            placeholderRegion
        }
    }

    fun dispose() {
        placeholderTexture?.dispose()
        backgroundMenu?.dispose()
        // Curata orice alta textura creata manual aici
    }
}
