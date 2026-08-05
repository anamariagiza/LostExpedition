package com.lostexpedition.game.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound

/**
 * Manager centralizat pentru muzica si efecte sonore.
 *
 * Fisierele audio se pun in assets/sounds/ cu numele de mai jos (format .ogg recomandat).
 * Daca un fisier lipseste, jocul merge normal - doar se logheaza un avertisment,
 * deci poti adauga sunetele pe rand.
 *
 * Respecta setarile din SettingsManager (muzica on/off, sunet on/off, volum master).
 */
object SoundManager {

    private const val SOUNDS_DIR = "sounds"

    // ==================== EFECTE SONORE (placeholder-e) ====================
    const val SFX_CLICK = "sfx_click.ogg"                       // click pe butoane de meniu
    const val SFX_ATTACK = "sfx_attack.ogg"                     // atacul jucatorului (sabie)
    const val SFX_PLAYER_HURT = "sfx_player_hurt.ogg"           // jucatorul ia damage
    const val SFX_ENEMY_HURT = "sfx_enemy_hurt.ogg"             // inamicul/boss-ul ia damage
    const val SFX_KEY = "sfx_key.ogg"                           // cheie/talisman colectat
    const val SFX_DOOR = "sfx_door.ogg"                         // usa se deschide
    const val SFX_TRAP = "sfx_trap.ogg"                         // capcana il raneste pe jucator
    const val SFX_PUZZLE_SUCCESS = "sfx_puzzle_success.ogg"     // puzzle rezolvat
    const val SFX_PUZZLE_FAIL = "sfx_puzzle_fail.ogg"           // puzzle esuat
    const val SFX_CHEST = "sfx_chest.ogg"                       // cufarul final se deschide
    const val SFX_VICTORY = "sfx_victory.ogg"                   // ecranul de victorie
    const val SFX_GAMEOVER = "sfx_gameover.ogg"                 // ecranul de game over

    // ==================== MUZICA (placeholder-e) ====================
    const val MUSIC_MENU = "music_menu.ogg"                     // meniul principal
    // music_level1.ogg / music_level2.ogg / music_level3.ogg - muzica per nivel
    fun musicForLevel(levelIndex: Int) = "music_level${levelIndex + 1}.ogg"

    // Cache pentru efecte (null = fisier lipsa, nu mai incercam sa-l incarcam)
    private val sounds = HashMap<String, Sound?>()
    private val missingLogged = HashSet<String>()

    private var music: Music? = null
    private var currentTrack: String? = null

    // ==================== EFECTE ====================
    fun playSfx(name: String) {
        if (!SettingsManager.isSoundEnabled) return

        val sound = sounds.getOrPut(name) {
            val file = Gdx.files.internal("$SOUNDS_DIR/$name")
            if (file.exists()) {
                try {
                    Gdx.audio.newSound(file)
                } catch (e: Exception) {
                    logMissing(name, "nu a putut fi incarcat: ${e.message}")
                    null
                }
            } else {
                logMissing(name, "lipseste")
                null
            }
        }
        sound?.play(SettingsManager.masterVolume)
    }

    // ==================== MUZICA ====================
    /** Porneste o piesa (in bucla). Daca piesa ceruta canta deja, nu face nimic. */
    fun playMusic(track: String) {
        if (currentTrack == track && music?.isPlaying == true) return
        currentTrack = track
        startMusicIfEnabled()
    }

    fun stopMusic() {
        music?.stop()
        music?.dispose()
        music = null
        currentTrack = null
    }

    /** Chemata cand se schimba setarile (muzica on/off): opreste sau reporneste piesa curenta. */
    fun refreshMusic() {
        startMusicIfEnabled()
    }

    /** Chemata cand se schimba volumul din setari. */
    fun updateVolume() {
        music?.volume = SettingsManager.masterVolume
    }

    private fun startMusicIfEnabled() {
        music?.stop()
        music?.dispose()
        music = null

        if (!SettingsManager.isMusicEnabled) return
        val track = currentTrack ?: return

        val file = Gdx.files.internal("$SOUNDS_DIR/$track")
        if (!file.exists()) {
            logMissing(track, "lipseste")
            return
        }

        try {
            music = Gdx.audio.newMusic(file).apply {
                isLooping = true
                volume = SettingsManager.masterVolume
                play()
            }
        } catch (e: Exception) {
            logMissing(track, "nu a putut fi incarcat: ${e.message}")
        }
    }

    private fun logMissing(name: String, reason: String) {
        if (missingLogged.add(name)) {
            Gdx.app.log("SoundManager", "Audio '$SOUNDS_DIR/$name' $reason - se continua fara el.")
        }
    }

    fun dispose() {
        sounds.values.forEach { it?.dispose() }
        sounds.clear()
        missingLogged.clear()
        music?.dispose()
        music = null
        currentTrack = null
    }
}
