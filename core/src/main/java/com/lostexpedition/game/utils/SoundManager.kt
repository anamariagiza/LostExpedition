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
    const val SFX_ATTACK = "sfx_attack.ogg"                     // atacul jucatorului (pumn)
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
    const val SFX_STEP1 = "sfx_step1.ogg"                       // pasi (alternam doua sunete)
    const val SFX_STEP2 = "sfx_step2.ogg"
    const val SFX_CARD_FLIP = "sfx_card_flip.ogg"               // intoarcerea unei carti (puzzle 5)

    // ==================== AMBIENTA ANIMALE (bucle, volum dupa distanta) ====================
    const val AMB_JAGUAR = "amb_jaguar.ogg"
    const val AMB_MONKEY = "amb_monkey.ogg"
    const val AMB_BAT = "amb_bat.ogg"

    // ==================== MUZICA (placeholder-e) ====================
    const val MUSIC_MENU = "music_menu.ogg"                     // meniul principal
    // music_level1.ogg / music_level2.ogg / music_level3.ogg - muzica per nivel
    fun musicForLevel(levelIndex: Int) = "music_level${levelIndex + 1}.ogg"

    // Cache pentru efecte (null = fisier lipsa, nu mai incercam sa-l incarcam)
    private val sounds = HashMap<String, Sound?>()
    private val missingLogged = HashSet<String>()

    private var music: Music? = null
    private var currentTrack: String? = null

    private val ALL_SFX = listOf(
        SFX_CLICK, SFX_ATTACK, SFX_PLAYER_HURT, SFX_ENEMY_HURT, SFX_KEY, SFX_DOOR,
        SFX_TRAP, SFX_PUZZLE_SUCCESS, SFX_PUZZLE_FAIL, SFX_CHEST, SFX_VICTORY, SFX_GAMEOVER,
        SFX_STEP1, SFX_STEP2, SFX_CARD_FLIP, AMB_JAGUAR, AMB_MONKEY, AMB_BAT
    )

    // ==================== EFECTE ====================
    /**
     * Încarcă toate efectele dinainte (apelat din LoadingScreenState). Pe Android, Sound
     * folosește SoundPool, care decodează asincron: un play() imediat după newSound() rămâne
     * de obicei mut. Preîncărcate la pornire, efectele sunt gata la prima folosire.
     */
    fun preloadSfx() {
        ALL_SFX.forEach { loadSfx(it) }
        Gdx.app.log("SoundManager", "Efecte preincarcate: ${sounds.count { it.value != null }}/${ALL_SFX.size}")
    }

    /** Sunetul standard de apăsare a unui buton (folosit în toate meniurile și puzzle-urile). */
    fun click() = playSfx(SFX_CLICK, 0.8f)

    /** [volume] = volumul relativ al efectului (ex. pașii mai încet), înmulțit cu volumul master. */
    fun playSfx(name: String, volume: Float = 1f) {
        if (!SettingsManager.isSoundEnabled) return
        loadSfx(name)?.play(SettingsManager.masterVolume * volume)
    }

    // ==================== BUCLE (ambianta animalelor) ====================
    // cheie unica (ex. un animal anume) -> (sunet, id-ul instantei care ruleaza in bucla)
    private val loops = HashMap<String, Pair<Sound, Long>>()
    private var loopsPaused = false

    /**
     * Setează volumul unei bucle identificate prin [key] (o pornește la prima folosire).
     * [volume] e relativ (0..1) și se înmulțește cu volumul master; cu sunetul oprit din
     * Setări bucla rămâne pornită dar mută, ca să revină imediat când e reactivat.
     */
    fun setLoopVolume(key: String, name: String, volume: Float) {
        resumeLoops()
        val finalVolume = if (SettingsManager.isSoundEnabled) volume * SettingsManager.masterVolume else 0f
        val existing = loops[key]
        if (existing == null) {
            if (finalVolume <= 0.001f) return
            val sound = loadSfx(name) ?: return
            val id = sound.loop(finalVolume)
            if (id != -1L) loops[key] = sound to id
            return
        }
        existing.first.setVolume(existing.second, finalVolume)
    }

    /** Oprește temporar toate buclele (pauză, puzzle, setări) - reiau la următorul setLoopVolume. */
    fun pauseLoops() {
        if (loopsPaused) return
        loops.values.forEach { (sound, id) -> sound.pause(id) }
        loopsPaused = true
    }

    private fun resumeLoops() {
        if (!loopsPaused) return
        loops.values.forEach { (sound, id) -> sound.resume(id) }
        loopsPaused = false
    }

    /** Oprește definitiv buclele (schimbare de nivel / ieșire din joc). */
    fun stopLoops() {
        loops.values.forEach { (sound, id) -> sound.stop(id) }
        loops.clear()
        loopsPaused = false
    }

    private fun loadSfx(name: String): Sound? = sounds.getOrPut(name) {
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
        if (!SettingsManager.isMusicEnabled) {
            music?.stop()
            music?.dispose()
            music = null
            return
        }
        // Deja pornita -> o lasam sa continue (nu o luam de la 0)
        if (music?.isPlaying == true) return
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

        val track = currentTrack ?: return
        if (!SettingsManager.isMusicEnabled) {
            Gdx.app.log("SoundManager", "Muzica e oprita din Setari - nu pornesc '$track'.")
            return
        }

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
            Gdx.app.log("SoundManager", "Pornesc muzica '$track' (volum ${SettingsManager.masterVolume}).")
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
        stopLoops()
        sounds.values.forEach { it?.dispose() }
        sounds.clear()
        missingLogged.clear()
        music?.dispose()
        music = null
        currentTrack = null
    }
}
