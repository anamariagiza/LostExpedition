package com.lostexpedition.game.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

object SettingsManager {
    private const val PREFS_NAME = "lost_expedition_save"
    private const val KEY_SOUND = "sound"
    private const val KEY_HIGHSCORE = "highscore"

    private val prefs: Preferences
        get() = Gdx.app.getPreferences(PREFS_NAME)

    // Proprietate simplă pentru Sunet (True/False)
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) {
            prefs.putBoolean(KEY_SOUND, value)
            prefs.flush() // Salvează fizic
        }

    // Proprietate pentru HighScore
    var highScore: Int
        get() = prefs.getInteger(KEY_HIGHSCORE, 0)
        set(value) {
            if (value > highScore) {
                prefs.putInteger(KEY_HIGHSCORE, value)
                prefs.flush()
            }
        }
}
