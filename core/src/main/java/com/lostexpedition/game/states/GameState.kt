package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.entities.*
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.map.FogOfWar
import com.lostexpedition.game.map.Map
import com.lostexpedition.game.tiles.Tile
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

    private var finalBoss: Agent? = null
    private var finalChest: Chest? = null
    private var bossDefeated = false
    private var agentIsChasing = false
    private var trapsTriggered = false
    private var trapActivationTime = 0L
    private val arenaTraps = mutableListOf<Trap>()
    private var lastAgentTrapDamageTime = 0L

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
        initLevelInternal(currentLevelIndex, false)
    }

    private fun initLevelInternal(desiredLevelIndex: Int, loadPlayerStateFromDb: Boolean) {
        var playerStartX = 100f
        var playerStartY = 100f
        var loadedHealth = 100

        if (loadPlayerStateFromDb) {
            val loadedDataList = refLink.databaseManager.loadGameData()

            if (loadedDataList.isNotEmpty()) {
                val data = loadedDataList[0] // Luăm primul element

                // --- FIX: Folosim numele corecte din PlayerData ---
                currentLevelIndex = data.levelIndex // Era .level
                playerStartX = data.playerX         // Era .x
                playerStartY = data.playerY         // Era .y
                loadedHealth = data.playerHealth
                hasLevelKey = data.hasKey
                hasDoorKeys = data.hasDoorKeys

                // Parsare puzzle-uri salvate
                if (data.puzzlesSolvedString.isNotEmpty()) {
                    data.puzzlesSolvedString.split(",").forEach { idString ->
                        val id = idString.toIntOrNull()
                        if (id != null && id in 1..TOTAL_PUZZLES_LEVEL2) {
                            puzzlesSolved[id] = true
                        }
                    }
                }

                isObjectiveDisplayed = true
                println("Joc încărcat: Level $currentLevelIndex, Pos $playerStartX, $playerStartY")
            } else {
                resetToDefaults()
            }
        } else {
            currentLevelIndex = desiredLevelIndex
            when (currentLevelIndex) {
                0 -> {
                    playerStartX = 3f * Tile.TILE_WIDTH
                    playerStartY = 3f * Tile.TILE_HEIGHT
                }
                1 -> {
                    playerStartX = 2f * Tile.TILE_WIDTH
                    playerStartY = 26f * Tile.TILE_HEIGHT
                }
                2 -> {
                    playerStartX = 37f * Tile.TILE_WIDTH
                    playerStartY = 57f * Tile.TILE_HEIGHT
                }
            }
        }

        if (currentLevelIndex !in levelPaths.indices) {
            println("Index nivel invalid: $currentLevelIndex. Reset la nivel 1.")
            resetToDefaults()
        }

        currentMap = Map(refLink)
        currentMap.loadMapFromFile(levelPaths[currentLevelIndex])
        refLink.map = currentMap
        fogOfWar = FogOfWar(refLink, currentMap.width, currentMap.height)

        player = Player(refLink, playerStartX, playerStartY)
        player.health = loadedHealth
        refLink.player = player

        refLink.gameCamera.setMapBounds(currentMap.width, currentMap.height)
        refLink.gameCamera.centerOnEntity(
            player.x,
            player.y,
            player.width.toFloat(),
            player.height.toFloat()
        )

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

    private fun loadLevel1Entities() {
        entities.add(Animal(refLink, 53f * Tile.TILE_WIDTH, 5f * Tile.TILE_HEIGHT,
            51f * Tile.TILE_WIDTH, 56f * Tile.TILE_WIDTH, Animal.AnimalType.JAGUAR))
        entities.add(Animal(refLink, 10f * Tile.TILE_WIDTH, 36f * Tile.TILE_HEIGHT,
            8f * Tile.TILE_WIDTH, 11f * Tile.TILE_WIDTH, Animal.AnimalType.MONKEY))
        entities.add(Animal(refLink, 89f * Tile.TILE_WIDTH, 29f * Tile.TILE_HEIGHT,
            88f * Tile.TILE_WIDTH, 91f * Tile.TILE_WIDTH, Animal.AnimalType.MONKEY))
        entities.add(Animal(refLink, 84f * Tile.TILE_WIDTH, 57f * Tile.TILE_HEIGHT,
            82f * Tile.TILE_WIDTH, 85f * Tile.TILE_WIDTH, Animal.AnimalType.BAT))

        entities.add(Trap(refLink, 66f * Tile.TILE_WIDTH, 31f * Tile.TILE_HEIGHT,
            Assets.spikeTrapImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }))
        entities.add(Trap(refLink, 67f * Tile.TILE_WIDTH, 38f * Tile.TILE_HEIGHT,
            Assets.spikeTrapImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }))
        entities.add(Trap(refLink, 66f * Tile.TILE_WIDTH, 45f * Tile.TILE_HEIGHT,
            Assets.spikeTrapImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }))

        caveGuardianNPC = NPC(refLink, 93f * Tile.TILE_WIDTH, 92f * Tile.TILE_HEIGHT)
        entities.add(caveGuardianNPC!!)

        entities.add(Talisman(refLink, 45f * Tile.TILE_WIDTH, 52f * Tile.TILE_HEIGHT,
            Assets.talismanImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }))

        caveEntrance = CaveEntrance(refLink, 91f * Tile.TILE_WIDTH, 88f * Tile.TILE_HEIGHT,
            Tile.TILE_WIDTH * 2, Tile.TILE_HEIGHT * 2)
        entities.add(caveEntrance!!)

        if (!hasDoorKeys[0]) {
            entities.add(Key(refLink, 12f * Tile.TILE_WIDTH, 85f * Tile.TILE_HEIGHT,
                Assets.keyImage, 0))
        }

        val woodSign1 = DecorativeObject(refLink, 2f * Tile.TILE_WIDTH, Tile.TILE_HEIGHT.toFloat(),
            64, 64, Assets.woodSignImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }, true)
        woodSign1.setDialogueMessage("Adună cheia și talismanul Lunii.")
        entities.add(woodSign1)
    }

    private fun loadLevel2Entities() {
        val puzzleTableCoordinates = arrayOf(
            intArrayOf(19, 20), intArrayOf(36, 14), intArrayOf(53, 20),
            intArrayOf(70, 11), intArrayOf(87, 20)
        )

        for (coords in puzzleTableCoordinates) {
            val pixelX = coords[0] * Tile.TILE_WIDTH
            val pixelY = coords[1] * Tile.TILE_HEIGHT - 24
            entities.add(DecorativeObject(refLink, pixelX.toFloat(), pixelY.toFloat(),
                96, 48, Assets.puzzleTableImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }, true))
        }

        if (!isPuzzleSolved(1)) {
            entities.add(PuzzleTrigger(refLink, 19f * Tile.TILE_WIDTH, 20f * Tile.TILE_HEIGHT,
                Tile.TILE_WIDTH, Tile.TILE_HEIGHT, 1))
        }
        if (!isPuzzleSolved(2)) {
            entities.add(PuzzleTrigger(refLink, 36f * Tile.TILE_WIDTH, 14f * Tile.TILE_HEIGHT,
                Tile.TILE_WIDTH, Tile.TILE_HEIGHT, 2))
        }
        if (!isPuzzleSolved(3)) {
            entities.add(PuzzleTrigger(refLink, 53f * Tile.TILE_WIDTH, 20f * Tile.TILE_HEIGHT,
                Tile.TILE_WIDTH, Tile.TILE_HEIGHT, 3))
        }
        if (!isPuzzleSolved(4)) {
            entities.add(PuzzleTrigger(refLink, 70f * Tile.TILE_WIDTH, 11f * Tile.TILE_HEIGHT,
                Tile.TILE_WIDTH, Tile.TILE_HEIGHT, 4))
        }
        if (!isPuzzleSolved(5)) {
            entities.add(PuzzleTrigger(refLink, 87f * Tile.TILE_WIDTH, 20f * Tile.TILE_HEIGHT,
                Tile.TILE_WIDTH, Tile.TILE_HEIGHT, 5))
        }

        entities.add(LevelExit(refLink, 110f * Tile.TILE_WIDTH, 14f * Tile.TILE_HEIGHT,
            Tile.TILE_WIDTH * 2, Tile.TILE_HEIGHT))

        for (i in 1..TOTAL_PUZZLES_LEVEL2) {
            if (isPuzzleSolved(i)) {
                val keyTileX = puzzleKeyPositions[i - 1][0]
                val keyTileY = puzzleKeyPositions[i - 1][1]
                entities.add(Key(refLink, keyTileX.toFloat() * Tile.TILE_WIDTH,
                    keyTileY.toFloat() * Tile.TILE_HEIGHT, Assets.keyImage, i))
            }
        }

        val woodSign2 = DecorativeObject(refLink, 3f * Tile.TILE_WIDTH, 25f * Tile.TILE_HEIGHT,
            64, 64, Assets.woodSignImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }, true)
        woodSign2.setDialogueMessage("Rezolvă puzzle-urile.")
        entities.add(woodSign2)
    }

    private fun loadLevel3Entities() {
        arenaTraps.clear()

        entities.add(DecorativeObject(refLink, 75f * Tile.TILE_WIDTH, 26f * Tile.TILE_HEIGHT,
            Tile.TILE_WIDTH, Tile.TILE_HEIGHT, Assets.puzzleTableImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }, true))

        entities.add(PuzzleTrigger(refLink, 75f * Tile.TILE_WIDTH, 26f * Tile.TILE_HEIGHT,
            Tile.TILE_WIDTH, Tile.TILE_HEIGHT, 99))

        finalChest = Chest(refLink, 37f * Tile.TILE_WIDTH, 3f * Tile.TILE_HEIGHT,
            Tile.TILE_WIDTH, Tile.TILE_HEIGHT)
        finalChest?.setCanInteract(false)
        entities.add(finalChest!!)

        val trapTiles = arrayOf(
            intArrayOf(29,22), intArrayOf(30,22), intArrayOf(31,22), intArrayOf(32,22),
            intArrayOf(29,23), intArrayOf(30,23), intArrayOf(31,23), intArrayOf(32,23),
            intArrayOf(29,36), intArrayOf(30,36), intArrayOf(31,36), intArrayOf(32,36),
            intArrayOf(29,37), intArrayOf(30,37), intArrayOf(31,37), intArrayOf(32,37),
            intArrayOf(38,29), intArrayOf(39,29), intArrayOf(40,29), intArrayOf(41,29),
            intArrayOf(38,30), intArrayOf(39,30), intArrayOf(40,30), intArrayOf(41,30),
            intArrayOf(47,22), intArrayOf(48,22), intArrayOf(49,22), intArrayOf(50,22),
            intArrayOf(47,23), intArrayOf(48,23), intArrayOf(49,23), intArrayOf(50,23),
            intArrayOf(47,36), intArrayOf(48,36), intArrayOf(49,36), intArrayOf(50,36),
            intArrayOf(47,37), intArrayOf(48,37), intArrayOf(49,37), intArrayOf(50,37)
        )

        for (pos in trapTiles) {
            val trap = Trap(refLink, pos[0].toFloat() * Tile.TILE_WIDTH,
                pos[1].toFloat() * Tile.TILE_HEIGHT,
                Assets.spikeTrapImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) })
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

            for (y in startY..endY) {
                for (x in startX..endX) {
                    entities.add(TrapTrigger(refLink, x.toFloat() * Tile.TILE_WIDTH,
                        y.toFloat() * Tile.TILE_HEIGHT, Tile.TILE_WIDTH, Tile.TILE_HEIGHT))
                }
            }
        }

        finalBoss = Agent(refLink, 36f * Tile.TILE_WIDTH, 22f * Tile.TILE_HEIGHT,
            36f * Tile.TILE_WIDTH, 43f * Tile.TILE_WIDTH, true)
        entities.add(finalBoss!!)

        val woodSign3 = DecorativeObject(refLink, 37f * Tile.TILE_WIDTH, 56f * Tile.TILE_HEIGHT,
            64, 64, Assets.woodSignImage?.let { com.badlogic.gdx.graphics.g2d.TextureRegion(it) }, true)
        woodSign3.setDialogueMessage("Învinge inamicul.")
        entities.add(woodSign3)
    }

    override fun update(delta: Float) {
        if (collectionMessage != null &&
            System.currentTimeMillis() - collectionMessageTime > MESSAGE_DURATION_MS) {
            collectionMessage = null
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // Putem adăuga și logica de salvare aici dacă vrei
            // saveCurrentState()
            refLink.setState(PauseState(refLink))
            return
        }

        currentMap.update()
        fogOfWar?.update()
        player.update()

        if (player.health <= 0) {
            refLink.setState(GameOverState(refLink))
            return
        }

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
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
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
                Gdx.input.isKeyJustPressed(Input.Keys.E)) {
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            checkAndOpenDoor()
        }

        val doorPos = puzzleDoorPositions[5]
        if (!currentMap.getTile(doorPos[0], doorPos[1]).isSolid) {
            val doorBounds = Rectangle(
                doorPos[0] * Tile.TILE_WIDTH.toFloat(),
                doorPos[1] * Tile.TILE_HEIGHT.toFloat(),
                (Tile.TILE_WIDTH * 2).toFloat(),
                Tile.TILE_HEIGHT.toFloat()
            )
            if (player.bounds.overlaps(doorBounds)) {
                passToLevel3()
            }
        }
    }

    private fun updateLevel3Logic() {
        handleFinalDoorInteraction()

        val playerTileX = (player.x / Tile.TILE_WIDTH).toInt()
        val playerTileY = (player.y / Tile.TILE_HEIGHT).toInt()

        val tableTileX = 75
        val tableTileY = 26

        if (abs(playerTileX - tableTileX) <= 1 && abs(playerTileY - tableTileY) <= 1) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) && woodSignMessage == null) {
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
            System.currentTimeMillis() - trapActivationTime >= GLOBAL_ACTIVATION_DELAY_MS) {
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

    // ==================== DOOR MANAGEMENT ====================
    private fun checkAndOpenDoor() {
        val playerTileX = (player.x / Tile.TILE_WIDTH).toInt()
        val playerTileY = (player.y / Tile.TILE_HEIGHT).toInt()
        val interactionRange = 2

        for (i in puzzleDoorPositions.indices) {
            val doorCoords = puzzleDoorPositions[i]
            if (abs(playerTileX - doorCoords[0]) <= interactionRange &&
                abs(playerTileY - doorCoords[1]) <= interactionRange) {

                if (currentMap.getTile(doorCoords[0], doorCoords[1]).isSolid) {
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
                currentMap.changeTileGid(doorCoords[0], doorCoords[1], Tile.DOOR_OPEN_TOP_LEFT_GID, 1)
                currentMap.changeTileGid(doorCoords[2], doorCoords[3], Tile.DOOR_OPEN_TOP_RIGHT_GID, 1)
                currentMap.changeTileGid(doorCoords[4], doorCoords[5], Tile.DOOR_OPEN_BOTTOM_LEFT_GID, 1)
                currentMap.changeTileGid(doorCoords[6], doorCoords[7], Tile.DOOR_OPEN_BOTTOM_RIGHT_GID, 1)

                hasDoorKeys[doorIndex] = false
                collectionMessage = "Ușa s-a deschis!"
                collectionMessageTime = System.currentTimeMillis()
            }
        }
    }

    private fun handleFinalDoorInteraction() {
        val doorTileX = 39
        val doorTileY = 6

        if (!currentMap.getTile(doorTileX, doorTileY).isSolid) {
            return
        }

        val playerTileX = (player.x / Tile.TILE_WIDTH).toInt()
        val playerTileY = (player.y / Tile.TILE_HEIGHT).toInt()

        if (abs(playerTileX - doorTileX) <= 2 && abs(playerTileY - doorTileY) <= 2) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
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
        currentMap.changeTileGid(39, 6, 74, layerIndex)
        currentMap.changeTileGid(40, 6, 75, layerIndex)
        currentMap.changeTileGid(39, 7, 120, layerIndex)
        currentMap.changeTileGid(40, 7, 121, layerIndex)

        if (hasDoorKeys.size > 6) {
            hasDoorKeys[6] = false
        }
        collectionMessage = "Ușa s-a deblocat!"
        collectionMessageTime = System.currentTimeMillis()
    }

    // ==================== LEVEL TRANSITIONS ====================
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

    // ==================== PUZZLE MANAGEMENT ====================
    fun isPuzzleSolved(puzzleId: Int): Boolean {
        return if (puzzleId in 1..TOTAL_PUZZLES_LEVEL2) {
            puzzlesSolved[puzzleId]
        } else false
    }

    fun puzzleSolved(puzzleId: Int) {
        if (puzzleId in 1..TOTAL_PUZZLES_LEVEL2) {
            puzzlesSolved[puzzleId] = true
            if (puzzleId - 1 < puzzleKeyPositions.size) {
                val keyTileX = puzzleKeyPositions[puzzleId - 1][0]
                val keyTileY = puzzleKeyPositions[puzzleId - 1][1]
                entities.add(Key(refLink, keyTileX.toFloat() * Tile.TILE_WIDTH,
                    keyTileY.toFloat() * Tile.TILE_HEIGHT, Assets.keyImage, puzzleId))
            }
        }
    }

    fun onPuzzleFailure() {
        player.takeDamage(20)

        if (player.health <= 0) {
            refLink.setState(GameOverState(refLink))
        } else {
            player.setPosition(2f * Tile.TILE_WIDTH, 26f * Tile.TILE_HEIGHT)
        }
    }

    // ==================== INVENTORY MANAGEMENT ====================
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

    // ==================== OBJECTIVES & MESSAGES ====================
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

    fun setCaveEntranceUnlocked(unlocked: Boolean) {
        caveEntranceUnlocked = unlocked
    }

    // ==================== SAVE/LOAD ====================
    fun saveCurrentState() {
        // Convertim array-ul de puzzle-uri în String pentru salvare
        val solvedPuzzlesString = puzzlesSolved
            .indices
            .filter { puzzlesSolved[it] && it in 1..TOTAL_PUZZLES_LEVEL2 }
            .joinToString(",")

        // --- FIX: Apelăm saveGameData cu TOȚI parametrii ceruți de noul DatabaseManager ---
        refLink.databaseManager.saveGameData(
            levelIndex = currentLevelIndex,
            score = 0, // Poți implementa un sistem de scor mai târziu
            playerX = player.x,
            playerY = player.y,
            playerHealth = player.health,
            hasKey = hasLevelKey,
            hasDoorKeys = hasDoorKeys,
            puzzlesSolvedString = solvedPuzzlesString
        )
    }

    // ==================== GETTERS ====================
    fun getMap() = currentMap
    fun getEntities() = entities

    // ==================== RENDERING ====================
    override fun render(batch: SpriteBatch) {
        val camera = refLink.gameCamera

        // Render map
        batch.begin()
        currentMap.render(batch, camera)
        batch.end()

        // Prepare entities for rendering
        val allEntities = entities.toMutableList()
        allEntities.add(player)
        finalBoss?.let { if (it.health > 0) allEntities.add(it) }
        allEntities.sortBy { it.y }

        // Render entities
        batch.projectionMatrix = camera.combined
        batch.begin()
        for (entity in allEntities) {
            entity.render(batch)
        }
        batch.end()

        // Render fog of war (handles its own batch state)
        fogOfWar?.render(batch, camera)

        // Render UI (handles its own batch state)
        renderUI(batch)
    }

    private fun renderUI(batch: SpriteBatch) {
        // Set up orthographic projection for UI
        batch.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        // Draw health bar (uses ShapeRenderer, needs batch ended)
        if (batch.isDrawing) {
            batch.end()
        }

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Health bar background
        val barWidth = 150f
        val barHeight = 20f
        val barX = 10f
        val barY = Gdx.graphics.height - 30f

        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.rect(barX, barY, barWidth, barHeight)

        // Health bar foreground
        val currentHealthWidth = (player.health.toFloat() / 100f) * barWidth
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(barX, barY, currentHealthWidth, barHeight)
        shapeRenderer.end()

        // Health bar border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(barX, barY, barWidth, barHeight)
        shapeRenderer.end()

        // Start batch for text rendering
        batch.begin()

        // Health text
        font.color = Color.WHITE
        val healthText = "HP: ${player.health}/100"
        font.draw(batch, healthText, barX + 5f, barY + 15f)

        // Collection message
        collectionMessage?.let { msg ->
            font.color = Color.WHITE
            val layout = font.draw(batch, msg, 0f, 0f)
            val x = (Gdx.graphics.width - layout.width) / 2
            val y = Gdx.graphics.height / 2f
            font.draw(batch, msg, x, y)
        }

        // Objective text
        if (isObjectiveDisplayed) {
            font.color = Color.YELLOW
            val objectiveText = "Obiectiv: $currentObjective"
            val layout = font.draw(batch, objectiveText, 0f, 0f)
            val x = (Gdx.graphics.width - layout.width) / 2
            font.draw(batch, objectiveText, x, 30f)
        }

        batch.end()

        // Wood sign message (needs ShapeRenderer for background)
        woodSignMessage?.let { msg ->
            shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
            shapeRenderer.rect(
                Gdx.graphics.width / 2f - 250f,
                Gdx.graphics.height / 2f - 75f,
                500f, 150f
            )
            shapeRenderer.end()

            batch.begin()
            font.color = Color.WHITE
            val layout = font.draw(batch, msg, 0f, 0f)
            val x = (Gdx.graphics.width - layout.width) / 2
            val y = Gdx.graphics.height / 2f
            font.draw(batch, msg, x, y)

            val instruction = "Apasă 'E' pentru a închide."
            val instrLayout = font.draw(batch, instruction, 0f, 0f)
            val instrX = (Gdx.graphics.width - instrLayout.width) / 2
            font.draw(batch, instruction, instrX, y - 50f)
            batch.end()
        }

        // Draw minimap
        drawMiniMap(batch)
    }

    private fun drawHealthBar(batch: SpriteBatch) {
        val barWidth = 150f
        val barHeight = 20f
        val x = 10f
        val y = Gdx.graphics.height - 30f

        batch.end()
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
        batch.begin()

        font.color = Color.WHITE
        val healthText = "HP: ${player.health}/100"
        font.draw(batch, healthText, x + 5f, y + 15f)
    }

    private fun drawMiniMap(batch: SpriteBatch) {
        val miniMapHeight = 150f
        val mapPixelWidth = currentMap.width * Tile.TILE_WIDTH
        val mapPixelHeight = currentMap.height * Tile.TILE_HEIGHT
        val miniMapWidth = (mapPixelWidth.toFloat() / mapPixelHeight) * miniMapHeight
        val padding = 10f
        val miniMapX = Gdx.graphics.width - miniMapWidth - padding
        val miniMapY = padding

        // Ensure batch is ended before using ShapeRenderer
        if (batch.isDrawing) {
            batch.end()
        }

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        // Minimap background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
        shapeRenderer.rect(miniMapX, miniMapY, miniMapWidth, miniMapHeight)
        shapeRenderer.end()

        // Minimap border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(miniMapX, miniMapY, miniMapWidth, miniMapHeight)
        shapeRenderer.end()

        val mapScaleX = miniMapWidth / mapPixelWidth
        val mapScaleY = miniMapHeight / mapPixelHeight

        // Draw map tiles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (yTile in 0 until currentMap.height) {
            for (xTile in 0 until currentMap.width) {
                val tile = currentMap.getTile(xTile, yTile)
                if (tile.isSolid) {
                    shapeRenderer.color = when (currentLevelIndex) {
                        0 -> Color(0.13f, 0.55f, 0.13f, 1f)
                        1 -> Color(0.4f, 0.4f, 0.4f, 1f)
                        else -> Color(0.34f, 0.2f, 0.14f, 1f)
                    }
                    shapeRenderer.rect(
                        miniMapX + xTile * Tile.TILE_WIDTH * mapScaleX,
                        miniMapY + yTile * Tile.TILE_HEIGHT * mapScaleY,
                        Tile.TILE_WIDTH * mapScaleX,
                        Tile.TILE_HEIGHT * mapScaleY
                    )
                }
            }
        }

        // Draw collectibles
        for (entity in entities) {
            if (entity is Key || entity is Talisman) {
                shapeRenderer.color = Color.YELLOW
                val entityMiniMapX = miniMapX + entity.x * mapScaleX
                val entityMiniMapY = miniMapY + entity.y * mapScaleY
                shapeRenderer.circle(entityMiniMapX, entityMiniMapY, 5f)
            }
        }

        // Draw player
        shapeRenderer.color = Color.CYAN
        val playerMiniMapX = miniMapX + player.x * mapScaleX
        val playerMiniMapY = miniMapY + player.y * mapScaleY
        shapeRenderer.circle(playerMiniMapX, playerMiniMapY, 5f)

        shapeRenderer.end()
    }

    override fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
    }
}
