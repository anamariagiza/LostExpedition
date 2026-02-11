package com.lostexpedition.game.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array as GdxArray

/**
 * Clasa Assets gestioneaza toate resursele grafice ale jocului.
 * Echivalent cu Assets.java dar adaptat pentru LibGDX si Kotlin.
 */
object Assets {

    // ==================== CONSTANTE DIMENSIUNI ====================
    const val PLAYER_FRAME_WIDTH = 64
    const val PLAYER_FRAME_HEIGHT = 64
    const val AGENT_FRAME_WIDTH = 64
    const val AGENT_FRAME_HEIGHT = 64
    const val NPC_FRAME_WIDTH = 61
    const val NPC_FRAME_HEIGHT = 62
    const val MONKEY_FRAME_WIDTH = 36
    const val MONKEY_FRAME_HEIGHT = 52
    const val JAGUAR_FRAME_WIDTH = 76
    const val JAGUAR_FRAME_HEIGHT = 41
    const val BAT_FRAME_WIDTH = 19
    const val BAT_FRAME_HEIGHT = 20

    // ==================== PLAYER ANIMATIONS ====================
    // Idle
    var playerIdleDown: Animation<TextureRegion>? = null
    var playerIdleUp: Animation<TextureRegion>? = null
    var playerIdleLeft: Animation<TextureRegion>? = null
    var playerIdleRight: Animation<TextureRegion>? = null

    // Walk
    var playerWalkDown: Animation<TextureRegion>? = null
    var playerWalkUp: Animation<TextureRegion>? = null
    var playerWalkLeft: Animation<TextureRegion>? = null
    var playerWalkRight: Animation<TextureRegion>? = null

    // Run
    var playerRunDown: Animation<TextureRegion>? = null
    var playerRunUp: Animation<TextureRegion>? = null
    var playerRunLeft: Animation<TextureRegion>? = null
    var playerRunRight: Animation<TextureRegion>? = null

    // Jump
    var playerJumpDown: Animation<TextureRegion>? = null
    var playerJumpUp: Animation<TextureRegion>? = null
    var playerJumpLeft: Animation<TextureRegion>? = null
    var playerJumpRight: Animation<TextureRegion>? = null

    // Attack/Halfslash
    var playerHalfslashDown: Animation<TextureRegion>? = null
    var playerHalfslashUp: Animation<TextureRegion>? = null
    var playerHalfslashLeft: Animation<TextureRegion>? = null
    var playerHalfslashRight: Animation<TextureRegion>? = null

    // Aliases pentru compatibilitate cu codul existent
    var playerAttackLeft: Animation<TextureRegion>? = null
        get() = playerHalfslashLeft
    var playerAttackRight: Animation<TextureRegion>? = null
        get() = playerHalfslashRight

    // Hurt
    var playerHurt: Animation<TextureRegion>? = null

    // ==================== AGENT ANIMATIONS ====================
    // Idle
    var agentIdleDown: Animation<TextureRegion>? = null
    var agentIdleUp: Animation<TextureRegion>? = null
    var agentIdleLeft: Animation<TextureRegion>? = null
    var agentIdleRight: Animation<TextureRegion>? = null

    // Walk
    var agentWalkDown: Animation<TextureRegion>? = null
    var agentWalkUp: Animation<TextureRegion>? = null
    var agentWalkLeft: Animation<TextureRegion>? = null
    var agentWalkRight: Animation<TextureRegion>? = null

    // Run
    var agentRunDown: Animation<TextureRegion>? = null
    var agentRunUp: Animation<TextureRegion>? = null
    var agentRunLeft: Animation<TextureRegion>? = null
    var agentRunRight: Animation<TextureRegion>? = null

    // Attack/Halfslash
    var agentHalfslashDown: Animation<TextureRegion>? = null
    var agentHalfslashUp: Animation<TextureRegion>? = null
    var agentHalfslashLeft: Animation<TextureRegion>? = null
    var agentHalfslashRight: Animation<TextureRegion>? = null

    // Hurt
    var agentHurt: Animation<TextureRegion>? = null

    // Alias pentru compatibilitate cu codul existent
    var agentAnimation: Animation<TextureRegion>? = null
    var enemyAnimation: Animation<TextureRegion>? = null

    // ==================== ANIMAL ANIMATIONS ====================
    var monkeyWalkAnim: Animation<TextureRegion>? = null
    var jaguarWalkAnim: Animation<TextureRegion>? = null
    var batAnim: Animation<TextureRegion>? = null

