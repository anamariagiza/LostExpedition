package com.lostexpedition.game.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.lostexpedition.game.graphics.Assets

object SettingsManager {
    private const val PREFS_NAME = "LostExpeditionSettings"
    private const val KEY_MUSIC = "music_enabled"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_VOLUME = "master_volume"

    // Folosim 'lazy' pentru a ne asigura că Gdx.app este inițializat înainte
    private val prefs: Preferences by lazy {
        Gdx.app.getPreferences(PREFS_NAME)
    }

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) {
            prefs.putBoolean(KEY_MUSIC, value)
            prefs.flush()
            updateAssets()
        }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) {
            prefs.putBoolean(KEY_SOUND, value)
            prefs.flush()
        }

    var masterVolume: Float
        get() = prefs.getFloat(KEY_VOLUME, 1.0f)
        set(value) {
            prefs.putFloat(KEY_VOLUME, value.coerceIn(0f, 1f))
            prefs.flush()
            updateAssets()
        }

    fun updateAssets() {
        // Aici va veni logica de muzică mai târziu
    }
}
