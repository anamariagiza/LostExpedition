package com.lostexpedition.game.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

class DatabaseManager {

    companion object {
        private const val CURRENT_SAVE_VERSION = 2
        private const val CHECKPOINT_PREFIX = "checkpoint_"
        private const val DOOR_KEYS_SIZE = 7
        private const val DOORS_OPENED_SIZE = 6
    }

    private val PREFS_GAME = "LostExpeditionSave"
    private val PREFS_SETTINGS = "LostExpeditionSettings"

    private fun getGamePrefs(): Preferences {
        return Gdx.app.getPreferences(PREFS_GAME)
    }

    private fun getSettingsPrefs(): Preferences {
        return Gdx.app.getPreferences(PREFS_SETTINGS)
    }

    // ========================================================================
    //                  SALVARE JOC (GAME STATE) - AUTOSAVE
    // ========================================================================

    fun saveGameData(
        levelIndex: Int,
        score: Int,
        playerX: Float,
        playerY: Float,
        playerHealth: Int,
        hasKey: Boolean,
        hasDoorKeys: BooleanArray,
        puzzlesSolvedString: String,
        hasTalisman: Boolean = false,
        caveEntranceUnlocked: Boolean = false,
        doorsOpened: BooleanArray = BooleanArray(DOORS_OPENED_SIZE),
        finalDoorOpened: Boolean = false,
        bossDefeated: Boolean = false,
        finalChestOpened: Boolean = false,
        keysCollected: BooleanArray = BooleanArray(DOOR_KEYS_SIZE),
        talismanCollected: Boolean = false
    ) {
        writeGameData(
            "", levelIndex, score, playerX, playerY, playerHealth, hasKey, hasDoorKeys,
            puzzlesSolvedString, hasTalisman, caveEntranceUnlocked, doorsOpened,
            finalDoorOpened, bossDefeated, finalChestOpened, keysCollected, talismanCollected
        )
        Gdx.app.log("DatabaseManager", "Joc salvat complet!")
    }

    fun loadGameData(): List<PlayerData> {
        return readGameData("")
    }

    // ========================================================================
    //                  CHECKPOINT (nivelul la care revine "TRY AGAIN")
    // ========================================================================

    fun saveCheckpointData(
        levelIndex: Int,
        score: Int,
        playerX: Float,
        playerY: Float,
        playerHealth: Int,
        hasKey: Boolean,
        hasDoorKeys: BooleanArray,
        puzzlesSolvedString: String,
        hasTalisman: Boolean = false,
        caveEntranceUnlocked: Boolean = false,
        doorsOpened: BooleanArray = BooleanArray(DOORS_OPENED_SIZE),
        finalDoorOpened: Boolean = false,
        bossDefeated: Boolean = false,
        finalChestOpened: Boolean = false,
        keysCollected: BooleanArray = BooleanArray(DOOR_KEYS_SIZE),
        talismanCollected: Boolean = false
    ) {
        writeGameData(
            CHECKPOINT_PREFIX, levelIndex, score, playerX, playerY, playerHealth, hasKey, hasDoorKeys,
            puzzlesSolvedString, hasTalisman, caveEntranceUnlocked, doorsOpened,
            finalDoorOpened, bossDefeated, finalChestOpened, keysCollected, talismanCollected
        )
        Gdx.app.log("DatabaseManager", "Checkpoint salvat!")
    }

    fun loadCheckpointData(): List<PlayerData> {
        return readGameData(CHECKPOINT_PREFIX)
    }

    /** Sterge atat autosave-ul cat si checkpoint-ul (folosit la New Game). */
    fun clearAllSaveData() {
        val prefs = getGamePrefs()
        prefs.clear()
        prefs.flush()
    }

    // ========================================================================
    //                  IMPLEMENTARE COMUNA (prefix "" = autosave, "checkpoint_" = checkpoint)
    // ========================================================================