    // Aliases pentru compatibilitate cu codul existent
    var monkeyAnimation: Animation<TextureRegion>? = null
        get() = monkeyWalkAnim
    var jaguarAnimation: Animation<TextureRegion>? = null
        get() = jaguarWalkAnim
    var batAnimation: Animation<TextureRegion>? = null
        get() = batAnim

    // ==================== NPC ANIMATIONS ====================
    var npcIdle: Animation<TextureRegion>? = null

    // ==================== OBJECTS ====================
    var keyImage: TextureRegion? = null
    var talismanImage: TextureRegion? = null
    var puzzleTableImage: TextureRegion? = null
    var woodSignImage: TextureRegion? = null
    var popupImage: TextureRegion? = null
    var spikeTrapImage: TextureRegion? = null

    // ==================== PUZZLE ELEMENTS ====================
    var puzzle1Sun: TextureRegion? = null
    var puzzle1Moon: TextureRegion? = null
    var puzzle1Star: TextureRegion? = null
    var puzzle1Bolt: TextureRegion? = null
    var puzzle2Gems: TextureRegion? = null
    var puzzle3Scroll: TextureRegion? = null
    var puzzle5CardFaces: Array<TextureRegion>? = null
    var puzzle5CardBack: TextureRegion? = null

    // ==================== TRAPS & LEVEL 3 ====================
    var trapDisabled: TextureRegion? = null
    var trapActiveAnim: Animation<TextureRegion>? = null
    var chestClosed: TextureRegion? = null
    var chestOpened: TextureRegion? = null

    // ==================== UI & BACKGROUNDS ====================
    var gameLogo: Texture? = null
    var backgroundMenu: Texture? = null

    // ==================== TILESETS ====================
    var jungleTilesetImage: Texture? = null
    var level2TilesetImage: Texture? = null
    var level3TilesetImage: Texture? = null

    // ==================== PLACEHOLDER ====================
    var placeholderTexture: Texture? = null
    var placeholderRegion: TextureRegion? = null

    /**
     * Initializare initiala - doar logo-ul pentru loading screen
     */
    fun init() {
        gameLogo = loadTexture("textures/logo.png")
        if (gameLogo == null) {
            println("Eroare: Nu s-a putut incarca logo.png!")
        }
    }

    /**
     * Incarca toate resursele grafice ale jocului
     */
    fun loadGameAssets() {
        // Placeholder pentru resurse lipsa
        try {
            placeholderTexture = Texture(Gdx.files.internal("libgdx.png"))
            placeholderRegion = TextureRegion(placeholderTexture)
        } catch (e: Exception) {
            println("ATENTIE: Nu s-a putut incarca placeholder-ul libgdx.png")
        }

        // Incarcare fundal meniu
        backgroundMenu = loadTexture("textures/menu_background.jpg")

        // Incarcare tilesets
        jungleTilesetImage = loadTexture("textures/gentle forest.png")
        level2TilesetImage = loadTexture("textures/tileset_level2.png")
        level3TilesetImage = loadTexture("textures/tileset_level3.png")

        // Incarcare obiecte
        loadObjectAssets()

        // Incarcare puzzle-uri
        loadPuzzleAssets()

        // Incarcare player animations
        loadPlayerAssets()

        // Incarcare agent animations
        loadAgentAssets()

        // Incarcare animal animations
        loadAnimalAssets()

        // Incarcare NPC animations
        loadNPCAssets()

        // Incarcare capcane
        loadTrapAssets()
    }

    /**
     * Alias pentru loadGameAssets() - pentru compatibilitate cu codul vechi
     */
    fun load() = loadGameAssets()

    private fun loadObjectAssets() {
        keyImage = loadTextureRegion("textures/objects/key.png")
        talismanImage = loadTextureRegion("textures/objects/talisman.png")
        popupImage = loadTextureRegion("textures/objects/pop_up.png")
        puzzleTableImage = loadTextureRegion("textures/objects/table.png")
        woodSignImage = loadTextureRegion("textures/objects/wood_sign.png")
        spikeTrapImage = loadTextureRegion("textures/traps/spikes.png")
    }

