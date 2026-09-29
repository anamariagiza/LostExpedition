# Audio credits

All audio in this folder is released under **CC0 1.0 (public domain)**:
https://creativecommons.org/publicdomain/zero/1.0/

CC0 does not require attribution. The sources are listed here for transparency and for the Google Play review.

## Music

| File | Track | Author | Source |
|---|---|---|---|
| `music_menu.ogg` | Cathedral in the Forest (ambient loop) | congusbongus | https://opengameart.org/content/cathedral-in-the-forest-ambient-loop |
| `music_level1.ogg` | Jungle Thriller | iamoneabe | https://opengameart.org/content/jungle-thriller |
| `music_level2.ogg` | I think I'd stay (jungle chill), clean version | Emmntt | https://opengameart.org/content/i-think-id-stay-jungle-chill |
| `music_level3.ogg` | Dark Cavern Ambient (001) | Paul Wortmann | https://opengameart.org/content/dark-cavern-ambient |

The music files were re-encoded to OGG Vorbis (quality 3, 44.1 kHz) to keep the APK small.

## Sound effects (Kenney, www.kenney.nl)

| File | Original file | Pack |
|---|---|---|
| `sfx_click.ogg` | `click_001.ogg` | [Interface Sounds](https://kenney.nl/assets/interface-sounds) |
| `sfx_puzzle_success.ogg` | `confirmation_002.ogg` | Interface Sounds |
| `sfx_puzzle_fail.ogg` | `error_006.ogg` | Interface Sounds |
| `sfx_attack.ogg` | `cloth4.ogg` (punch whoosh) | [RPG Audio](https://kenney.nl/assets/rpg-audio) |
| `sfx_key.ogg` | `handleCoins.ogg` | RPG Audio |
| `sfx_door.ogg` | `doorOpen_1.ogg` | RPG Audio |
| `sfx_chest.ogg` | `creak1.ogg` | RPG Audio |
| `sfx_player_hurt.ogg` | `impactPunch_medium_000.ogg` | [Impact Sounds](https://kenney.nl/assets/impact-sounds) |
| `sfx_enemy_hurt.ogg` | `impactPunch_heavy_000.ogg` | Impact Sounds |
| `sfx_trap.ogg` | `impactMetal_heavy_000.ogg` | Impact Sounds |
| `sfx_victory.ogg` | `Steel jingles/jingles_STEEL02.ogg` | [Music Jingles](https://kenney.nl/assets/music-jingles) |
| `sfx_gameover.ogg` | `Pizzicato jingles/jingles_PIZZI03.ogg` | Music Jingles |

To swap a sound, replace the file and keep the same name. `SoundManager.kt` loads the files by name.
