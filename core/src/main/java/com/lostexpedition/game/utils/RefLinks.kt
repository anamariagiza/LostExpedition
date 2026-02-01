package com.lostexpedition.game.utils

import com.badlogic.gdx.Gdx
import com.lostexpedition.game.LostExpeditionGame
import com.lostexpedition.game.camera.GameCamera
import com.lostexpedition.game.entities.Player
import com.lostexpedition.game.input.TouchController
import com.lostexpedition.game.map.Map
import com.lostexpedition.game.states.GameState
import com.lostexpedition.game.states.State

/**
 * RefLinks - Central reference holder for game components
 *
 * Provides access to shared game resources and state management.
 * Acts as a service locator pattern implementation.
 *
 * @param game The main game instance
 * @author LostExpedition Team
 */
class RefLinks(val game: LostExpeditionGame) {

    /** Current map instance */
    var map: Map? = null

    /** Current player instance */
    var player: Player? = null

    /** Current game state reference */
    var gameState: GameState? = null

    /** Game camera with lazy initialization */
    val gameCamera: GameCamera by lazy {
        GameCamera(this, Gdx.graphics.width, Gdx.graphics.height)
    }

    /** Touch controller for mobile input with lazy initialization */
    val touchController: TouchController by lazy {
        TouchController(Gdx.graphics.width, Gdx.graphics.height)
    }

    /** Database manager for save/load functionality */
    val databaseManager: DatabaseManager = DatabaseManager()

    /** Persisted game state (for save/load) */
    private var persistedGameState: GameState? = null

    /** Reference to Level 1 state (for returning from pause, etc.) */
    private var level1State: GameState? = null

    /** Reference to Level 2 state */
    private var level2State: GameState? = null

    /** Reference to Level 3 state */
    private var level3State: GameState? = null

    /**
     * Sets the current game state
     * @param state The new state to set
     */
    fun setState(state: State) {
        State.setState(state)
    }

    /**
     * Gets the previous state (for returning from menus, etc.)
     * @return The previous State or null
     */
    fun getPreviousState(): State? = State.getPreviousState()

    /**
     * Sets the persisted game state for save/load
     * @param state The game state to persist
     */
    fun setPersistedGameState(state: GameState) {
        this.persistedGameState = state
    }

    /**
     * Gets the persisted game state
     * @return The persisted GameState or null
     */
    fun getPersistedGameState(): GameState? = persistedGameState

    /**
     * Sets Level 1 state reference
     * @param state The Level 1 GameState
     */
    fun setLevel1State(state: GameState) {
        this.level1State = state
    }

    /**
     * Gets Level 1 state reference
     * @return The Level 1 GameState or null
     */
    fun getLevel1State(): GameState? = level1State

    /**
     * Sets Level 2 state reference
     * @param state The Level 2 GameState
     */
    fun setLevel2State(state: GameState) {
        this.level2State = state
    }

    /**
     * Gets Level 2 state reference
     * @return The Level 2 GameState or null
     */
    fun getLevel2State(): GameState? = level2State

    /**
     * Sets Level 3 state reference
     * @param state The Level 3 GameState
     */
    fun setLevel3State(state: GameState) {
        this.level3State = state
    }

    /**
     * Gets Level 3 state reference
     * @return The Level 3 GameState or null
     */
    fun getLevel3State(): GameState? = level3State

    /** Screen width */
    val width: Int get() = Gdx.graphics.width

    /** Screen height */
    val height: Int get() = Gdx.graphics.height

    /**
     * Disposes all managed resources
     */
    fun dispose() {
        databaseManager.disconnect()
        DebugLogger.log("RefLinks", "Disposed resources")
    }
}