    private fun loadPuzzleAssets() {
        puzzle1Sun = loadTextureRegion("textures/puzzles/sun.png")
        puzzle1Moon = loadTextureRegion("textures/puzzles/moon.png")
        puzzle1Star = loadTextureRegion("textures/puzzles/star.png")
        puzzle1Bolt = loadTextureRegion("textures/puzzles/bolt.png")
        puzzle2Gems = loadTextureRegion("textures/puzzles/crystals.png")
        puzzle3Scroll = loadTextureRegion("textures/puzzles/ancient_scroll.png")

        // Card faces pentru puzzle 5
        val cardFacesSheet = loadTexture("textures/puzzles/card_faces.png")
        if (cardFacesSheet != null) {
            val CARD_WIDTH = 60
            val CARD_HEIGHT = 84
            val tempRegion = TextureRegion(cardFacesSheet)

            puzzle5CardBack = TextureRegion(tempRegion, 0, 0, CARD_WIDTH, CARD_HEIGHT)

            puzzle5CardFaces = Array(8) { i ->
                TextureRegion(tempRegion, (i + 1) * CARD_WIDTH, 0, CARD_WIDTH, CARD_HEIGHT)
            }
        }
    }

    private fun loadPlayerAssets() {
        // IDLE - 4 directii x 2 frame-uri
        val playerIdleSheet = loadTexture("textures/player/idle.png")
        if (playerIdleSheet != null) {
            playerIdleUp = loadAnimationFromRow(playerIdleSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 0, 2, 0.2f)
            playerIdleLeft = loadAnimationFromRow(playerIdleSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 1, 2, 0.2f)
            playerIdleDown = loadAnimationFromRow(playerIdleSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 2, 2, 0.2f)
            playerIdleRight = loadAnimationFromRow(playerIdleSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 3, 2, 0.2f)
        }

        // WALK - 4 directii x 9 frame-uri
        val playerWalkSheet = loadTexture("textures/player/walk.png")
        if (playerWalkSheet != null) {
            playerWalkUp = loadAnimationFromRow(playerWalkSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 0, 9, 0.15f)
            playerWalkLeft = loadAnimationFromRow(playerWalkSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 1, 9, 0.15f)
            playerWalkDown = loadAnimationFromRow(playerWalkSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 2, 9, 0.15f)
            playerWalkRight = loadAnimationFromRow(playerWalkSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 3, 9, 0.15f)
        }

        // RUN - 4 directii x 8 frame-uri
        val playerRunSheet = loadTexture("textures/player/run.png")
        if (playerRunSheet != null) {
            playerRunUp = loadAnimationFromRow(playerRunSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 0, 8, 0.1f)
            playerRunLeft = loadAnimationFromRow(playerRunSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 1, 8, 0.1f)
            playerRunDown = loadAnimationFromRow(playerRunSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 2, 8, 0.1f)
            playerRunRight = loadAnimationFromRow(playerRunSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 3, 8, 0.1f)
        }

        // JUMP - 4 directii x 5 frame-uri
        val playerJumpSheet = loadTexture("textures/player/jump.png")
        if (playerJumpSheet != null) {
            playerJumpUp = loadAnimationFromRow(playerJumpSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 0, 5, 0.15f)
            playerJumpLeft = loadAnimationFromRow(playerJumpSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 1, 5, 0.15f)
            playerJumpDown = loadAnimationFromRow(playerJumpSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 2, 5, 0.15f)
            playerJumpRight = loadAnimationFromRow(playerJumpSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 3, 5, 0.15f)
        }

        // HURT - 1 rand x 6 frame-uri
        val playerHurtSheet = loadTexture("textures/player/hurt.png")
        if (playerHurtSheet != null) {
            playerHurt = loadAnimationFromRow(playerHurtSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 0, 6, 0.1f)
        }

        // HALFSLASH (Attack) - 4 directii x 7 frame-uri
        val playerHalfslashSheet = loadTexture("textures/player/halfslash.png")
        if (playerHalfslashSheet != null) {
            playerHalfslashUp = loadAnimationFromRow(playerHalfslashSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 0, 7, 0.08f)
            playerHalfslashLeft = loadAnimationFromRow(playerHalfslashSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 1, 7, 0.08f)
            playerHalfslashDown = loadAnimationFromRow(playerHalfslashSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 2, 7, 0.08f)
            playerHalfslashRight = loadAnimationFromRow(playerHalfslashSheet, PLAYER_FRAME_WIDTH, PLAYER_FRAME_HEIGHT, 3, 7, 0.08f)
        }
    }

