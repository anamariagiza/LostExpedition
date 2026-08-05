package com.lostexpedition.game.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

class DatabaseManager {

    private val PREFS_GAME = "LostExpeditionSave"
    private val PREFS_SETTINGS = "LostExpeditionSettings"

    private fun getGamePrefs(): Preferences {
        return Gdx.app.getPreferences(PREFS_GAME)
    }

    private fun getSettingsPrefs(): Preferences {
        return Gdx.app.getPreferences(PREFS_SETTINGS)
    }

    // ========================================================================
    //                  SALVARE JOC (GAME STATE)
    // ========================================================================

    fun saveGameData(
        levelIndex: Int,
        score: Int,
        playerX: Float,
        playerY: Float,
        playerHealth: Int,
        hasKey: Boolean,
        hasDoorKeys: BooleanArray,
        puzzlesSolvedString: String
    ) {
        val prefs = getGamePrefs()
        prefs.putInteger("levelIndex", levelIndex)
        prefs.putInteger("score", score)
        prefs.putFloat("playerX", playerX)
        prefs.putFloat("playerY", playerY)
        prefs.putInteger("playerHealth", playerHealth)
        prefs.putBoolean("hasKey", hasKey)

        // Convertim array-ul de chei în String (ex: "true,false,true")
        val doorKeysStr = hasDoorKeys.joinToString(",")
        prefs.putString("hasDoorKeys", doorKeysStr)

        prefs.putString("puzzlesSolved", puzzlesSolvedString)

        prefs.flush() // Scrie fizic pe disc
        Gdx.app.log("DatabaseManager", "Joc salvat complet!")
    }

    fun loadGameData(): List<PlayerData> {
        val prefs = getGamePrefs()

        if (!prefs.contains("levelIndex")) {
            return emptyList()
        }

        val levelIndex = prefs.getInteger("levelIndex", 0)
        val score = prefs.getInteger("score", 0)
        val px = prefs.getFloat("playerX", 100f)
        val py = prefs.getFloat("playerY", 100f)
        val hp = prefs.getInteger("playerHealth", 100)
        val key = prefs.getBoolean("hasKey", false)
        val puzzles = prefs.getString("puzzlesSolved", "")

        // Decodăm array-ul de chei
        val doorKeysStr = prefs.getString("hasDoorKeys", "false,false,false,false,false,false,false")
        val doorKeysList = doorKeysStr.split(",").map { it.toBoolean() }.toBooleanArray()

        // Asigurăm dimensiunea corectă (7 chei)
        val finalDoorKeys = BooleanArray(7)
        for (i in finalDoorKeys.indices) {
            if (i < doorKeysList.size) finalDoorKeys[i] = doorKeysList[i]
        }

        // Returnăm o listă cu un singur element (pentru compatibilitate cu forEach din GameState)
        return listOf(
            PlayerData(levelIndex, score, px, py, hp, key, finalDoorKeys, puzzles)
        )
    }

    // ========================================================================
    //                  SETĂRI (SETTINGS STATE)
    // ========================================================================

    fun saveSettingsData(sound: Boolean, music: Boolean, vol: Int) {
        val prefs = getSettingsPrefs()
        prefs.putBoolean("sound", sound)
        prefs.putBoolean("music", music)
        prefs.putInteger("volume", vol)
        prefs.flush()
    }

    fun loadSettingsData(): SettingsData {
        val prefs = getSettingsPrefs()
        val s = prefs.getBoolean("sound", true)
        val m = prefs.getBoolean("music", true)
        val v = prefs.getInteger("volume", 100)
        return SettingsData(s, m, v)
    }

    // Metode legacy (nu fac nimic, doar ca să nu crape apelurile vechi)
    fun connect() {}
    fun disconnect() {}
}

// ========================================================================
// DATA CLASSES - Structura exactă cerută de GameState
// ========================================================================

data class PlayerData(
    val levelIndex: Int,
    val score: Int,
    val playerX: Float,
    val playerY: Float,
    val playerHealth: Int,
    val hasKey: Boolean,
    val hasDoorKeys: BooleanArray,
    val puzzlesSolvedString: String
)

data class SettingsData(
    val soundEnabled: Boolean,
    val musicEnabled: Boolean,
    val volume: Int
)
