package com.lostexpedition.game.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.lostexpedition.game.entities.*
import com.lostexpedition.game.graphics.Assets
import com.lostexpedition.game.map.FogOfWar
import com.lostexpedition.game.map.Map
import com.lostexpedition.game.tiles.Tile
import com.lostexpedition.game.tiles.TileConstants
import com.lostexpedition.game.utils.PlayerData
import com.lostexpedition.game.utils.RefLinks
import com.lostexpedition.game.utils.SoundManager
import kotlin.math.abs

class GameState(
    refLink: RefLinks,
    private var startLevel: Int = 0,
    private val isLoadingFromSave: Boolean = false
) : State(refLink) {

    companion object {
        private const val TOTAL_PUZZLES_LEVEL2 = 5
        private const val MESSAGE_DURATION_MS = 2000L
        private const val ANIMAL_DAMAGE_COOLDOWN_MS = 4000L
        private const val TRAP_DAMAGE_COOLDOWN_MS = 2000L
        private const val GLOBAL_ACTIVATION_DELAY_MS = 1000L
        private const val AMBIENCE_FULL = 2.5f     // dale: animal la volum maxim
        private const val AMBIENCE_SILENT = 8f     // dale: animal inaudibil
        private const val AMBIENCE_FADE_SPEED = 2.5f
        private const val OBJECTIVE_HOLD = 1.5f  // secunde în centrul ecranului
        private const val OBJECTIVE_MOVE = 0.5f  // secunde pentru urcarea spre partea de sus
        private const val DOOR_LEFT_POST = 24f
        private const val DOOR_RIGHT_POST = 12f
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
    // Ușile deja deschise (0..5 = ușile din nivelul 2, 6 = ușa finală din nivelul 3).
    // Se salvează, ca la reîncărcare harta să fie readusă în aceeași stare.
    var doorsOpened = BooleanArray(7) { false }
    private var wordPuzzleSolved = false
    private var hasTalisman = false
    private var caveEntranceUnlocked = false
    private var currentObjective = "Find the key and the Moon talisman."
    private var isObjectiveDisplayed = false

    private val entities = mutableListOf<Entity>()
    var puzzlesSolved = BooleanArray(TOTAL_PUZZLES_LEVEL2 + 1) { false }
    private var caveGuardianNPC: NPC? = null
    private var caveEntrance: CaveEntrance? = null

    private var collectionMessage: String? = null
    private var collectionMessageTime = 0L
    // >= 0: animația obiectivului citit de pe indicator (centru -> sus) e în desfășurare
    private var objectiveRevealTime = -1f

    // Ceas de joc (ms) avansat doar din update(delta): se oprește cât timp jocul e pe pauză,
    // în puzzle sau aplicația e în fundal, spre deosebire de System.currentTimeMillis().
    private var gameTimeMs = 0L

    // Setat de handler-ele de nivel când butonul de interacțiune a fost folosit în acest frame
    // (NPC, intrare peșteră, ușă), ca același tap să nu comute și bannerul de obiectiv.
    private var interactConsumed = false

    // Pornesc "în trecut" ca prima lovitură să nu aștepte un cooldown întreg de la începutul nivelului.
    private var lastAnimalDamageTime = -ANIMAL_DAMAGE_COOLDOWN_MS
    private var lastTrapDamageTime = -TRAP_DAMAGE_COOLDOWN_MS
    private var lastAgentTrapDamageTime = -TRAP_DAMAGE_COOLDOWN_MS

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

    // Zone solide care nu sunt dale întregi (ex. stâlpii ușilor din nivelul 2), verificate de Player.
    private val movementBlockers = mutableListOf<Rectangle>()
    fun getMovementBlockers(): List<Rectangle> = movementBlockers

    private val puzzleDoorPositions = arrayOf(
        intArrayOf(19, 24, 20, 24, 19, 25, 20, 25),
        intArrayOf(36, 18, 37, 18, 36, 19, 37, 19),
        intArrayOf(53, 24, 54, 24, 53, 25, 54, 25),
        intArrayOf(70, 15, 71, 15, 70, 16, 71, 16),
        intArrayOf(87, 24, 88, 24, 87, 25, 88, 25),
        intArrayOf(110, 15, 111, 15, 110, 16, 111, 16)
    )

    private lateinit var font: BitmapFont
    private val shapeRenderer = ShapeRenderer()
    private val currentZoom = 0.9f

    // Paleta comună pentru tot HUD-ul (health bar, cluster dreapta-sus, overlay minihartă)
    // ca toate elementele să arate ca un singur sistem vizual.
    private val hudPanelColor = Color(0f, 0f, 0f, 0.55f)
    private val hudPanelColorActive = Color(0f, 0f, 0f, 0.8f)
    private val hudBorderColor = Color(1f, 1f, 1f, 0.85f)

    // Bounds pentru clusterul de butoane din dreapta sus (minihartă / pauză / setări)
    private val mapButtonBounds = Rectangle()
    private val pauseButtonBounds = Rectangle()
    private val settingsButtonBounds = Rectangle()
    private var showMiniMapOverlay = false

    // Terenul solid al minihărții, copt o singură dată per nivel (vezi bakeMiniMapTerrainTexture()),
    // ca overlay-ul să nu mai reparcurgă toată harta în fiecare frame cât timp e deschis.
    private var miniMapTerrainTexture: Texture? = null

    init {
        refLink.gameState = this

        try {
            val generator = FreeTypeFontGenerator(Gdx.files.internal("font.ttf"))
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()

            parameter.size = 40
            parameter.borderWidth = 3f
            parameter.borderColor = Color.BLACK
            parameter.color = Color.WHITE
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ăâîșțĂÂÎȘȚ"
            parameter.minFilter = Texture.TextureFilter.Linear
            parameter.magFilter = Texture.TextureFilter.Linear

            font = generator.generateFont(parameter)
            generator.dispose()

        } catch (e: Exception) {
            Gdx.app.error("FontLoader", "ATENTIE: Nu s-a gasit 'font.ttf'! Se foloseste fontul default.")
            font = BitmapFont()
            font.data.setScale(2f)
        }

        initLevelInternal(currentLevelIndex, isLoadingFromSave)

        refLink.gameCamera.zoom = currentZoom
        refLink.gameCamera.update()
    }

    private fun initLevelInternal(desiredLevelIndex: Int, loadPlayerStateFromDb: Boolean) {
        var playerStartX = 100f
        var playerStartY = 100f
        var loadedHealth = 100
        var loadedData: PlayerData? = null

        currentLevelIndex = if (desiredLevelIndex in levelPaths.indices) desiredLevelIndex else 0

        // Stabilim ÎNTÂI care e nivelul corect (citind salvarea, dacă e cazul) și abia
        // APOI creăm Map-ul, ca să nu încărcăm harta greșită când levelIndex din DB
        // diferă de desiredLevelIndex.
        if (loadPlayerStateFromDb) {
            val loadedDataList = refLink.databaseManager.loadGameData()
            if (loadedDataList.isNotEmpty()) {
                loadedData = loadedDataList[0]
                currentLevelIndex = loadedData.levelIndex
            } else {
                resetToDefaults()
            }
        } else {
            currentLevelIndex = desiredLevelIndex
        }

        currentMap = Map(refLink, levelPaths[currentLevelIndex], currentLevelIndex)
        refLink.map = currentMap

        fogOfWar = FogOfWar(refLink, currentMap.width, currentMap.height)
        bakeMiniMapTerrainTexture()

        val TS = TileConstants.TILE_SIZE

        if (loadedData != null) {
            val data = loadedData
            playerStartX = data.playerX
            playerStartY = data.playerY
            loadedHealth = data.playerHealth
            hasLevelKey = data.hasKey
            hasDoorKeys = data.hasDoorKeys
            hasTalisman = data.hasTalisman
            caveEntranceUnlocked = data.caveEntranceUnlocked
            bossDefeated = data.bossDefeated
            doorsOpened = data.doorsOpened
            wordPuzzleSolved = data.wordPuzzleSolved

            if (data.puzzlesSolvedString.isNotEmpty()) {
                data.puzzlesSolvedString.split(",").forEach { idString ->
                    val id = idString.toIntOrNull()
                    if (id != null && id in 1..TOTAL_PUZZLES_LEVEL2) {
                        puzzlesSolved[id] = true
                    }
                }
            }
            isObjectiveDisplayed = true
        } else if (!loadPlayerStateFromDb) {
            when (currentLevelIndex) {
                0 -> {
                    playerStartX = 2f * TS
                    playerStartY = topDownY(2)
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

        refLink.gameCamera.position.set(player.x, player.y, 0f)
        refLink.gameCamera.update()

        val mapWidth = currentMap.width * TileConstants.TILE_SIZE
        val mapHeight = currentMap.height * TileConstants.TILE_SIZE

        val camX = MathUtils.clamp(
            refLink.gameCamera.position.x,
            refLink.gameCamera.viewportWidth / 2f,
            mapWidth - refLink.gameCamera.viewportWidth / 2f
        )
        val camY = MathUtils.clamp(
            refLink.gameCamera.position.y,
            refLink.gameCamera.viewportHeight / 2f,
            mapHeight - refLink.gameCamera.viewportHeight / 2f
        )
        refLink.gameCamera.position.set(camX, camY, 0f)

        entities.clear()
        loadLevelEntities()
        restoreOpenedDoors()
        updateObjective()

        // Restaurăm starea finalChest (relevantă doar pe nivelul 3) după ce entitățile
        // nivelului au fost create.
        if (loadedData != null) {
            finalChest?.setCanInteract(loadedData.finalChestCanInteract)
        }

        // Muzica nivelului curent (music_level1.ogg / music_level2.ogg / music_level3.ogg)
        SoundManager.playMusic(SoundManager.musicForLevel(currentLevelIndex))
    }

    private fun resetToDefaults() {
        currentLevelIndex = 0
        hasLevelKey = false
        hasDoorKeys = BooleanArray(7) { false }
        puzzlesSolved = BooleanArray(TOTAL_PUZZLES_LEVEL2 + 1) { false }
        hasTalisman = false
        caveEntranceUnlocked = false
        doorsOpened = BooleanArray(7) { false }
        wordPuzzleSolved = false
        bossDefeated = false
    }

    private fun loadLevelEntities() {
        movementBlockers.clear()
        when (currentLevelIndex) {
            0 -> loadLevel1Entities()
            1 -> loadLevel2Entities()
            2 -> loadLevel3Entities()
        }
    }

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

    private fun loadLevel1Entities() {
        val TS = TileConstants.TILE_SIZE

        entities.add(Animal(refLink, 53f * TS, topDownY(5), 51f * TS, 56f * TS, Animal.AnimalType.JAGUAR))
        entities.add(Animal(refLink, 10f * TS, topDownY(36), 8f * TS, 11f * TS, Animal.AnimalType.MONKEY))
        entities.add(Animal(refLink, 89f * TS, topDownY(29), 88f * TS, 91f * TS, Animal.AnimalType.MONKEY))
        entities.add(Animal(refLink, 84f * TS, topDownY(57), 82f * TS, 85f * TS, Animal.AnimalType.BAT))

        // Adăugăm capcanele și le activăm
        val trap1 = Trap(refLink, 66f * TS, topDownY(31), TextureRegion(Assets.spikeTrapImage))
        trap1.setActive(true)
        entities.add(trap1)

        val trap2 = Trap(refLink, 67f * TS, topDownY(38), TextureRegion(Assets.spikeTrapImage))
        trap2.setActive(true)
        entities.add(trap2)

        val trap3 = Trap(refLink, 66f * TS, topDownY(45), TextureRegion(Assets.spikeTrapImage))
        trap3.setActive(true)
        entities.add(trap3)

        caveGuardianNPC = NPC(refLink, 93f * TS, topDownY(92))
        entities.add(caveGuardianNPC!!)

        // După ce a fost predat NPC-ului (intrarea deblocată), talismanul nu mai reapare.
        if (!hasTalisman && !caveEntranceUnlocked) {
            entities.add(Talisman(refLink, 45f * TS, topDownY(52), TextureRegion(Assets.talismanImage)))
        }

        caveEntrance = CaveEntrance(refLink, 91f * TS, topDownY(88), (TS * 2).toInt(), (TS * 2).toInt())
        entities.add(caveEntrance!!)

        if (!hasDoorKeys[0]) {
            entities.add(Key(refLink, 12f * TS, topDownY(85), Assets.keyImage, 0))
        }

        val woodSign1 = DecorativeObject(
            refLink, 2f * TS, topDownY(1), 64, 64,
            TextureRegion(Assets.woodSignImage), true
        )
        woodSign1.setDialogueMessage("Find the key and the Moon talisman.")
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
                    TextureRegion(Assets.puzzleTableImage), true, isSolid = true
                )
            )
        }

        if (!isPuzzleSolved(1)) entities.add(PuzzleTrigger(refLink, 19f * TS, topDownY(20), TS.toInt(), TS.toInt(), 1))
        if (!isPuzzleSolved(2)) entities.add(PuzzleTrigger(refLink, 36f * TS, topDownY(14), TS.toInt(), TS.toInt(), 2))
        if (!isPuzzleSolved(3)) entities.add(PuzzleTrigger(refLink, 53f * TS, topDownY(20), TS.toInt(), TS.toInt(), 3))
        if (!isPuzzleSolved(4)) entities.add(PuzzleTrigger(refLink, 70f * TS, topDownY(11), TS.toInt(), TS.toInt(), 4))
        if (!isPuzzleSolved(5)) entities.add(PuzzleTrigger(refLink, 87f * TS, topDownY(20), TS.toInt(), TS.toInt(), 5))

        entities.add(LevelExit(refLink, 110f * TS, topDownY(14), (TS * 2).toInt(), TS.toInt()))

        // Stâlpii ușilor: prin ușa deschisă se trece doar pe mijloc. În stânga e canatul ușii
        // (~24px), în dreapta tocul (~12px), pe ambele rânduri ale ușii (arcul + partea de jos).
        for (door in puzzleDoorPositions) {
            val left = door[0] * TS
            val right = (door[2] + 1) * TS
            val bottom = topDownY(door[5])
            val h = TS * 2
            movementBlockers.add(Rectangle(left, bottom, DOOR_LEFT_POST, h))
            movementBlockers.add(Rectangle(right - DOOR_RIGHT_POST, bottom, DOOR_RIGHT_POST, h))
        }

        // Cheia unui puzzle rezolvat reapare doar dacă nu a fost încă ridicată și nici folosită.
        for (i in 1..TOTAL_PUZZLES_LEVEL2) {
            if (isPuzzleSolved(i) && !hasDoorKeys[i] && !doorsOpened[i]) {
                val keyTileX = puzzleKeyPositions[i - 1][0]
                val keyTileY = puzzleKeyPositions[i - 1][1]
                entities.add(Key(refLink, keyTileX * TS, topDownY(keyTileY), Assets.keyImage, i))
            }
        }

        val woodSign2 = DecorativeObject(
            refLink, 3f * TS, topDownY(25), 64, 64,
            TextureRegion(Assets.woodSignImage), true
        )
        woodSign2.setDialogueMessage("Solve the puzzles to open the doors.")
        entities.add(woodSign2)
    }

    private fun loadLevel3Entities() {
        val TS = TileConstants.TILE_SIZE
        arenaTraps.clear()

        entities.add(
            DecorativeObject(
                refLink, 75f * TS, topDownY(26), TS.toInt(), TS.toInt(),
                TextureRegion(Assets.puzzleTableImage), true, isSolid = true
            )
        )

        if (!wordPuzzleSolved) {
            entities.add(PuzzleTrigger(refLink, 75f * TS, topDownY(26), TS.toInt(), TS.toInt(), 99))
        } else if (!hasDoorKeys[6] && !doorsOpened[6]) {
            spawnFinalKey()
        }

        finalChest = Chest(refLink, 37f * TS, topDownY(3), TS.toInt(), TS.toInt())
        finalChest?.setCanInteract(false)
        entities.add(finalChest!!)

        // Capcanele se citesc din layer-ul "traps" al hărții: fiecare dală cu țepi desenată în Tiled
        // devine o capcană de mărimea unei dale, exact pe poziția ei. Layer-ul în sine nu se mai
        // desenează (îl desenează entitățile Trap: găuri când sunt inactive, țepi când se activează).
        // Aceleași dale sunt și zona de declanșare a arenei.
        val trapLayer = currentMap.tiledMap.layers.get("traps") as? com.badlogic.gdx.maps.tiled.TiledMapTileLayer
        if (trapLayer != null) {
            trapLayer.isVisible = false
            for (ty in 0 until trapLayer.height) {
                for (tx in 0 until trapLayer.width) {
                    if (trapLayer.getCell(tx, ty)?.tile == null) continue
                    val trap = Trap(
                        refLink, tx * TS, ty * TS, null, TS.toInt(),
                        inactiveImage = Assets.trapDisabled,
                        riseFrames = Assets.trapRiseFrames
                    )
                    entities.add(trap)
                    arenaTraps.add(trap)
                    entities.add(TrapTrigger(refLink, tx * TS, ty * TS, TS.toInt(), TS.toInt()))
                }
            }
        }

        finalBoss = null
        if (!bossDefeated) {
            finalBoss = Agent(refLink, 36f * TS, topDownY(22), 36f * TS, 43f * TS, true)
            entities.add(finalBoss!!)
        }

        val woodSign3 = DecorativeObject(
            refLink, 37f * TS, topDownY(56), 64, 64,
            TextureRegion(Assets.woodSignImage), true
        )
        woodSign3.setDialogueMessage("Defeat the Agent and claim the treasure.")
        entities.add(woodSign3)
    }

    override fun update(delta: Float) {
        gameTimeMs += (delta * 1000f).toLong()
        interactConsumed = false

        if (collectionMessage != null &&
            gameTimeMs - collectionMessageTime > MESSAGE_DURATION_MS
        ) {
            collectionMessage = null
        }

        // Tasta/gestul BACK (Android) declanșează exact același flux ca butonul de pauză.
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            saveCurrentState()
            refLink.setState(PauseState(refLink))
            return
        }

        // ✅ CHECK CLUSTER BUTOANE HUD (minihartă / pauză / setări)
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat() // Inversăm Y pentru UI

            if (mapButtonBounds.contains(touchX, touchY)) {
                SoundManager.click()
                showMiniMapOverlay = !showMiniMapOverlay
            } else if (pauseButtonBounds.contains(touchX, touchY)) {
                SoundManager.click()
                // Salvăm jocul înainte de pauză pentru siguranță
                saveCurrentState()
                refLink.setState(PauseState(refLink))
                return // Oprim update-ul curent
            } else if (settingsButtonBounds.contains(touchX, touchY)) {
                SoundManager.click()
                saveCurrentState()
                refLink.setState(SettingsState(refLink))
                return // Oprim update-ul curent
            } else if (showMiniMapOverlay) {
                // Orice atingere în afara clusterului închide overlay-ul de minihartă
                showMiniMapOverlay = false
            }
        }

        refLink.touchController.update()
        player.update()

        fogOfWar?.update()

        if (player.health <= 0) {
            refLink.setState(GameOverState(refLink))
            return
        }

        val gameCamera = refLink.gameCamera
        gameCamera.position.x = player.x + player.width / 2
        gameCamera.position.y = player.y + player.height / 2

        val mapWidthPixels = currentMap.width * TileConstants.TILE_SIZE.toFloat()
        val mapHeightPixels = currentMap.height * TileConstants.TILE_SIZE.toFloat()

        val effectiveViewportWidth = gameCamera.viewportWidth * currentZoom
        val effectiveViewportHeight = gameCamera.viewportHeight * currentZoom

        gameCamera.position.x = MathUtils.clamp(
            gameCamera.position.x,
            effectiveViewportWidth / 2f,
            mapWidthPixels - effectiveViewportWidth / 2f
        )
        gameCamera.position.y = MathUtils.clamp(
            gameCamera.position.y,
            effectiveViewportHeight / 2f,
            mapHeightPixels - effectiveViewportHeight / 2f
        )
        gameCamera.update()

        updateLevelSpecificLogic(delta)
        if (State.currentState !== this) return // am trecut în altă stare în acest frame

        // Rulează DUPĂ logica de nivel: dacă tap-ul a fost folosit de un NPC/ușă/intrare,
        // nu mai deschidem și panoul de obiectiv/indicatorul.
        // Indicatorul (wood sign): obiectivul apare mare în centrul ecranului, apoi urcă sus
        // și rămâne acolo (vezi drawObjective()).
        if (!interactConsumed && objectiveRevealTime < 0f &&
            (refLink.touchController.isInteractJustPressed || Gdx.input.isKeyJustPressed(Input.Keys.E)) &&
            nearbyWoodSign() != null
        ) {
            objectiveRevealTime = 0f
            isObjectiveDisplayed = false
        }
        if (objectiveRevealTime >= 0f) {
            objectiveRevealTime += delta
            if (objectiveRevealTime >= OBJECTIVE_HOLD + OBJECTIVE_MOVE) {
                objectiveRevealTime = -1f
                isObjectiveDisplayed = true
            }
        }

        updateEntities(delta)
        updateAnimalAmbience(delta)

        // Butonul E e activ doar lângă ceva cu care se poate interacționa (pentru frame-ul următor).
        refLink.touchController.isInteractEnabled = hasInteractionTarget()
    }

    // Volumul curent al fiecărui animal (se apropie lin de ținta calculată din distanță = fade in/out)
    private val animalVolumes = HashMap<Animal, Float>()

    private fun updateAnimalAmbience(delta: Float) {
        val TS = TileConstants.TILE_SIZE
        val px = player.x + player.width / 2f
        val py = player.y + player.height / 2f
        for (e in entities) {
            if (e !is Animal) continue
            val (sound, maxVolume) = when (e.type) {
                Animal.AnimalType.JAGUAR -> SoundManager.AMB_JAGUAR to 0.9f
                Animal.AnimalType.MONKEY -> SoundManager.AMB_MONKEY to 0.7f
                Animal.AnimalType.BAT -> SoundManager.AMB_BAT to 0.5f
            }
            val distTiles = com.badlogic.gdx.math.Vector2.dst(px, py, e.x + e.width / 2f, e.y + e.height / 2f) / TS
            // Volum maxim sub AMBIENCE_FULL dale, nimic peste AMBIENCE_SILENT dale, liniar între ele.
            val closeness = ((AMBIENCE_SILENT - distTiles) / (AMBIENCE_SILENT - AMBIENCE_FULL)).coerceIn(0f, 1f)
            val target = closeness * maxVolume
            val current = animalVolumes[e] ?: 0f
            val next = current + (target - current) * (delta * AMBIENCE_FADE_SPEED).coerceAtMost(1f)
            animalVolumes[e] = next
            SoundManager.setLoopVolume("animal_${System.identityHashCode(e)}", sound, next)
        }
    }

    private fun nearbyWoodSign(): DecorativeObject? = entities.firstOrNull {
        it is DecorativeObject && it.getDialogueMessage() != null &&
            com.badlogic.gdx.math.Vector2.dst(player.x, player.y, it.x, it.y) < 100f
    } as DecorativeObject?

    /** Există lângă jucător un indicator, o ușă închisă, o masă de puzzle, NPC-ul, intrarea sau cufărul? */
    private fun hasInteractionTarget(): Boolean {
        if (nearbyWoodSign() != null) return true
        val p = player.bounds.toRectangle()
        val TS = TileConstants.TILE_SIZE
        for (e in entities) {
            when (e) {
                is PuzzleTrigger ->
                    if (Rectangle(e.x - 30f, e.y - 30f, e.width + 60f, e.height + 60f).overlaps(p)) return true
                is NPC, is CaveEntrance ->
                    if (e.bounds.overlaps(player.bounds)) return true
                is Chest ->
                    if (e.canInteract() && Rectangle(e.x - 20f, e.y - 20f, e.width + 40f, e.height + 40f).overlaps(p)) return true
                else -> {}
            }
        }
        val tx = getPlayerTileX()
        val ty = getPlayerTileY()
        when (currentLevelIndex) {
            1 -> for (door in puzzleDoorPositions) {
                if (abs(tx - door[0]) <= 2 && abs(ty - door[1]) <= 2 && getTileJava(door[0], door[1]).isSolid) return true
            }
            2 -> if (abs(tx - 39) <= 2 && abs(ty - 6) <= 2 && getTileJava(39, 6).isSolid) return true
        }
        return false
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
                if (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractJustPressed) {
                    interactConsumed = true
                    if (hasTalisman) {
                        caveEntranceUnlocked = true
                        removeTalismanFromInventory()
                    } else if (!caveEntranceUnlocked) {
                        collectionMessage = "I don't have the talisman yet!"
                        collectionMessageTime = gameTimeMs
                    }
                }
            }
        }

        caveEntrance?.let { entrance ->
            if (player.bounds.overlaps(entrance.bounds) &&
                (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractJustPressed)
            ) {
                interactConsumed = true
                if (caveEntranceUnlocked && hasDoorKeys[0]) {
                    passToLevel2()
                } else if (!caveEntranceUnlocked) {
                    collectionMessage = "The entrance is blocked."
                    collectionMessageTime = gameTimeMs
                } else if (!hasDoorKeys[0]) {
                    collectionMessage = "You need a key!"
                    collectionMessageTime = gameTimeMs
                }
            }
        }
    }

    private fun updateLevel2Logic() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractJustPressed) {
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

        // NOTA: WordPuzzleState se deschide prin PuzzleTrigger(99) de la masa (75, 26),
        // exact ca in varianta Java. Nu mai deschidem si de aici, altfel cele doua stari
        // se suprapun si cheia finala nu se mai genereaza.

        if (!trapsTriggered) {
            for (entity in entities) {
                if (entity is TrapTrigger && entity.bounds.overlaps(player.bounds)) {
                    trapsTriggered = true
                    trapActivationTime = gameTimeMs
                    break
                }
            }
        }

        if (trapsTriggered &&
            gameTimeMs - trapActivationTime >= GLOBAL_ACTIVATION_DELAY_MS
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

            if (boss.isDefeated() && !bossDefeated) {   // MODIFICAT
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
            if (entity is Agent && entity.isDefeated()) continue   // NOU
            entity.update()

            when (entity) {
                is Key -> {
                    if (entity.bounds.overlaps(player.bounds)) {
                        val associatedId = entity.associatedPuzzleId
                        if (associatedId in hasDoorKeys.indices) {
                            hasDoorKeys[associatedId] = true
                            collectionMessage = "Key collected!"
                            collectionMessageTime = gameTimeMs
                            SoundManager.playSfx(SoundManager.SFX_KEY)
                        }
                        iterator.remove()
                    }
                }
                is Talisman -> {
                    if (entity.bounds.overlaps(player.bounds)) {
                        hasTalisman = true
                        collectionMessage = "Talisman colectat!"
                        collectionMessageTime = gameTimeMs
                        SoundManager.playSfx(SoundManager.SFX_KEY)
                        iterator.remove()
                    }
                }
                is Animal -> {
                    if (player.bounds.overlaps(entity.bounds)) {
                        playerInContactWithAnimal = true
                    }
                }
                is Trap -> {
                    if (entity.isActive()) {
                        if (player.bounds.overlaps(entity.bounds)) {
                            if (gameTimeMs - lastTrapDamageTime >= TRAP_DAMAGE_COOLDOWN_MS) {
                                player.takeDamage(30)
                                lastTrapDamageTime = gameTimeMs
                                SoundManager.playSfx(SoundManager.SFX_TRAP)
                            }
                        }
                        finalBoss?.let { boss ->
                            if (boss.bounds.overlaps(entity.bounds)) {
                                if (gameTimeMs - lastAgentTrapDamageTime >= TRAP_DAMAGE_COOLDOWN_MS) {
                                    boss.takeDamage(20)
                                    lastAgentTrapDamageTime = gameTimeMs
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
            if (gameTimeMs - lastAnimalDamageTime >= ANIMAL_DAMAGE_COOLDOWN_MS) {
                for (entity in entities) {
                    if (entity is Animal && player.bounds.overlaps(entity.bounds)) {
                        player.takeDamage(entity.damage)
                        break
                    }
                }
                lastAnimalDamageTime = gameTimeMs
            }
        }
    }

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
                    interactConsumed = true
                    if (hasDoorKeys[i]) {
                        openDoor(i)
                        return
                    } else {
                        collectionMessage = "The door is locked!"
                        collectionMessageTime = gameTimeMs
                        return
                    }
                }
            }
        }
    }

    private fun openDoor(doorIndex: Int) {
        if (doorIndex in hasDoorKeys.indices && hasDoorKeys[doorIndex]) {
            if (applyDoorOpenTiles(doorIndex)) {
                doorsOpened[doorIndex] = true
                hasDoorKeys[doorIndex] = false
                collectionMessage = "The door opened!"
                collectionMessageTime = gameTimeMs
                SoundManager.playSfx(SoundManager.SFX_DOOR)
            }
        }
    }

    /** Înlocuiește cele 4 dale ale ușii [doorIndex] (nivelul 2) cu cele ale ușii deschise. */
    private fun applyDoorOpenTiles(doorIndex: Int): Boolean {
        val doorCoords = puzzleDoorPositions[doorIndex]
        if (doorCoords.size != 8) return false

        // GID-urile ușii deschise (ID-ul din Tiled + 1): stânga-sus, dreapta-sus, stânga-jos, dreapta-jos
        val openTopLeft = 60
        val openTopRight = 61
        val openBotLeft = 92
        val openBotRight = 93

        changeTileGidJava(doorCoords[0], doorCoords[1], openTopLeft, 1)
        changeTileGidJava(doorCoords[2], doorCoords[3], openTopRight, 1)
        changeTileGidJava(doorCoords[4], doorCoords[5], openBotLeft, 1)
        changeTileGidJava(doorCoords[6], doorCoords[7], openBotRight, 1)
        return true
    }

    private fun applyFinalDoorOpenTiles() {
        val layerIndex = 2
        changeTileGidJava(39, 6, 74, layerIndex)
        changeTileGidJava(40, 6, 75, layerIndex)
        changeTileGidJava(39, 7, 120, layerIndex)
        changeTileGidJava(40, 7, 121, layerIndex)
    }

    /** La (re)încărcarea unui nivel, redeschide pe hartă ușile care fuseseră deja deschise. */
    private fun restoreOpenedDoors() {
        when (currentLevelIndex) {
            1 -> for (i in puzzleDoorPositions.indices) {
                if (doorsOpened[i]) applyDoorOpenTiles(i)
            }
            2 -> if (doorsOpened[6]) applyFinalDoorOpenTiles()
        }
    }

    private fun handleFinalDoorInteraction() {
        val doorTileX = 39
        val doorTileY = 6

        if (!getTileJava(doorTileX, doorTileY).isSolid) {
            return
        }

        val playerTileX = getPlayerTileX()
        val playerTileY = getPlayerTileY()

        if (abs(playerTileX - doorTileX) <= 2 && abs(playerTileY - doorTileY) <= 2) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || refLink.touchController.isInteractJustPressed) {
                interactConsumed = true
                if (hasDoorKeys.size > 6 && hasDoorKeys[6]) {
                    openFinalDoor()
                } else {
                    collectionMessage = "The door is locked."
                    collectionMessageTime = gameTimeMs
                }
            }
        }
    }

    private fun openFinalDoor() {
        applyFinalDoorOpenTiles()
        doorsOpened[6] = true

        if (hasDoorKeys.size > 6) {
            hasDoorKeys[6] = false
        }
        collectionMessage = "The door is unlocked!"
        collectionMessageTime = gameTimeMs
        SoundManager.playSfx(SoundManager.SFX_DOOR)
    }

    private fun passToLevel2() {
        println("Trecere la Nivelul 2!")

        // 1. Memorăm viața (matricea de chei se păstrează automat în clasă)
        val currentHealth = player.health

        // 2. Încărcăm harta nouă fără a distruge GameState-ul curent
        // Dispunem harta și fog-of-war-ul vechi înainte să fie suprascrise, ca să nu
        // se scurgă memorie (texturi/tiled map) la fiecare schimbare de nivel.
        currentMap.dispose()
        fogOfWar?.dispose()
        SoundManager.stopLoops()
        animalVolumes.clear()

        currentLevelIndex = 1
        initLevelInternal(currentLevelIndex, false)

        // 3. Restaurăm viața
        player.health = currentHealth

        // 4. Afișăm un mesaj de succes
        collectionMessage = "Level 2: The Labyrinth"
        collectionMessageTime = gameTimeMs

        // 5. Checkpoint: Salvăm jocul automat (inclusiv cheia)
        saveCurrentState()
    }

    private fun passToLevel3() {
        println("Trecere la Nivelul 3!")

        // Aceeași logică sigură și pentru ultimul nivel
        val currentHealth = player.health

        // Dispunem harta și fog-of-war-ul vechi înainte să fie suprascrise.
        currentMap.dispose()
        fogOfWar?.dispose()
        SoundManager.stopLoops()
        animalVolumes.clear()

        currentLevelIndex = 2
        initLevelInternal(currentLevelIndex, false)

        player.health = currentHealth

        collectionMessage = "Level 3: The Final Battle"
        collectionMessageTime = gameTimeMs

        saveCurrentState()
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

    /** Apelat de WordPuzzleState la rezolvare: cheia finală (id 6) apare la (77, 31) pe grilă. */
    fun onWordPuzzleSolved() {
        wordPuzzleSolved = true
        spawnFinalKey()
    }

    private fun spawnFinalKey() {
        val TS = TileConstants.TILE_SIZE
        entities.add(Key(refLink, 77f * TS, topDownY(31), Assets.keyImage, 6))
    }

    fun onPuzzleFailure() {
        player.takeDamage(20, ignoreInvulnerability = true)

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
        collectionMessage = "The entrance is open!"
        collectionMessageTime = gameTimeMs
    }

    fun addEntity(entity: Entity) {
        entities.add(entity)
    }

    private fun updateObjective() {
        currentObjective = when (currentLevelIndex) {
            1 -> "Solve the puzzles to open the doors."
            2 -> "Defeat the Agent and claim the treasure."
            else -> "Find the key and the Moon talisman."
        }
    }

    /** Obiectivul e afișat mare în centru (după citirea indicatorului) - puzzle-urile așteaptă. */
    fun isWoodSignMessageShowing(): Boolean = objectiveRevealTime in 0f..OBJECTIVE_HOLD
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
            puzzlesSolvedString = solvedPuzzlesString,
            hasTalisman = hasTalisman,
            caveEntranceUnlocked = caveEntranceUnlocked,
            bossDefeated = bossDefeated,
            finalChestCanInteract = finalChest?.canInteract() ?: false,
            doorsOpened = doorsOpened,
            wordPuzzleSolved = wordPuzzleSolved
        )
    }

    fun getMap() = currentMap
    fun getEntities() = entities
    fun getCurrentLevel(): Int = currentLevelIndex

    override fun dispose() {
        SoundManager.stopLoops()
        currentMap.dispose()
        fogOfWar?.dispose()
        miniMapTerrainTexture?.dispose()
        font.dispose()
        shapeRenderer.dispose()
        // touchController și Assets sunt la nivel de aplicație (RefLinks/LostExpeditionGame),
        // nu per GameState, deci nu se dispun aici.
    }

    override fun render(batch: SpriteBatch) {
        val camera = refLink.gameCamera

        currentMap.render(camera)

        val allEntities = entities.toMutableList()
        allEntities.add(player)
        // finalBoss este deja in lista de entitati (adaugat in loadLevel3Entities),
        // nu il mai adaugam o data ca sa nu fie desenat de doua ori.
        // Capcanele sunt pe podea: se desenează primele, altfel una aflată sub jucător
        // (y mai mic) ar fi desenată peste picioarele lui.
        allEntities.sortWith(compareBy<Entity> { if (it is Trap) 0 else 1 }.thenByDescending { it.y })

        batch.projectionMatrix = camera.combined
        batch.begin()
        for (entity in allEntities) {
            entity.render(batch)
        }
        if (currentLevelIndex == 1) drawDoorArchesInFront(batch)
        batch.end()

        fogOfWar?.render(batch, camera)
        renderUI(batch)
        refLink.touchController.draw()
    }

    /**
     * Redesenează rândul de sus al fiecărei uși (arcul) DUPĂ entități, ca jucătorul să pară că
     * trece PE SUB arc, nu peste el. Desenăm dala curentă din layer-ul de obiecte, deci merge
     * atât pentru ușa închisă cât și pentru cea deschisă.
     */
    private fun drawDoorArchesInFront(batch: SpriteBatch) {
        val layer = currentMap.tiledMap.layers.get("objects") as? com.badlogic.gdx.maps.tiled.TiledMapTileLayer ?: return
        val TS = TileConstants.TILE_SIZE
        for (door in puzzleDoorPositions) {
            val archRow = currentMap.height - 1 - door[1]
            for (tx in intArrayOf(door[0], door[2])) {
                val region = layer.getCell(tx, archRow)?.tile?.textureRegion ?: continue
                batch.draw(region, tx * TS, archRow * TS, TS, TS)
            }
        }
    }

    private fun renderUI(batch: SpriteBatch) {
        batch.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        drawHealthBar(batch)

        batch.begin()

        font.data.setScale(1f)

        collectionMessage?.let { msg ->
            val layout = GlyphLayout(font, msg)
            val x = (Gdx.graphics.width - layout.width) / 2
            val y = Gdx.graphics.height / 2f + 100f

            font.color = Color.CYAN
            font.draw(batch, msg, x, y)
        }

        batch.end()

        drawObjective(batch)

        if (showMiniMapOverlay) drawMiniMapOverlay(batch)
        drawTopRightButtons(batch)
    }

    /**
     * Obiectivul nivelului. După citirea indicatorului apare mare în centru (OBJECTIVE_HOLD),
     * apoi urcă și se micșorează spre partea de sus (OBJECTIVE_MOVE), unde rămâne afișat.
     */
    private fun drawObjective(batch: SpriteBatch) {
        val animating = objectiveRevealTime >= 0f
        if (!animating && !isObjectiveDisplayed) return

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val topY = h - h * 0.06f
        val centerY = h * 0.62f

        // 0 = centru (mare), 1 = sus (normal)
        val t = if (!animating) 1f
        else ((objectiveRevealTime - OBJECTIVE_HOLD) / OBJECTIVE_MOVE).coerceIn(0f, 1f)
        val eased = t * t * (3f - 2f * t)
        val scale = MathUtils.lerp(1.3f, 0.8f, eased)
        val textY = MathUtils.lerp(centerY, topY, eased)

        val text = "Objective: $currentObjective"
        font.data.setScale(scale)
        val layout = GlyphLayout(font, text)
        val padX = 24f
        val padY = 14f
        val boxX = (w - layout.width) / 2f - padX
        val boxY = textY - layout.height - padY

        Gdx.gl.glEnable(GL20.GL_BLEND)
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = hudPanelColor
        shapeRenderer.rect(boxX, boxY, layout.width + padX * 2, layout.height + padY * 2)
        shapeRenderer.end()

        batch.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        batch.begin()
        font.color = Color.YELLOW
        font.draw(batch, text, (w - layout.width) / 2f, textY)
        batch.end()
        font.data.setScale(1f)
    }

    private fun drawHealthBar(batch: SpriteBatch) {
        if (batch.isDrawing) batch.end()

        val h = Gdx.graphics.height.toFloat()
        val w = Gdx.graphics.width.toFloat()

        // Layout relativ la înălțimea ecranului (ca la joystick/butoane), nu pixeli ficși,
        // ca bara să rămână complet vizibilă pe orice rezoluție/aspect ratio. Padding mai mare
        // decât la clusterul din dreapta sus, pentru că o bară dreptunghiulară lipită de colț
        // se simte vizual mult mai "înghesuită" decât un cerc la aceeași distanță de margine.
        val padding = MathUtils.clamp(h * 0.055f, 32f, 64f)
        val barWidth = MathUtils.clamp(h * 0.34f, 160f, 260f)
        val barHeight = MathUtils.clamp(h * 0.045f, 22f, 34f)
        val x = padding
        val y = h - padding - barHeight

        val maxHealth = player.getMaxHealth()
        val healthPercent = (player.health.toFloat() / maxHealth.toFloat()).coerceIn(0f, 1f)
        val fillColor = when {
            healthPercent > 0.5f -> Color(0.25f, 0.75f, 0.25f, 1f)
            healthPercent > 0.25f -> Color(0.9f, 0.65f, 0.1f, 1f)
            else -> Color(0.85f, 0.2f, 0.2f, 1f)
        }

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = hudPanelColor
        shapeRenderer.rect(x, y, barWidth, barHeight)
        shapeRenderer.color = fillColor
        shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = hudBorderColor
        shapeRenderer.rect(x, y, barWidth, barHeight)
        shapeRenderer.end()

        // Valoare HP curent/maxim, ca bara să nu fie doar o dungă de culoare fără context.
        batch.begin()
        font.data.setScale(0.7f)
        font.color = Color.WHITE
        val hpText = "${player.health}/$maxHealth"
        val layout = GlyphLayout(font, hpText)
        val textX = x + (barWidth - layout.width) / 2f
        val textY = y + (barHeight + layout.height) / 2f
        font.draw(batch, hpText, textX, textY)
        font.data.setScale(1f)
        batch.end()
    }

    // Cluster din dreapta sus: minihartă / pauză / setări.
    // Aceeași familie vizuală ca butoanele de atac/interacțiune din TouchController:
    // cercuri cu fundal semi-transparent, contur subțire, iconiță simplă în interior.
    private fun drawTopRightButtons(batch: SpriteBatch) {
        if (batch.isDrawing) batch.end()

        val h = Gdx.graphics.height.toFloat()
        val w = Gdx.graphics.width.toFloat()
        val padding = MathUtils.clamp(h * 0.025f, 16f, 32f)
        val radius = MathUtils.clamp(h * 0.045f, 26f, 42f)
        val gap = radius * 0.6f

        val cx = w - padding - radius
        val mapCy = h - padding - radius
        val pauseCy = mapCy - radius * 2f - gap
        val settingsCy = pauseCy - radius * 2f - gap

        mapButtonBounds.set(cx - radius, mapCy - radius, radius * 2f, radius * 2f)
        pauseButtonBounds.set(cx - radius, pauseCy - radius, radius * 2f, radius * 2f)
        settingsButtonBounds.set(cx - radius, settingsCy - radius, radius * 2f, radius * 2f)

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = if (showMiniMapOverlay) hudPanelColorActive else hudPanelColor
        shapeRenderer.circle(cx, mapCy, radius, 32)
        shapeRenderer.color = hudPanelColor
        shapeRenderer.circle(cx, pauseCy, radius, 32)
        shapeRenderer.circle(cx, settingsCy, radius, 32)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = hudBorderColor
        shapeRenderer.circle(cx, mapCy, radius, 32)
        shapeRenderer.circle(cx, pauseCy, radius, 32)
        shapeRenderer.circle(cx, settingsCy, radius, 32)
        shapeRenderer.end()

        drawMapIcon(cx, mapCy, radius)
        drawPauseIcon(cx, pauseCy, radius)
        drawSettingsIcon(cx, settingsCy, radius)
    }

    private fun drawMapIcon(cx: Float, cy: Float, radius: Float) {
        val s = radius * 0.45f
        val gap = s * 0.3f
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = hudBorderColor
        shapeRenderer.rect(cx - s - gap / 2f, cy + gap / 2f, s, s)
        shapeRenderer.rect(cx + gap / 2f, cy + gap / 2f, s, s)
        shapeRenderer.rect(cx - s - gap / 2f, cy - s - gap / 2f, s, s)
        shapeRenderer.rect(cx + gap / 2f, cy - s - gap / 2f, s, s)
        shapeRenderer.end()
    }

    private fun drawPauseIcon(cx: Float, cy: Float, radius: Float) {
        val barWidth = radius * 0.22f
        val barHeight = radius * 0.9f
        val gap = radius * 0.18f
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = hudBorderColor
        shapeRenderer.rect(cx - gap / 2f - barWidth, cy - barHeight / 2f, barWidth, barHeight)
        shapeRenderer.rect(cx + gap / 2f, cy - barHeight / 2f, barWidth, barHeight)
        shapeRenderer.end()
    }

    private fun drawSettingsIcon(cx: Float, cy: Float, radius: Float) {
        val lineLength = radius * 0.9f
        val lineGap = radius * 0.35f
        val knobRadius = radius * 0.09f
        val startX = cx - lineLength / 2f
        val endX = cx + lineLength / 2f
        val ys = floatArrayOf(cy + lineGap, cy, cy - lineGap)
        val knobOffsets = floatArrayOf(0.3f, 0.7f, 0.5f)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = hudBorderColor
        for (y in ys) {
            shapeRenderer.rectLine(startX, y, endX, y, 2f)
        }
        for (i in ys.indices) {
            val knobX = startX + lineLength * knobOffsets[i]
            shapeRenderer.circle(knobX, ys[i], knobRadius, 12)
        }
        shapeRenderer.end()
    }

    // Overlay de minihartă, deschis/închis din butonul din cluster. Reutilizează
    // exact logica de desenare a hărții de dinainte, doar centrată și mai mare.
    /**
     * Coace terenul solid al nivelului curent într-o textură (1 pixel per tile), o singură
     * dată la încărcarea nivelului, folosind isSolidAt() (fără alocare, fără GID lookups).
     * Overlay-ul de minihartă doar scalează această textură la desenare, în loc să
     * reparcurgă toată harta în fiecare frame cât timp e deschis.
     */
    private fun bakeMiniMapTerrainTexture() {
        miniMapTerrainTexture?.dispose()

        val terrainColor = when (currentLevelIndex) {
            0 -> Color(34f / 255f, 139f / 255f, 34f / 255f, 1f)
            1 -> Color(100f / 255f, 100f / 255f, 100f / 255f, 1f)
            else -> Color(87f / 255f, 51f / 255f, 35f / 255f, 1f)
        }

        val pixmap = Pixmap(currentMap.width, currentMap.height, Pixmap.Format.RGBA8888)
        pixmap.setColor(0f, 0f, 0f, 0f)
        pixmap.fill()
        pixmap.setColor(terrainColor)

        for (yTile in 0 until currentMap.height) {
            for (xTile in 0 until currentMap.width) {
                if (currentMap.isSolidAt(xTile, yTile)) {
                    // Pixmap are originea sus-stânga (Y în jos); harta/minimapa folosesc Y în sus
                    // (yTile=0 = jos), deci inversăm rândul la copiere.
                    pixmap.drawPixel(xTile, currentMap.height - 1 - yTile)
                }
            }
        }

        miniMapTerrainTexture = Texture(pixmap).apply {
            setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }
        pixmap.dispose()
    }

    private fun drawMiniMapOverlay(batch: SpriteBatch) {
        if (batch.isDrawing) batch.end()

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        val TS = TileConstants.TILE_SIZE
        val mapPixelWidth = currentMap.width * TS
        val mapPixelHeight = currentMap.height * TS

        val overlayHeight = h * 0.55f
        val overlayWidth = (mapPixelWidth / mapPixelHeight) * overlayHeight
        val miniMapX = (w - overlayWidth) / 2f
        val miniMapY = (h - overlayHeight) / 2f

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        Gdx.gl.glEnable(GL20.GL_BLEND)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.6f)
        shapeRenderer.rect(0f, 0f, w, h)
        shapeRenderer.color = hudPanelColorActive
        shapeRenderer.rect(miniMapX, miniMapY, overlayWidth, overlayHeight)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = hudBorderColor
        shapeRenderer.rect(miniMapX, miniMapY, overlayWidth, overlayHeight)
        shapeRenderer.end()

        // Terenul static - o singură desenare a texturii coapte, nu o buclă width x height.
        miniMapTerrainTexture?.let { terrain ->
            batch.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
            batch.begin()
            batch.setColor(1f, 1f, 1f, 1f)
            batch.draw(terrain, miniMapX, miniMapY, overlayWidth, overlayHeight)
            batch.end()
        }

        val mapScaleX = overlayWidth / mapPixelWidth
        val mapScaleY = overlayHeight / mapPixelHeight

        // Doar elementele care se mișcă (jucător/chei/talisman) rămân desenate live.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color.YELLOW
        for (entity in entities) {
            if ((entity is Key || entity is Talisman)) {
                val entityMiniMapX = miniMapX + entity.x * mapScaleX
                val entityMiniMapY = miniMapY + entity.y * mapScaleY
                shapeRenderer.circle(entityMiniMapX, entityMiniMapY, 4f)
            }
        }

        shapeRenderer.color = Color.CYAN
        val playerMiniMapX = miniMapX + player.x * mapScaleX
        val playerMiniMapY = miniMapY + player.y * mapScaleY
        shapeRenderer.circle(playerMiniMapX, playerMiniMapY, 6f)

        shapeRenderer.end()

        batch.begin()
        font.data.setScale(0.6f)
        font.color = Color.WHITE
        val hint = "Tap anywhere to close"
        val hintLayout = GlyphLayout(font, hint)
        font.draw(batch, hint, (w - hintLayout.width) / 2f, miniMapY - 10f)
        font.data.setScale(1f)
        batch.end()
    }
}