    private fun loadAgentAssets() {
        // IDLE - 4 directii x 2 frame-uri
        val agentIdleSheet = loadTexture("textures/agent/idle.png")
        if (agentIdleSheet != null) {
            agentIdleUp = loadAnimationFromRow(agentIdleSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 0, 2, 0.2f)
            agentIdleLeft = loadAnimationFromRow(agentIdleSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 1, 2, 0.2f)
            agentIdleDown = loadAnimationFromRow(agentIdleSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 2, 2, 0.2f)
            agentIdleRight = loadAnimationFromRow(agentIdleSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 3, 2, 0.2f)
        }

        // WALK - 4 directii x 9 frame-uri
        val agentWalkSheet = loadTexture("textures/agent/walk.png")
        if (agentWalkSheet != null) {
            agentWalkUp = loadAnimationFromRow(agentWalkSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 0, 9, 0.15f)
            agentWalkLeft = loadAnimationFromRow(agentWalkSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 1, 9, 0.15f)
            agentWalkDown = loadAnimationFromRow(agentWalkSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 2, 9, 0.15f)
            agentWalkRight = loadAnimationFromRow(agentWalkSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 3, 9, 0.15f)
        }

        // RUN - 4 directii x 8 frame-uri
        val agentRunSheet = loadTexture("textures/agent/run.png")
        if (agentRunSheet != null) {
            agentRunUp = loadAnimationFromRow(agentRunSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 0, 8, 0.1f)
            agentRunLeft = loadAnimationFromRow(agentRunSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 1, 8, 0.1f)
            agentRunDown = loadAnimationFromRow(agentRunSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 2, 8, 0.1f)
            agentRunRight = loadAnimationFromRow(agentRunSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 3, 8, 0.1f)
        }

        // HURT - 1 rand x 6 frame-uri
        val agentHurtSheet = loadTexture("textures/agent/hurt.png")
        if (agentHurtSheet != null) {
            agentHurt = loadAnimationFromRow(agentHurtSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 0, 6, 0.1f)
        }

        // HALFSLASH (Attack) - 4 directii x 7 frame-uri
        val agentHalfslashSheet = loadTexture("textures/agent/halfslash.png")
        if (agentHalfslashSheet != null) {
            agentHalfslashUp = loadAnimationFromRow(agentHalfslashSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 0, 7, 0.08f)
            agentHalfslashLeft = loadAnimationFromRow(agentHalfslashSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 1, 7, 0.08f)
            agentHalfslashDown = loadAnimationFromRow(agentHalfslashSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 2, 7, 0.08f)
            agentHalfslashRight = loadAnimationFromRow(agentHalfslashSheet, AGENT_FRAME_WIDTH, AGENT_FRAME_HEIGHT, 3, 7, 0.08f)
        }

        // Alias pentru compatibilitate cu codul existent
        agentAnimation = agentIdleDown
        enemyAnimation = agentIdleDown
    }

    private fun loadAnimalAssets() {
        // MONKEY - Animatie walk cu frame-uri de dimensiuni variabile
        val monkeySheet = loadTexture("textures/animals/monkey.png")
        if (monkeySheet != null) {
            // In Java, monkey-ul are 13 frame-uri cu dimensiuni variabile
            // Pentru simplificare in LibGDX, folosim o dimensiune uniforma
            monkeyWalkAnim = loadAnimationFromRow(monkeySheet, MONKEY_FRAME_WIDTH, MONKEY_FRAME_HEIGHT, 0, 13, 0.12f)
        }

        // JAGUAR - Animatie walk cu frame-uri de dimensiuni variabile
        val jaguarSheet = loadTexture("textures/animals/jaguar.png")
        if (jaguarSheet != null) {
            // In Java, jaguar-ul are 8 frame-uri cu dimensiuni variabile
            jaguarWalkAnim = loadAnimationFromRow(jaguarSheet, JAGUAR_FRAME_WIDTH, JAGUAR_FRAME_HEIGHT, 0, 8, 0.15f)
        }

        // BAT - Animatie zbor cu frame-uri de dimensiuni variabile
        val batSheet = loadTexture("textures/animals/bat.png")
        if (batSheet != null) {
            // In Java, bat-ul are 4 frame-uri cu dimensiuni variabile
            batAnim = loadAnimationFromRow(batSheet, BAT_FRAME_WIDTH, BAT_FRAME_HEIGHT, 0, 4, 0.15f)
        }
    }

    private fun loadNPCAssets() {
        // NPC (Old Man) - Idle animation
        val npcIdleSheet = loadTexture("textures/old_man.png")
        if (npcIdleSheet != null) {
            npcIdle = loadAnimationFromRow(npcIdleSheet, NPC_FRAME_WIDTH, NPC_FRAME_HEIGHT, 0, 6, 0.2f)
        }
    }

