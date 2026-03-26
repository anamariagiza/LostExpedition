# Lost Expedition — Android

A 2D top-down action-adventure game built with **LibGDX** and **Kotlin**, targeting Android. This is the mobile port of the original Java/AWT desktop version, developed as part of a university project for the **PAOO** course.

> ⚠️ **Work in progress.** The project is actively under development.

---

## Gameplay Overview

You play as an explorer navigating through tile-based maps across 3 levels. Collect keys, solve puzzles, avoid traps, defeat enemies, and uncover the secrets of the Lost Expedition. The main antagonist — **the Agent**, henchman of Magnus Voss — patrols the world, chases you down, and attacks on sight.

---

## Features

- **3 tile-based levels** loaded from `.tmx` map files
- **Player combat & movement** — walk, run, attack, interact, with full directional animations
- **Enemy AI** — the Agent patrols, chases, and attacks with cooldown logic
- **Fog of War** — visibility radius around the player; explored tiles are permanently revealed with exploration percentage tracking
- **Puzzle system** — 5 puzzle types:
  - Symbol grid puzzle
  - Gem ordering puzzle
  - Riddle puzzle
  - Memory card matching puzzle
  - Word puzzle ("LOST EXPEDITION" letter scramble)
- **Touch controls** — virtual joystick + attack/interact buttons optimized for Android
- **Save / Load system** — uses LibGDX `Preferences` (Android SharedPreferences under the hood)
- **Auto-save** on app pause/minimize
- **Settings** — configurable options persisted across sessions
- **State machine** — Menu, Loading, Game, Pause, Puzzle, WordPuzzle, GameOver, EndGame, About, Help states
- **Responsive viewport** — maintains a fixed game width (1500px) and adjusts height to screen aspect ratio
- **Debug logger** utility for development

---

## Project Structure

```
core/src/main/java/com/lostexpedition/game/
├── LostExpeditionGame.kt       # ApplicationAdapter — main game loop, lifecycle
├── camera/
│   └── GameCamera.kt           # Camera following player, clamped to map bounds
├── entities/
│   ├── Entity.kt               # Base class for all game entities
│   ├── Player.kt               # Player movement, combat, animations
│   ├── Agent.kt                # Enemy AI (patrol → chase → attack)
│   ├── SimpleEntities.kt       # Keys, chests, collectibles
│   ├── Complexentities.kt      # NPCs, doors, cave entrances
│   └── Triggerentities.kt      # Traps and trigger zones
├── graphics/
│   ├── Assets.kt               # Loads and stores all textures and animations
│   ├── Animation.kt            # Frame-based animation wrapper
│   ├── SpriteSheet.kt          # Sprite sheet slicing utility
│   └── ImageLoader.kt          # Image loading helper
├── input/
│   ├── TouchController.kt      # Virtual joystick + buttons for Android touch
│   ├── KeyManager.kt           # Keyboard input (desktop/debug)
│   └── MouseManager.kt         # Mouse input (desktop/debug)
├── map/
│   ├── Map.kt                  # TMX map loading and tile rendering
│   └── FogOfWar.kt             # Visibility system with exploration tracking
├── screens/
│   ├── MainMenuScreen.kt       # LibGDX Screen wrapper for menu
│   └── TestScreen.kt           # Development/testing screen
├── states/
│   ├── State.kt                # Base state + static current state manager
│   ├── GameState.kt            # Core gameplay — levels, entities, objectives
│   ├── MenuState.kt            # Main menu (New Game, Load, Settings, About, Exit)
│   ├── PauseState.kt           # Pause overlay
│   ├── PuzzleState.kt          # Mini-game puzzles (5 types)
│   ├── WordPuzzleState.kt      # Letter scramble puzzle
│   ├── LoadingScreenState.kt   # Asset loading screen
│   ├── GameOverState.kt        # Death / game over screen
│   ├── EndGameState.kt         # Victory screen
│   ├── SettingsState.kt        # Settings menu
│   ├── HelpState.kt            # Controls / help screen
│   └── AboutState.kt           # About / credits screen
├── tiles/
│   ├── Tile.kt                 # Tile rendering and collision
│   ├── TileFactory.kt          # Creates tile instances from map data
│   ├── TileTypes.kt            # Tile type enum
│   └── TileConstants.kt        # Tile size constants
└── utils/
    ├── RefLinks.kt             # Central reference hub for game components
    ├── Databasemanager.kt      # Save/load via LibGDX Preferences
    ├── SettingsManager.kt      # Persistent settings (volume, difficulty, etc.)
    ├── DebugLogger.kt          # Logging utility for development
    └── Enums.kt                # Shared enums
```

---

## Getting Started

### Prerequisites

- **Android Studio** (Hedgehog or newer recommended)
- **Android SDK** with a device or emulator running Android 8.0+ (API 26+)
- **JDK 17**
- Kotlin plugin (bundled with Android Studio)

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/anamariagiza/LostExpedition.git
   ```
2. Open the project in **Android Studio**
3. Sync Gradle (`File → Sync Project with Gradle Files`)
4. Select your Android device/emulator and press **Run**

### Gradle

The project uses the Gradle wrapper. You can also build from the command line:

```bash
# Build debug APK
./gradlew assembleDebug

# Install directly on connected device
./gradlew installDebug
```

---

## Controls

### Android (Touch)
| Control | Action |
|---------|--------|
| Virtual joystick (bottom-left) | Move |
| Attack button (bottom-right) | Attack |
| Interact button (bottom-right) | Interact with objects / NPCs |

### Desktop / Debug (Keyboard)
| Key | Action |
|-----|--------|
| `W` / `A` / `S` / `D` | Move |
| `Space` | Attack |
| `E` | Interact |
| `Escape` | Pause |

---

## Architecture Notes

- **`RefLinks`** acts as a dependency injection hub — a single object passed through the game that gives access to the camera, input, state, and database without global singletons everywhere.
- **`State`** uses a static `currentState` field to manage transitions between screens (Menu → LoadingScreen → Game → Pause → Game, etc.).
- **`FogOfWar`** tracks a `revealedTiles` boolean grid that persists within a session, giving the classic RPG "explore-once" fog behavior.
- **Save system** uses LibGDX `Preferences` which maps to Android `SharedPreferences` — no database or file I/O required, making it portable across platforms.
- The viewport in `resize()` keeps a fixed game width of **1500 units** and adjusts the height to the screen's aspect ratio, ensuring the game looks correct on any Android screen size.

---

## Dependencies

| Library | Purpose |
|---------|---------|
| `libGDX` | Core game framework (rendering, input, audio, assets) |
| `gdx-freetype` | TrueType font rendering |
| Kotlin stdlib | Language runtime |

---

## Academic Context

Developed for the **PAOO** (Programarea Avansată pe Obiecte și Obiecte) course as part of a Computer Engineering degree. This version ports the original Java/AWT desktop game to Android using LibGDX + Kotlin, demonstrating cross-platform game architecture, OOP design patterns (State, Service Locator), and mobile-specific considerations like touch input and lifecycle management.

---

## License

This project is for educational purposes. All assets used are either original or free-to-use.
