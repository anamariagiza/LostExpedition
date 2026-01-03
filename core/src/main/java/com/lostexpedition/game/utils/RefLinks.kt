package com.lostexpedition.game.utils

import com.badlogic.gdx.Gdx
import com.lostexpedition.game.LostExpeditionGame
import com.lostexpedition.game.camera.GameCamera
import com.lostexpedition.game.entities.Player
import com.lostexpedition.game.input.TouchController
import com.lostexpedition.game.map.Map
import com.lostexpedition.game.states.GameState
import com.lostexpedition.game.states.State

class RefLinks(val game: LostExpeditionGame) {

    var map: Map? = null
    var player: Player? = null
    var gameState: GameState? = null

    val gameCamera: GameCamera by lazy {
        GameCamera(this, Gdx.graphics.width, Gdx.graphics.height)
    }

    // ✅ FIX: Constructorul TouchController acceptă acum lățimea și înălțimea, nu RefLinks
    val touchController: TouchController by lazy {
        TouchController(Gdx.graphics.width, Gdx.graphics.height)
    }

    val databaseManager: DatabaseManager = DatabaseManager()

    private var persistedGameState: GameState? = null
    private var level1State: GameState? = null

    fun setState(state: State) {
        State.setState(state)
    }

    fun setPersistedGameState(state: GameState) {
        this.persistedGameState = state
    }

    fun getPersistedGameState(): GameState? = persistedGameState

    fun setLevel1State(state: GameState) {
        this.level1State = state
    }

    fun getLevel1State(): GameState? = level1State

    val width: Int get() = Gdx.graphics.width
    val height: Int get() = Gdx.graphics.height

    fun dispose() {
        databaseManager.disconnect()
    }
}