    private fun loadTrapAssets() {
        // Capcana cu tepi din nivelul 1/2
        val spikesSheet = loadTexture("textures/traps/spikes.png")
        if (spikesSheet != null) {
            spikeTrapImage = TextureRegion(spikesSheet, 0, 0, 39, 25)
        }

        // Capcane si chest din nivelul 3 (folosesc tileset-ul level3)
        if (level3TilesetImage != null) {
            trapDisabled = getTileImageByGID(45, level3TilesetImage!!)

            // Animatie trap activ - 3 frame-uri
            val trapFrames = GdxArray<TextureRegion>()
            val frame1 = getTileImageByGID(46, level3TilesetImage!!)
            val frame2 = getTileImageByGID(44, level3TilesetImage!!)
            val frame3 = getTileImageByGID(43, level3TilesetImage!!)

            if (frame1 != null) trapFrames.add(frame1)
            if (frame2 != null) trapFrames.add(frame2)
            if (frame3 != null) trapFrames.add(frame3)

            if (trapFrames.size > 0) {
                trapActiveAnim = Animation(0.15f, trapFrames, Animation.PlayMode.LOOP)
            }

            chestClosed = getTileImageByGID(522, level3TilesetImage!!)
            chestOpened = getTileImageByGID(614, level3TilesetImage!!)
        }
    }

    /**
     * Incarca o animatie dintr-un rand specific al unui spritesheet
     */
    private fun loadAnimationFromRow(
        texture: Texture,
        frameWidth: Int,
        frameHeight: Int,
        row: Int,
        numFrames: Int,
        frameDuration: Float
    ): Animation<TextureRegion>? {
        return try {
            val frames = GdxArray<TextureRegion>()

            for (col in 0 until numFrames) {
                val x = col * frameWidth
                val y = row * frameHeight

                // Verificare limite
                if (x + frameWidth <= texture.width && y + frameHeight <= texture.height) {
                    frames.add(TextureRegion(texture, x, y, frameWidth, frameHeight))
                } else {
                    println("ATENTIE: Frame-ul ($row, $col) depaseste limitele texture-ului!")
                }
            }

            if (frames.size > 0) {
                Animation(frameDuration, frames, Animation.PlayMode.LOOP)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Eroare la incarcarea animatiei din randul $row: ${e.message}")
            null
        }
    }

    /**
     * Extrage imaginea unei dale dintr-un tileset pe baza GID-ului
     * @param gid Global ID-ul dalei (din Tiled). GID-urile incep de la 1.
     * @param tilesetTexture Imaginea completa a tileset-ului.
     */
    private fun getTileImageByGID(gid: Int, tilesetTexture: Texture): TextureRegion? {
        if (gid == 0) return null

        val tileWidth = 32  // Dimensiune standard pentru tile-uri (ajusteaza daca e diferita)
        val tileHeight = 32

        val columns = tilesetTexture.width / tileWidth
        val index = gid - 1

        val tileX = (index % columns) * tileWidth
        val tileY = (index / columns) * tileHeight

        return try {
            if (tileX >= 0 && tileY >= 0 &&
                tileX + tileWidth <= tilesetTexture.width &&
                tileY + tileHeight <= tilesetTexture.height) {
                TextureRegion(tilesetTexture, tileX, tileY, tileWidth, tileHeight)
            } else {
                println("ATENTIE: GID $gid depaseste limitele tileset-ului!")
                null
            }
        } catch (e: Exception) {
            println("Eroare la extragerea tile-ului cu GID $gid: ${e.message}")
            null
        }
    }

    /**
     * Incarca o textura dintr-o cale
     */
    private fun loadTexture(path: String): Texture? {
        return try {
            val handle = Gdx.files.internal(path)
            if (handle.exists()) {
                Texture(handle)
            } else {
                println("Fisierul nu exista: $path")
                null
            }
        } catch (e: Exception) {
            println("Eroare la incarcarea texturii $path: ${e.message}")
            null
        }
    }

    /**
     * Incarca un TextureRegion dintr-o cale
     */
    private fun loadTextureRegion(path: String): TextureRegion? {
        val texture = loadTexture(path)
        return if (texture != null) TextureRegion(texture) else null
    }

    /**
     * Elibereaza toate resursele
     */
    fun dispose() {
        placeholderTexture?.dispose()
        backgroundMenu?.dispose()
        gameLogo?.dispose()
        jungleTilesetImage?.dispose()
        level2TilesetImage?.dispose()
        level3TilesetImage?.dispose()

        // NOTA: TextureRegion-urile nu trebuie dispose() individual,
        // ele sunt doar referinte catre Texture-uri
    }
}
