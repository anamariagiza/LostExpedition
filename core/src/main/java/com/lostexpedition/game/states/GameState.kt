package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.entities.*
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.map.FogOfWar
import com.lostexpedition.game.map.Map
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.RefLinks
import kotlin.math.abs

class GameState(
    refLink: RefLinks,
    private var startLevel: Int = 0
) : State(refLink) {

    companion object {
        private const val TOTAL_PUZZLES_LEVEL2 = 5
        private const val MESSAGE_DURATION_MS = 2000L
        private const val ANIMAL_DAMAGE_COOLDOWN_MS = 4000L
        private const val TRAP_DAMAGE_COOLDOWN_MS = 2000L
        private const val GLOBAL_ACTIVATION_DELAY_MS = 1000L
    }

    private val levelPaths = arrayOf(
        "maps/level_1.tmx",
        "maps/level_2.tmx",
        "maps/level_3.tmx"
    )

    private lateinit var currentMap: Map
    private lateinit var player: Player
    private var fogOfWar: FogOfWar? = null

    private var currentLevelIndex: Int = startLevel
    private var hasLevelKey = false
    var hasDoorKeys = BooleanArray(7) { false }
    private var hasTalisman = false
    private var caveEntranceUnlocked = false
    private var currentObjective = "Adună cheia și talismanul Lunii."
    private var isObjectiveDisplayed = false

    private val entities = mutableListOf<Entity>()
    var puzzlesSolved = BooleanArray(TOTAL_PUZZLES_LEVEL2 + 1) { false }
    private var caveGuardianNPC: NPC? = null
    private var caveEntrance: CaveEntrance? = null

    private var collectionMessage: String? = null
    private var collectionMessageTime = 0L
    private var woodSignMessage: String? = null

    private var lastAnimalDamageTime = 0L
    private var lastTrapDamageTime = 0L
    private var lastAgentTrapDamageTime = 0L

    private var finalBoss: Agent? = null
    private var finalChest: Chest? = null
    private var bossDefeated = false
    private var agentIsChasing = false
    private var trapsTriggered = false
    private var trapActivationTime = 0L
    private val arenaTraps = mutableListOf<Trap>()

    private val puzzleKeyPositions = arrayOf(
        intArrayOf(18, 22), intArrayOf(35, 16), intArrayOf(52, 22),
        intArrayOf(69, 13), intArrayOf(86, 22)
    )

    private val puzzleDoorPositions = arrayOf(
        intArrayOf(19, 24, 20, 24, 19, 25, 20, 25),
        intArrayOf(36, 18, 37, 18, 36, 19, 37, 19),
        intArrayOf(53, 24, 54, 24, 53, 25, 54, 25),
        intArrayOf(70, 15, 71, 15, 70, 16, 71, 16),
        intArrayOf(87, 24, 88, 24, 87, 25, 88, 25),
        intArrayOf(110, 15, 111, 15, 110, 16, 111, 16)
    )

    private val font = BitmapFont()
    private val shapeRenderer = ShapeRenderer()

    init {
        refLink.gameState = this

        // Configurare Input Processor pentru TouchController
        Gdx.input.inputProcessor = object : com.badlogic.gdx.InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val gameY = Gdx.graphics.height - screenY.toFloat()
                return refLink.touchController.touchDown(screenX.toFloat(), gameY, pointer)
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val gameY = Gdx.graphics.height - screenY.toFloat()
                return refLink.touchController.touchUp(screenX.toFloat(), gameY, pointer)
            }

            override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
                val gameY = Gdx.graphics.height - screenY.toFloat()
                return refLink.touchController.touchDragged(screenX.toFloat(), gameY, pointer)
            }
        }

        initLevelInternal(currentLevelIndex, false)
    }

    private fun initLevelInternal(desiredLevelIndex: Int, loadPlayerStateFromDb: Boolean) {
        var playerStartX = 100f
        var playerStartY = 100f
        var loadedHealth = 100

        currentLevelIndex = if (desiredLevelIndex in levelPaths.indices) desiredLevelIndex else 0

        currentMap = Map(refLink, levelPaths[currentLevelIndex], currentLevelIndex)
        refLink.map = currentMap
        // Setăm limitele hărții în cameră, dar controlul fin îl facem în update()
        refLink.gameCamera.setMapBounds(currentMap.width, currentMap.height)
        fogOfWar = FogOfWar(refLink, currentMap.width, currentMap.height)

        val TS = TileConstants.TILE_SIZE

        // Încărcare stare din baza de date sau setare default
        if (loadPlayerStateFromDb) {
            val loadedDataList = refLink.databaseManager.loadGameData()
            if (loadedDataList.isNotEmpty()) {
                val data = loadedDataList[0]
                currentLevelIndex = data.levelIndex
                playerStartX = data.playerX
                playerStartY = data.playerY
                loadedHealth = data.playerHealth
                hasLevelKey = data.hasKey
                hasDoorKeys = data.hasDoorKeys

                if (data.puzzlesSolvedString.isNotEmpty()) {
                    data.puzzlesSolvedString.split(",").forEach { idString ->
                        val id = idString.toIntOrNull()
                        if (id != null && id in 1..TOTAL_PUZZLES_LEVEL2) {
                            puzzlesSolved[id] = true
                        }
                    }
                }
                isObjectiveDisplayed = true
            } else {
                resetToDefaults()
            }
        } else {
            // Setări default pentru joc nou (folosind conversia de coordonate)
            currentLevelIndex = desiredLevelIndex
            when (currentLevelIndex) {
                0 -> {
                    playerStartX = 2f * TS
                    playerStartY = topDownY(2) // Echivalent cu Y=2 în Tiled (Sus)
                }
                1 -> {
                    playerStartX = 2f * TS
                    playerStartY = topDownY(26)
                }
                2 -> {
                    playerStartX = 37f * TS
                    playerStartY = topDownY(57)
                }
            }
        }

        player = Player(refLink, playerStartX, playerStartY)
        player.health = loadedHealth
        refLink.player = player

        // Centrare inițială cameră
        refLink.gameCamera.position.set(player.x, player.y, 0f)
        refLink.gameCamera.update()

        entities.clear()
        loadLevelEntities()
        updateObjective()
    }

    private fun resetToDefaults() {
        currentLevelIndex = 0
        hasLevelKey = false
        hasDoorKeys = BooleanArray(7) { false }
        puzzlesSolved = BooleanArray(TOTAL_PUZZLES_LEVEL2 + 1) { false }
        hasTalisman = false
        caveEntranceUnlocked = false
    }

    private fun loadLevelEntities() {
        when (currentLevelIndex) {
            0 -> loadLevel1Entities()
            1 -> loadLevel2Entities()
            2 -> loadLevel3Entities()
        }
    }

    /** * Convertește coordonata Y din sistemul "Top-Down" (Tiled/Java) în "Bottom-Up" (LibGDX).
     * @param gridY Coordonata Y în tile-uri (de sus în jos, începând de la 0).
     */
    private fun topDownY(gridY: Int): Float {
        return (currentMap.height - 1 - gridY) * TileConstants.TILE_SIZE
    }

    private fun getPlayerTileX(): Int = (player.x / TileConstants.TILE_SIZE).toInt()
    private fun getPlayerTileY(): Int = currentMap.height - 1 - (player.y / TileConstants.TILE_SIZE).toInt()

    private fun getTileJava(x: Int, javaY: Int): Tile {
        return currentMap.getTile(x, currentMap.height - 1 - javaY)
    }

    private fun changeTileGidJava(x: Int, javaY: Int, newGid: Int, layerIndex: Int) {
        currentMap.changeTileGid(x, currentMap.height - 1 - javaY, newGid, layerIndex)
    }

    // ==================== ÎNCĂRCARE ENTITĂȚI (COORDONATE FIXATE) ====================

    private fun loadLevel1Entities() {
        val TS = TileConstants.TILE_SIZE

        // Animale - Coordonate din Java/Tiled (Y=0 e sus)
        entities.add(Animal(refLink, 53f * TS, topDownY(5), 51f * TS, 56f * TS, Animal.AnimalType.JAGUAR))
        entities.add(Animal(refLink, 10f * TS, topDownY(36), 8f * TS, 11f * TS, Animal.AnimalType.MONKEY))
        entities.add(Animal(refLink, 89f * TS, topDownY(29), 88f * TS, 91f * TS, Animal.AnimalType.MONKEY))
        entities.add(Animal(refLink, 84f * TS, topDownY(57), 82f * TS, 85f * TS, Animal.AnimalType.BAT))

        // Capcane
        entities.add(Trap(refLink, 66f * TS, topDownY(31), TextureRegion(Assets.spikeTrapImage)))
        entities.add(Trap(refLink, 67f * TS, topDownY(38), TextureRegion(Assets.spikeTrapImage)))
        entities.add(Trap(refLink, 66f * TS, topDownY(45), TextureRegion(Assets.spikeTrapImage)))

        // NPC și Obiecte
        caveGuardianNPC = NPC(refLink, 93f * TS, topDownY(92))
        entities.add(caveGuardianNPC!!)

        entities.add(Talisman(refLink, 45f * TS, topDownY(52), TextureRegion(Assets.talismanImage)))

        caveEntrance = CaveEntrance(refLink, 91f * TS, topDownY(88), (TS * 2).toInt(), (TS * 2).toInt())
        entities.add(caveEntrance!!)

        if (!hasDoorKeys[0]) {
            entities.add(Key(refLink, 12f * TS, topDownY(85), Assets.keyImage, 0))
        }

        // Panoul de lemn (Lângă punctul de start al jucătorului la Y=2)
        val woodSign1 = DecorativeObject(
            refLink, 2f * TS, topDownY(1), 64, 64,
            TextureRegion(Assets.woodSignImage), true
        )
        woodSign1.setDialogueMessage("Adună cheia și talismanul Lunii.")
        entities.add(woodSign1)
    }

    private fun loadLevel2Entities() {
        val TS = TileConstants.TILE_SIZE
        val puzzleTableCoordinates = arrayOf(
            intArrayOf(19, 20), intArrayOf(36, 14), intArrayOf(53, 20),
            intArrayOf(70, 11), intArrayOf(87, 20)
        )

        for (coords in puzzleTableCoordinates) {
            val pixelX = coords[0] * TS
            val pixelY = topDownY(coords[1]) - 24
            entities.add(
                DecorativeObject(
                    refLink, pixelX, pixelY, 96, 48,
                    TextureRegion(Assets.puzzleTableImage), true
                )
            )
        }

        if (!isPuzzleSolved(1)) entities.add(PuzzleTrigger(refLink, 19f * TS, topDownY(20), TS.toInt(), TS.toInt(), 1))
        if (!isPuzzleSolved(2)) entities.add(PuzzleTrigger(refLink, 36f * TS, topDownY(14), TS.toInt(), TS.toInt(), 2))
        if (!isPuzzleSolved(3)) entities.add(PuzzleTrigger(refLink, 53f * TS, topDownY(20), TS.toInt(), TS.toInt(), 3))
        if (!isPuzzleSolved(4)) entities.add(PuzzleTrigger(refLink, 70f * TS, topDownY(11), TS.toInt(), TS.toInt(), 4))
        if (!isPuzzleSolved(5)) entities.add(PuzzleTrigger(refLink, 87f * TS, topDownY(20), TS.toInt(), TS.toInt(), 5))

        entities.add(LevelExit(refLink, 110f * TS, topDownY(14), (TS * 2).toInt(), TS.toInt()))

        for (i in 1..TOTAL_PUZZLES_LEVEL2) {
            if (isPuzzleSolved(i)) {
                val keyTileX = puzzleKeyPositions[i - 1][0]
                val keyTileY = puzzleKeyPositions[i - 1][1]
                entities.add(Key(refLink, keyTileX * TS, topDownY(keyTileY), Assets.keyImage, i))
            }
        }

        val woodSign2 = DecorativeObject(
            refLink, 3f * TS, topDownY(25), 64, 64,
            TextureRegion(Assets.woodSignImage), true
        )
        woodSign2.setDialogueMessage("Rezolvă puzzle-urile.")
        entities.add(woodSign2)
    }

    private fun loadLevel3Entities() {
        val TS = TileConstants.TILE_SIZE
        arenaTraps.clear()

        entities.add(
            DecorativeObject(
                refLink, 75f * TS, topDownY(26), TS.toInt(), TS.toInt(),
                TextureRegion(Assets.puzzleTableImage), true
            )
        )

        entities.add(PuzzleTrigger(refLink, 75f * TS, topDownY(26), TS.toInt(), TS.toInt(), 99))

        finalChest = Chest(refLink, 37f * TS, topDownY(3), TS.toInt(), TS.toInt())
        finalChest?.setCanInteract(false)
        entities.add(finalChest!!)

        val trapTiles = arrayOf(
            intArrayOf(29, 22), intArrayOf(30, 22), intArrayOf(31, 22), intArrayOf(32, 22),
            intArrayOf(29, 23), intArrayOf(30, 23), intArrayOf(31, 23), intArrayOf(32, 23),
            intArrayOf(29, 36), intArrayOf(30, 36), intArrayOf(31, 36), intArrayOf(32, 36),
            intArrayOf(29, 37), intArrayOf(30, 37), intArrayOf(31, 37), intArrayOf(32, 37),
            intArrayOf(38, 29), intArrayOf(39, 29), intArrayOf(40, 29), intArrayOf(41, 29),
            intArrayOf(38, 30), intArrayOf(39, 30), intArrayOf(40, 30), intArrayOf(41, 30),
            intArrayOf(47, 22), intArrayOf(48, 22), intArrayOf(49, 22), intArrayOf(50, 22),
            intArrayOf(47, 23), intArrayOf(48, 23), intArrayOf(49, 23), intArrayOf(50, 23),
            intArrayOf(47, 36), intArrayOf(48, 36), intArrayOf(49, 36), intArrayOf(50, 36),
            intArrayOf(47, 37), intArrayOf(48, 37), intArrayOf(49, 37), intArrayOf(50, 37)
        )

        for (pos in trapTiles) {
            val trap = Trap(
                refLink, pos[0] * TS, topDownY(pos[1]),
                TextureRegion(Assets.spikeTrapImage))
            entities.add(trap)
            arenaTraps.add(trap)
        }

        val triggerGroups = arrayOf(
            intArrayOf(46, 21, 51, 24), intArrayOf(37, 28, 42, 31), intArrayOf(28, 21, 33, 24),
            intArrayOf(46, 35, 51, 38), intArrayOf(28, 35, 33, 38)
        )

        for (group in triggerGroups) {
            val startX = group[0]
            val startY = group[1]
            val endX = group[2]
            val endY = group[3]

            for (javaY in startY..endY) {
                for (x in startX..endX) {
                    entities.add(TrapTrigger(refLink, x * TS, topDownY(javaY), TS.toInt(), TS.toInt()))
                }
            }
        }

        finalBoss = Agent(refLink, 36f * TS, topDownY(22), 36f * TS, 43f * TS, true)
        entities.add(finalBoss!!)

        val woodSign3 = DecorativeObject(
            refLink, 37f * TS, topDownY(56), 64, 64,
            TextureRegion(Assets.woodSignImage), true
        )
        woodSign3.setDialogueMessage("Învinge inamicul.")
        entities.add(woodSign3)
    }

    // ==================== UPDATE (LOGICA JOCULUI) ====================

    override fun update(delta: Float) {
        if (collectionMessage != null &&
            System.currentTimeMillis() - collectionMessageTime > MESSAGE_DURATION_MS
        ) {
            collectionMessage = null
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            refLink.setState(PauseState(refLink))
            return
        }

        // Input și logică player
        refLink.touchController.update()
        player.update() // Player ar trebui să primească delta, dar păstrăm așa dacă clasa Player nu cere

        if (player.health <= 0) {
            refLink.setState(GameOverState(refLink))
            return
        }

        // ===================================================================================
        // MODIFICARE CAMERĂ (ECHIVALENT JAVA checkBlankSpace)
        // ===================================================================================
        val gameCamera = refLink.gameCamera

        // 1. Centrare pe jucător
        gameCamera.position.x = player.x + player.width / 2
        gameCamera.position.y = player.y + player.height / 2

        // 2. Calcul limite
        val mapWidthPixels = currentMap.width * TileConstants.TILE_SIZE
        val mapHeightPixels = currentMap.height * TileConstants.TILE_SIZE
        val camHalfW = gameCamera.viewportWidth / 2
        val camHalfH = gameCamera.viewportHeight / 2

        // 3. Clamping (Restricționare) - Camera nu iese din hartă
        gameCamera.position.x = MathUtils.clamp(gameCamera.position.x, camHalfW, mapWidthPixels - camHalfW)
        gameCamera.position.y = MathUtils.clamp(gameCamera.position.y, camHalfH, mapHeightPixels - camHalfH)

        gameCamera.update()
        currentMap.render(gameCamera) // Actualizează vizualizarea hărții doar dacă e necesar
        // ===================================================================================

        updateLevelSpecificLogic(delta)
        updateEntities(delta)
    }

    private fun updateLevelSpecificLogic(delta: Float) {
        when (currentLevelIndex) {
            0 -> updateLevel1Logic()
            1 -> updateLevel2Logic()
            2 -> updateLevel3Logic()
        }
    }

    private fun updateLevel1Logic() {
        caveGuardianNPC?.let { npc ->
            if (player.bounds.overlaps(npc.bounds)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractPressed) {
                    if (hasTalisman) {
                        caveEntranceUnlocked = true
                        removeTalismanFromInventory()
                    } else {
                        collectionMessage = "Nu am talismanul!"
                        collectionMessageTime = System.currentTimeMillis()
                    }
                }
            }
        }

        caveEntrance?.let { entrance ->
            if (player.bounds.overlaps(entrance.bounds) &&
                (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractPressed)
            ) {
                if (caveEntranceUnlocked && hasDoorKeys[0]) {
                    passToLevel2()
                } else if (!caveEntranceUnlocked) {
                    collectionMessage = "Intrarea este blocată."
                    collectionMessageTime = System.currentTimeMillis()
                } else if (!hasDoorKeys[0]) {
                    collectionMessage = "Ai nevoie de cheie!"
                    collectionMessageTime = System.currentTimeMillis()
                }
            }
        }
    }

    private fun updateLevel2Logic() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractPressed) {
            checkAndOpenDoor()
        }

        val doorPos = puzzleDoorPositions[5]
        if (!getTileJava(doorPos[0], doorPos[1]).isSolid) {
            val doorBounds = Rectangle(
                doorPos[0] * TileConstants.TILE_SIZE,
                topDownY(doorPos[1]),
                TileConstants.TILE_SIZE * 2,
                TileConstants.TILE_SIZE
            )
            if (player.bounds.overlaps(doorBounds)) {
                passToLevel3()
            }
        }
    }

    private fun updateLevel3Logic() {
        handleFinalDoorInteraction()

        val playerTileX = getPlayerTileX()
        val playerTileY = getPlayerTileY()

        val tableTileX = 75
        val tableTileY = 26

        if (abs(playerTileX - tableTileX) <= 1 && abs(playerTileY - tableTileY) <= 1) {
            if ((Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractPressed) && woodSignMessage == null) {
                refLink.setState(WordPuzzleState(refLink))
            }
        }

        if (!trapsTriggered) {
            for (entity in entities) {
                if (entity is TrapTrigger && entity.bounds.overlaps(player.bounds)) {
                    trapsTriggered = true
                    trapActivationTime = System.currentTimeMillis()
                    break
                }
            }
        }

        if (trapsTriggered &&
            System.currentTimeMillis() - trapActivationTime >= GLOBAL_ACTIVATION_DELAY_MS
        ) {
            for (trap in arenaTraps) {
                trap.setActive(true)
            }
            trapsTriggered = false
        }

        finalBoss?.let { boss ->
            if (!agentIsChasing && playerTileX in 39..40 && playerTileY == 39) {
                agentIsChasing = true
                boss.setChaseMode(true)
            }

            if (boss.health <= 0 && !bossDefeated) {
                bossDefeated = true
                finalChest?.setCanInteract(true)
            }
        }
    }

    private fun updateEntities(delta: Float) {
        val iterator = entities.iterator()
        var playerInContactWithAnimal = false

        while (iterator.hasNext()) {
            val entity = iterator.next()
            entity.update()

            when (entity) {
                is Key -> {
                    if (entity.bounds.overlaps(player.bounds)) {
                        val associatedId = entity.associatedPuzzleId
                        if (associatedId in hasDoorKeys.indices) {
                            hasDoorKeys[associatedId] = true
                            collectionMessage = "Cheia colectată!"
                            collectionMessageTime = System.currentTimeMillis()
                        }
                        iterator.remove()
                    }
                }

                is Talisman -> {
                    if (entity.bounds.overlaps(player.bounds)) {
                        hasTalisman = true
                        collectionMessage = "Talisman colectat!"
                        collectionMessageTime = System.currentTimeMillis()
                        iterator.remove()
                    }
                }

                is Animal -> {
                    if (player.bounds.overlaps(entity.bounds)) {
                        playerInContactWithAnimal = true
                    }
                }

                is Trap -> {
                    if (currentLevelIndex == 0 || entity.isActive()) {
                        if (player.bounds.overlaps(entity.bounds)) {
                            if (System.currentTimeMillis() - lastTrapDamageTime >= TRAP_DAMAGE_COOLDOWN_MS) {
                                player.takeDamage(30)
                                lastTrapDamageTime = System.currentTimeMillis()
                            }
                        }
                        finalBoss?.let { boss ->
                            if (boss.bounds.overlaps(entity.bounds)) {
                                if (System.currentTimeMillis() - lastAgentTrapDamageTime >= TRAP_DAMAGE_COOLDOWN_MS) {
                                    boss.takeDamage(20)
                                    lastAgentTrapDamageTime = System.currentTimeMillis()
                                }
                            }
                        }
                    }
                }

                is PuzzleTrigger -> {
                    if (isPuzzleSolved(entity.getPuzzleId())) {
                        iterator.remove()
                    }
                }
            }
        }

        if (playerInContactWithAnimal) {
            if (System.currentTimeMillis() - lastAnimalDamageTime >= ANIMAL_DAMAGE_COOLDOWN_MS) {
                for (entity in entities) {
                    if (entity is Animal && player.bounds.overlaps(entity.bounds)) {
                        player.takeDamage(20)
                        break
                    }
                }
                lastAnimalDamageTime = System.currentTimeMillis()
            }
        }
    }

    // ==================== USI SI INTERACTIUNI ====================

    private fun checkAndOpenDoor() {
        val playerTileX = getPlayerTileX()
        val playerTileY = getPlayerTileY()
        val interactionRange = 2

        for (i in puzzleDoorPositions.indices) {
            val doorCoords = puzzleDoorPositions[i]
            if (abs(playerTileX - doorCoords[0]) <= interactionRange &&
                abs(playerTileY - doorCoords[1]) <= interactionRange
            ) {

                if (getTileJava(doorCoords[0], doorCoords[1]).isSolid) {
                    if (hasDoorKeys[i]) {
                        openDoor(i)
                        return
                    } else {
                        collectionMessage = "Ușa este blocată!"
                        collectionMessageTime = System.currentTimeMillis()
                        return
                    }
                }
            }
        }
    }

    private fun openDoor(doorIndex: Int) {
        if (doorIndex in hasDoorKeys.indices && hasDoorKeys[doorIndex]) {
            val doorCoords = puzzleDoorPositions[doorIndex]
            if (doorCoords.size == 8) {
                changeTileGidJava(doorCoords[0], doorCoords[1], 0, 1)
                changeTileGidJava(doorCoords[2], doorCoords[3], 0, 1)
                changeTileGidJava(doorCoords[4], doorCoords[5], 0, 1)
                changeTileGidJava(doorCoords[6], doorCoords[7], 0, 1)

                hasDoorKeys[doorIndex] = false
                collectionMessage = "Ușa s-a deschis!"
                collectionMessageTime = System.currentTimeMillis()
            }
        }
    }

    private fun handleFinalDoorInteraction() {
        val doorTileX = 39
        val doorTileY = 6  // Coordonata Java

        if (!getTileJava(doorTileX, doorTileY).isSolid) {
            return
        }

        val playerTileX = getPlayerTileX()
        val playerTileY = getPlayerTileY()

        if (abs(playerTileX - doorTileX) <= 2 && abs(playerTileY - doorTileY) <= 2) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractPressed) {
                if (hasDoorKeys.size > 6 && hasDoorKeys[6]) {
                    openFinalDoor()
                } else {
                    collectionMessage = "Ușa este încuiată."
                    collectionMessageTime = System.currentTimeMillis()
                }
            }
        }
    }

    private fun openFinalDoor() {
        val layerIndex = 2
        changeTileGidJava(39, 6, 74, layerIndex)
        changeTileGidJava(40, 6, 75, layerIndex)
        changeTileGidJava(39, 7, 120, layerIndex)
        changeTileGidJava(40, 7, 121, layerIndex)

        if (hasDoorKeys.size > 6) {
            hasDoorKeys[6] = false
        }
        collectionMessage = "Ușa s-a deblocat!"
        collectionMessageTime = System.currentTimeMillis()
    }

    private fun passToLevel2() {
        if (currentLevelIndex < levelPaths.size - 1) {
            refLink.setState(GameState(refLink, currentLevelIndex + 1))
        } else {
            refLink.setState(GameOverState(refLink))
        }
    }

    private fun passToLevel3() {
        println("Trecere la Nivelul 3!")
        currentLevelIndex = 2
        initLevelInternal(currentLevelIndex, false)
    }

    fun isPuzzleSolved(puzzleId: Int): Boolean {
        return if (puzzleId in 1..TOTAL_PUZZLES_LEVEL2) {
            puzzlesSolved[puzzleId]
        } else false
    }

    fun puzzleSolved(puzzleId: Int) {
        val TS = TileConstants.TILE_SIZE
        if (puzzleId in 1..TOTAL_PUZZLES_LEVEL2) {
            puzzlesSolved[puzzleId] = true
            if (puzzleId - 1 < puzzleKeyPositions.size) {
                val keyTileX = puzzleKeyPositions[puzzleId - 1][0]
                val keyTileY = puzzleKeyPositions[puzzleId - 1][1]
                entities.add(Key(refLink, keyTileX * TS, topDownY(keyTileY), Assets.keyImage, puzzleId))
            }
        }
    }

    fun onPuzzleFailure() {
        player.takeDamage(20)

        if (player.health <= 0) {
            refLink.setState(GameOverState(refLink))
        } else {
            val TS = TileConstants.TILE_SIZE
            player.setPosition(2f * TS, topDownY(26))
        }
    }

    fun removeTalismanFromInventory() {
        hasTalisman = false
        entities.removeAll { it is Talisman }
        println("Talismanul a fost predat.")
        collectionMessage = "Intrarea este deschisă!"
        collectionMessageTime = System.currentTimeMillis()
    }

    fun addEntity(entity: Entity) {
        entities.add(entity)
    }

    private fun updateObjective() {
        currentObjective = when (currentLevelIndex) {
            1 -> "Rezolvă puzzle-urile."
            2 -> "Învinge agentul și ia comoara."
            else -> "Adună cheia și talismanul."
        }
    }

    fun showWoodSignMessage(message: String?) {
        woodSignMessage = message
    }

    fun isWoodSignMessageShowing(): Boolean = woodSignMessage != null
    fun isCaveEntranceUnlocked(): Boolean = caveEntranceUnlocked
    fun setCaveEntranceUnlocked(unlocked: Boolean) { caveEntranceUnlocked = unlocked }

    fun saveCurrentState() {
        val solvedPuzzlesString = puzzlesSolved
            .indices
            .filter { puzzlesSolved[it] && it in 1..TOTAL_PUZZLES_LEVEL2 }
            .joinToString(",")

        refLink.databaseManager.saveGameData(
            levelIndex = currentLevelIndex,
            score = 0,
            playerX = player.x,
            playerY = player.y,
            playerHealth = player.health,
            hasKey = hasLevelKey,
            hasDoorKeys = hasDoorKeys,
            puzzlesSolvedString = solvedPuzzlesString
        )
    }

    fun getMap() = currentMap
    fun getEntities() = entities
    fun getCurrentLevel(): Int = currentLevelIndex

    // ==================== RENDERING ====================

    override fun render(batch: SpriteBatch) {
        val camera = refLink.gameCamera

        // 1. Hartă
        currentMap.render(camera)

        // 2. Entități
        val allEntities = entities.toMutableList()
        allEntities.add(player)
        finalBoss?.let { if (it.health > 0) allEntities.add(it) }
        allEntities.sortByDescending { it.y }

        batch.projectionMatrix = camera.combined
        batch.begin()
        for (entity in allEntities) {
            entity.render(batch)
        }
        batch.end()

        // 3. Ceață
        //fogOfWar?.render(batch, camera) // Decomentează dacă ai FogOfWar funcțional

        // 4. UI
        renderUI(batch)

        // 5. Controale
        refLink.touchController.draw()
    }

    private fun renderUI(batch: SpriteBatch) {
        batch.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        drawHealthBar(batch)

        batch.begin()

        collectionMessage?.let { msg ->
            font.color = Color.WHITE
            val layout = GlyphLayout(font, msg)
            val x = (Gdx.graphics.width - layout.width) / 2
            val y = Gdx.graphics.height / 2f
            font.draw(batch, msg, x, y)
        }

        if (isObjectiveDisplayed) {
            font.color = Color.YELLOW
            val objectiveText = "Obiectiv: $currentObjective"
            val layout = GlyphLayout(font, objectiveText)
            val x = (Gdx.graphics.width - layout.width) / 2
            font.draw(batch, objectiveText, x, 30f)
        }

        batch.end()

        woodSignMessage?.let { msg ->
            // Desenare fundal mesaj
            shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
            shapeRenderer.rect(Gdx.graphics.width / 2f - 250f, Gdx.graphics.height / 2f - 75f, 500f, 150f)
            shapeRenderer.end()

            // Desenare text mesaj
            batch.begin()
            font.color = Color.WHITE
            val layout = GlyphLayout(font, msg)
            val x = (Gdx.graphics.width - layout.width) / 2
            val y = Gdx.graphics.height / 2f
            font.draw(batch, msg, x, y)

            val instruction = "Apasă 'E' pentru a închide."
            val instrLayout = GlyphLayout(font, instruction)
            val instrX = (Gdx.graphics.width - instrLayout.width) / 2
            font.draw(batch, instruction, instrX, y - 50f)
            batch.end()
        }

        drawMiniMap(batch)
    }

    private fun drawHealthBar(batch: SpriteBatch) {
        if (batch.isDrawing) batch.end()

        val barWidth = 150f
        val barHeight = 20f
        val x = 10f
        val y = Gdx.graphics.height - 30f

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(x, y, barWidth, barHeight)

        val currentHealthWidth = (player.health.toFloat() / 100f) * barWidth
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(x, y, currentHealthWidth, barHeight)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(x, y, barWidth, barHeight)
        shapeRenderer.end()
    }

    private fun drawMiniMap(batch: SpriteBatch) {
        if (batch.isDrawing) batch.end()

        val miniMapHeight = 150f
        val TS = TileConstants.TILE_SIZE
        val mapPixelWidth = currentMap.width * TS
        val mapPixelHeight = currentMap.height * TS
        val miniMapWidth = (mapPixelWidth / mapPixelHeight) * miniMapHeight
        val padding = 10f

        val miniMapX = Gdx.graphics.width - miniMapWidth - padding
        val miniMapY = Gdx.graphics.height - miniMapHeight - padding

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
        shapeRenderer.rect(miniMapX, miniMapY, miniMapWidth, miniMapHeight)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(miniMapX, miniMapY, miniMapWidth, miniMapHeight)
        shapeRenderer.end()

        val mapScaleX = miniMapWidth / mapPixelWidth
        val mapScaleY = miniMapHeight / mapPixelHeight

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        for (yTile in 0 until currentMap.height) {
            for (xTile in 0 until currentMap.width) {
                val tile = currentMap.getTile(xTile, yTile)
                if (tile.isSolid) {
                    when (currentLevelIndex) {
                        0 -> shapeRenderer.color = Color(34f / 255f, 139f / 255f, 34f / 255f, 1f)
                        1 -> shapeRenderer.color = Color(100f / 255f, 100f / 255f, 100f / 255f, 1f)
                        else -> shapeRenderer.color = Color(87f / 255f, 51f / 255f, 35f / 255f, 1f)
                    }
                    shapeRenderer.rect(
                        miniMapX + xTile * TS * mapScaleX,
                        miniMapY + yTile * TS * mapScaleY,
                        TS * mapScaleX + 1,
                        TS * mapScaleY + 1
                    )
                }
            }
        }

        shapeRenderer.color = Color.YELLOW
        for (entity in entities) {
            if ((entity is Key || entity is Talisman)) {
                val entityMiniMapX = miniMapX + entity.x * mapScaleX
                val entityMiniMapY = miniMapY + entity.y * mapScaleY
                shapeRenderer.circle(entityMiniMapX, entityMiniMapY, 3f)
            }
        }

        shapeRenderer.color = Color.CYAN
        val playerMiniMapX = miniMapX + player.x * mapScaleX
        val playerMiniMapY = miniMapY + player.y * mapScaleY
        shapeRenderer.circle(playerMiniMapX, playerMiniMapY, 5f)

        shapeRenderer.end()
    }
}