    private fun writeGameData(
        prefix: String,
        levelIndex: Int,
        score: Int,
        playerX: Float,
        playerY: Float,
        playerHealth: Int,
        hasKey: Boolean,
        hasDoorKeys: BooleanArray,
        puzzlesSolvedString: String,
        hasTalisman: Boolean,
        caveEntranceUnlocked: Boolean,
        doorsOpened: BooleanArray,
        finalDoorOpened: Boolean,
        bossDefeated: Boolean,
        finalChestOpened: Boolean,
        keysCollected: BooleanArray,
        talismanCollected: Boolean
    ) {
        val prefs = getGamePrefs()
        prefs.putInteger(prefix + "levelIndex", levelIndex)
        prefs.putInteger(prefix + "score", score)
        prefs.putFloat(prefix + "playerX", playerX)
        prefs.putFloat(prefix + "playerY", playerY)
        prefs.putInteger(prefix + "playerHealth", playerHealth)
        prefs.putBoolean(prefix + "hasKey", hasKey)
        prefs.putString(prefix + "hasDoorKeys", hasDoorKeys.joinToString(","))
        prefs.putString(prefix + "puzzlesSolved", puzzlesSolvedString)
        prefs.putBoolean(prefix + "hasTalisman", hasTalisman)
        prefs.putBoolean(prefix + "caveEntranceUnlocked", caveEntranceUnlocked)
        prefs.putString(prefix + "doorsOpened", doorsOpened.joinToString(","))
        prefs.putBoolean(prefix + "finalDoorOpened", finalDoorOpened)
        prefs.putBoolean(prefix + "bossDefeated", bossDefeated)
        prefs.putBoolean(prefix + "finalChestOpened", finalChestOpened)
        prefs.putString(prefix + "keysCollected", keysCollected.joinToString(","))
        prefs.putBoolean(prefix + "talismanCollected", talismanCollected)
        prefs.putInteger(prefix + "saveVersion", CURRENT_SAVE_VERSION)
        prefs.flush()
    }

    private fun readGameData(prefix: String): List<PlayerData> {
        val prefs = getGamePrefs()

        if (!prefs.contains(prefix + "levelIndex")) {
            return emptyList()
        }

        val levelIndex = prefs.getInteger(prefix + "levelIndex", 0)
        val score = prefs.getInteger(prefix + "score", 0)
        val px = prefs.getFloat(prefix + "playerX", 100f)
        val py = prefs.getFloat(prefix + "playerY", 100f)
        val hp = prefs.getInteger(prefix + "playerHealth", 100)
        val key = prefs.getBoolean(prefix + "hasKey", false)
        val puzzles = prefs.getString(prefix + "puzzlesSolved", "")

        val hasDoorKeys = readBooleanArray(prefs, prefix + "hasDoorKeys", DOOR_KEYS_SIZE)
        val hasTalisman = prefs.getBoolean(prefix + "hasTalisman", false)
        val caveEntranceUnlocked = prefs.getBoolean(prefix + "caveEntranceUnlocked", false)
        val doorsOpened = readBooleanArray(prefs, prefix + "doorsOpened", DOORS_OPENED_SIZE)
        val finalDoorOpened = prefs.getBoolean(prefix + "finalDoorOpened", false)
        val bossDefeated = prefs.getBoolean(prefix + "bossDefeated", false)
        val finalChestOpened = prefs.getBoolean(prefix + "finalChestOpened", false)
        val keysCollected = readBooleanArray(prefs, prefix + "keysCollected", DOOR_KEYS_SIZE)
        val talismanCollected = prefs.getBoolean(prefix + "talismanCollected", false)
        val saveVersion = prefs.getInteger(prefix + "saveVersion", 1)

        return listOf(
            PlayerData(
                levelIndex, score, px, py, hp, key, hasDoorKeys, puzzles,
                hasTalisman, caveEntranceUnlocked, doorsOpened, finalDoorOpened,
                bossDefeated, finalChestOpened, keysCollected, talismanCollected, saveVersion
            )
        )
    }

    /** Citeste un BooleanArray dintr-un String "true,false,...", cu dimensiune fixa si default false. */
    private fun readBooleanArray(prefs: Preferences, key: String, size: Int): BooleanArray {
        val result = BooleanArray(size) { false }
        val str = prefs.getString(key, "")
        if (str.isNotEmpty()) {
            val parts = str.split(",")
            for (i in result.indices) {
                if (i < parts.size) {
                    result[i] = parts[i].toBoolean()
                }
            }
        }
        return result
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
    val puzzlesSolvedString: String,
    val hasTalisman: Boolean = false,
    val caveEntranceUnlocked: Boolean = false,
    val doorsOpened: BooleanArray = BooleanArray(6),
    val finalDoorOpened: Boolean = false,
    val bossDefeated: Boolean = false,
    val finalChestOpened: Boolean = false,
    val keysCollected: BooleanArray = BooleanArray(7),
    val talismanCollected: Boolean = false,
    val saveVersion: Int = 1
)

data class SettingsData(
    val soundEnabled: Boolean,
    val musicEnabled: Boolean,
    val volume: Int
)
