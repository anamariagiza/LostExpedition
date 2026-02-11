package com.lostexpedition.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array as GdxArray

object Assets {
    // --- PLAYER ANIMATIONS ---
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

    var playerJumpDown: Animation<TextureRegion>? = null
    var playerJumpUp: Animation<TextureRegion>? = null
    var playerJumpLeft: Animation<TextureRegion>? = null
    var playerJumpRight: Animation<TextureRegion>? = null

    var playerAttackLeft: Animation<TextureRegion>? = null
    var playerAttackRight: Animation<TextureRegion>? = null

    var playerHurt: Animation<TextureRegion>? = null

    // --- PLAYER SPECIAL ACTIONS (FROM JAVA) ---
    var playerThrust: Animation<TextureRegion>? = null
    var playerHalfslash: Animation<TextureRegion>? = null
    var playerBackslash: Animation<TextureRegion>? = null
    var playerSpellcast: Animation<TextureRegion>? = null
    var playerShoot: Animation<TextureRegion>? = null
    var playerSlash: Animation<TextureRegion>? = null
    var playerSit: Animation<TextureRegion>? = null
    var playerClimb: Animation<TextureRegion>? = null

    // --- ENEMY & NPC ---
    var agentAnimation: Animation<TextureRegion>? = null
    var jaguarAnimation: Animation<TextureRegion>? = null
    var monkeyAnimation: Animation<TextureRegion>? = null
    var batAnimation: Animation<TextureRegion>? = null
    var npcIdle: Animation<TextureRegion>? = null

    fun load() {
        loadPlayerAssets()
        loadEnemyAssets()
    }

    private fun loadPlayerAssets() {
        // IDLE: Java folosea 4 rânduri și 2 coloane [cite: 1, 270]
        playerIdleUp = loadAnimation("textures/player/idle.png", 4, 2, 0, 0.2f)
        playerIdleLeft = loadAnimation("textures/player/idle.png", 4, 2, 1, 0.2f)
        playerIdleDown = loadAnimation("textures/player/idle.png", 4, 2, 2, 0.2f)
        playerIdleRight = loadAnimation("textures/player/idle.png", 4, 2, 3, 0.2f)

        // WALK: Java folosea 4 rânduri și 9 coloane
        playerWalkUp = loadAnimation("textures/player/walk.png", 4, 9, 0, 0.15f)
        playerWalkLeft = loadAnimation("textures/player/walk.png", 4, 9, 1, 0.15f)
        playerWalkDown = loadAnimation("textures/player/walk.png", 4, 9, 2, 0.15f)
        playerWalkRight = loadAnimation("textures/player/walk.png", 4, 9, 3, 0.15f)

        // RUN: Java folosea 4 rânduri și 8 coloane [cite: 1, 278-280]
        playerRunUp = loadAnimation("textures/player/run.png", 4, 8, 0, 0.1f)
        playerRunLeft = loadAnimation("textures/player/run.png", 4, 8, 1, 0.1f)
        playerRunDown = loadAnimation("textures/player/run.png", 4, 8, 2, 0.1f)
        playerRunRight = loadAnimation("textures/player/run.png", 4, 8, 3, 0.1f)

        // JUMP: Java folosea 4 rânduri și 5 coloane [cite: 1, 281-284]
        playerJumpUp = loadAnimation("textures/player/jump.png", 4, 5, 0, 0.15f)
        playerJumpLeft = loadAnimation("textures/player/jump.png", 4, 5, 1, 0.15f)
        playerJumpDown = loadAnimation("textures/player/jump.png", 4, 5, 2, 0.15f)
        playerJumpRight = loadAnimation("textures/player/jump.png", 4, 5, 3, 0.15f)

        // HURT: Java folosea 1 rând și 6 coloane [cite: 1, 287]
        playerHurt = loadAnimation("textures/player/hurt.png", 1, 6, 0, 0.1f)

        // SPECIAL ATTACKS (CUTS FROM JAVA)
        playerThrust = loadAnimation("textures/player/thrust.png", 4, 8, 2, 0.1f) // Down row [cite: 1, 291]
        playerHalfslash = loadAnimation("textures/player/halfslash.png", 4, 7, 2, 0.1f) // [cite: 1, 293]
        playerBackslash = loadAnimation("textures/player/backslash.png", 4, 13, 2, 0.1f) // [cite: 1, 294]
        playerSpellcast = loadAnimation("textures/player/spellcast.png", 4, 7, 2, 0.1f) // [cite: 1, 296]
        playerShoot = loadAnimation("textures/player/shoot.png", 4, 13, 2, 0.1f) // [cite: 1, 297]
        playerSlash = loadAnimation("textures/player/slash.png", 4, 6, 2, 0.1f) // [cite: 1, 300]
        playerSit = loadAnimation("textures/player/sit.png", 4, 3, 2, 0.15f) // [cite: 1, 288]
        playerClimb = loadAnimation("textures/player/climb.png", 1, 6, 0, 0.1f) // [cite: 1, 285]
    }

    private fun loadEnemyAssets() {
        // ANIMALE & NPC: Folosim grilele identificate în Java (standard 4 rânduri x N coloane)
        // Decupăm rândul 2 (care de obicei este direcția "Down" sau "Forward") pentru a evita imaginea completă.
        jaguarAnimation = loadAnimation("textures/animals/jaguar.png", 4, 4, 2, 0.15f)
        monkeyAnimation = loadAnimation("textures/animals/monkey.png", 4, 4, 2, 0.15f)
        batAnimation = loadAnimation("textures/animals/bat.png", 4, 4, 2, 0.15f)

        // NPC (Old Man): Java folosea grila de idle [cite: 1, 270, 299]
        npcIdle = loadAnimation("textures/objects/old_man.png", 4, 2, 2, 0.2f)

        // Agent (Combat Idle): 4 rânduri x 2 coloane în Java [cite: 1, 299]
        agentAnimation = loadAnimation("textures/agent/combat_idle.png", 4, 2, 2, 0.15f)
    }

    /**
     * Funcție helper care replică exact metoda cropFramesFromSheet din Java [cite: 1, 313-328]
     * Extrage doar un rând specific dintr-un sprite sheet.
     */
    private fun loadAnimation(
        path: String,
        rows: Int,
        cols: Int,
        rowToCrop: Int,
        frameDuration: Float
    ): Animation<TextureRegion>? {
        return try {
            val texture = Texture(Gdx.files.internal(path))
            val frameWidth = 64 // Dimensiunea fixă folosită în Java
            val frameHeight = 64

            val tempFrames = TextureRegion.split(texture, frameWidth, frameHeight)
            val animationFrames = GdxArray<TextureRegion>()

            // Luăm doar rândul specificat (echivalent cu startRow din Java) [cite: 1, 323]
            for (c in 0 until cols) {
                animationFrames.add(tempFrames[rowToCrop][c])
            }

            Animation(frameDuration, animationFrames, Animation.PlayMode.LOOP)
        } catch (e: Exception) {
            null
        }
    }
}
