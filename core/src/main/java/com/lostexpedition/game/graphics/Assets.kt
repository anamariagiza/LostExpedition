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
        // Oglindim logica din codul initial kt: loadAnimationRow(path, rows, cols, rowToCrop)
        playerIdleUp = loadAnimationRow("textures/player/idle.png", 4, 2, 0, 0.2f)
        playerIdleLeft = loadAnimationRow("textures/player/idle.png", 4, 2, 1, 0.2f)
        playerIdleDown = loadAnimationRow("textures/player/idle.png", 4, 2, 2, 0.2f)
        playerIdleRight = loadAnimationRow("textures/player/idle.png", 4, 2, 3, 0.2f)

        playerWalkUp = loadAnimationRow("textures/player/walk.png", 4, 9, 0, 0.15f)
        playerWalkLeft = loadAnimationRow("textures/player/walk.png", 4, 9, 1, 0.15f)
        playerWalkDown = loadAnimationRow("textures/player/walk.png", 4, 9, 2, 0.15f)
        playerWalkRight = loadAnimationRow("textures/player/walk.png", 4, 9, 3, 0.15f)

        playerRunUp = loadAnimationRow("textures/player/run.png", 4, 8, 0, 0.1f)
        playerRunLeft = loadAnimationRow("textures/player/run.png", 4, 8, 1, 0.1f)
        playerRunDown = loadAnimationRow("textures/player/run.png", 4, 8, 2, 0.1f)
        playerRunRight = loadAnimationRow("textures/player/run.png", 4, 8, 3, 0.1f)

        playerHalfslashLeft = loadAnimationRow("textures/player/slash.png", 4, 6, 1, 0.1f)
        playerHalfslashRight = loadAnimationRow("textures/player/slash.png", 4, 6, 3, 0.1f)
        playerHalfslashUp = loadAnimationRow("textures/player/slash.png", 4, 6, 0, 0.1f)
        playerHalfslashDown = loadAnimationRow("textures/player/slash.png", 4, 6, 2, 0.1f)

        playerJumpLeft = loadAnimationRow("textures/player/jump.png", 4, 5, 1, 0.15f)
        playerJumpRight = loadAnimationRow("textures/player/jump.png", 4, 5, 3, 0.15f)
        playerJumpUp = loadAnimationRow("textures/player/jump.png", 4, 5, 0, 0.15f)
        playerJumpDown = loadAnimationRow("textures/player/jump.png", 4, 5, 2, 0.15f)

        playerHurt = loadAnimationRow("textures/player/hurt.png", 1, 6, 0, 0.1f)
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
        // MONKEY - Daca sprite sheet-ul are 4 randuri (Up, Left, Down, Right)
        // folosim randul 2 (DOWN - cu fata spre jucator)
        val monkeySheet = loadTexture("textures/animals/monkey.png")
        if (monkeySheet != null) {
            // Verificam daca e un grid 4x4 sau un singur rand cu frame-uri variabile
            // Daca inaltimea = 4 * MONKEY_FRAME_HEIGHT, atunci e grid 4x4
            if (monkeySheet.height >= 4 * MONKEY_FRAME_HEIGHT) {
                // Grid 4x4 - folosim randul 2 (DOWN)
                monkeyWalkAnim = loadAnimationFromRow(monkeySheet, MONKEY_FRAME_WIDTH, MONKEY_FRAME_HEIGHT, 2, 4, 0.12f)
            } else {
                // Un singur rand cu frame-uri variabile (ca in Java original)
                val monkeyFrames = arrayOf(
                    Pair(0, 36), Pair(36, 36), Pair(72, 28), Pair(100, 32),
                    Pair(132, 28), Pair(160, 34), Pair(194, 39), Pair(233, 41),
                    Pair(274, 29), Pair(303, 36), Pair(339, 38), Pair(377, 37),
                    Pair(414, 36)
                )
                monkeyWalkAnim = loadAnimationFromVariableFrames(monkeySheet, monkeyFrames, MONKEY_FRAME_HEIGHT, 0.12f)
            }
        }

        // JAGUAR - Aceeasi logica
        val jaguarSheet = loadTexture("textures/animals/jaguar.png")
        if (jaguarSheet != null) {
            if (jaguarSheet.height >= 4 * JAGUAR_FRAME_HEIGHT) {
                // Grid 4x4 - folosim randul 2 (DOWN)
                jaguarWalkAnim = loadAnimationFromRow(jaguarSheet, JAGUAR_FRAME_WIDTH, JAGUAR_FRAME_HEIGHT, 2, 4, 0.15f)
            } else {
                // Un singur rand cu frame-uri variabile
                val jaguarFrames = arrayOf(
                    Pair(0, 76), Pair(76, 73), Pair(149, 75), Pair(224, 71),
                    Pair(295, 70), Pair(365, 66), Pair(431, 63), Pair(494, 75)
                )
                jaguarWalkAnim = loadAnimationFromVariableFrames(jaguarSheet, jaguarFrames, JAGUAR_FRAME_HEIGHT, 0.15f)
            }
        }

        // BAT - Aceeasi logica
        val batSheet = loadTexture("textures/animals/bat.png")
        if (batSheet != null) {
            if (batSheet.height >= 4 * BAT_FRAME_HEIGHT) {
                // Grid 4x4 - folosim randul 2 (DOWN)
                batAnim = loadAnimationFromRow(batSheet, BAT_FRAME_WIDTH, BAT_FRAME_HEIGHT, 2, 4, 0.15f)
            } else {
                // Un singur rand cu frame-uri variabile
                val batFrames = arrayOf(
                    Pair(0, 18), Pair(18, 21), Pair(39, 17), Pair(56, 22)
                )
                batAnim = loadAnimationFromVariableFrames(batSheet, batFrames, BAT_FRAME_HEIGHT, 0.15f)
            }
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
     * VERSIUNEA PENTRU TEXTURE OBJECT (pentru Agent)
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
     * Functie helper care taie un rand specific dintr-un spritesheet (din codul initial).
     * VERSIUNEA PENTRU STRING PATH (pentru Player)
     * rows = numar total de randuri in spritesheet
     * cols = numar de coloane (frame-uri) de taiat
     * rowToCrop = randul specific de extras (0-indexed)
     */
    private fun loadAnimationRow(
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
            val frameWidth = texture.width / cols
            val frameHeight = texture.height / rows

            val tempFrames = TextureRegion.split(texture, frameWidth, frameHeight)
            val frameArray = GdxArray<TextureRegion>()

            // Luam doar randul corespunzator directiei dorite
            if (rowToCrop < tempFrames.size) {
                for (c in 0 until cols) {
                    frameArray.add(tempFrames[rowToCrop][c])
                }
            }

            Animation(frameDuration, frameArray, Animation.PlayMode.LOOP)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Incarca o animatie cu frame-uri de dimensiuni variabile
     * @param texture Texture-ul sursa
     * @param frameData Array de perechi (x, width) pentru fiecare frame
     * @param frameHeight Inaltimea comuna a tuturor frame-urilor
     * @param frameDuration Durata unui frame in secunde
     */
    private fun loadAnimationFromVariableFrames(
        texture: Texture,
        frameData: Array<Pair<Int, Int>>,
        frameHeight: Int,
        frameDuration: Float
    ): Animation<TextureRegion>? {
        return try {
            val frames = GdxArray<TextureRegion>()

            for ((x, width) in frameData) {
                // Verificare limite
                if (x >= 0 && x + width <= texture.width && frameHeight <= texture.height) {
                    frames.add(TextureRegion(texture, x, 0, width, frameHeight))
                } else {
                    println("ATENTIE: Frame-ul la x=$x, width=$width depaseste limitele texture-ului!")
                }
            }

            if (frames.size > 0) {
                Animation(frameDuration, frames, Animation.PlayMode.LOOP)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Eroare la incarcarea animatiei cu frame-uri variabile: ${e.message}")
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
