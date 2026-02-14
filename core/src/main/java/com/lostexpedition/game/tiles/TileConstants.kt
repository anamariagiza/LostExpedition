package com.lostexpedition.game.tiles

/**
 * TileConstants - Constants for tiles and animations
 *
 * Contains tile dimensions, GID sets for different tile types,
 * and animation timing constants (matching Java version).
 *
 * @author LostExpedition Team
 */
object TileConstants {
    // ==================== DIMENSIUNI DALE ====================
    // Sincronizat cu SpriteSheet.java din versiunea Desktop
    const val TILE_SIZE = 48f
    const val TILE_WIDTH = 48
    const val TILE_HEIGHT = 48

    // ==================== GID-uri SOLIDE (Exemple de verificat în Tiled) ====================
    const val GRASS_TILE_SOLID = 81 //modificat pentru a testa mai usor, normal e 82
    const val WALL_TILE_SOLID = 33
    const val WALL_LEVEL3 = 64

    // GID-uri pentru pietre (Preluat din logica de coliziune Java/Desktop)
    val ROCK_GIDS = setOf(216, 97, 232, 217, 233, 249, 234, 235, 236, 221)

    // GID-uri pentru uși (Level 2 & 3)
    val DOOR_CLOSED_GIDS = setOf(56, 57, 88, 89)
    val DOOR_OPEN_GIDS_L3 = setOf(74, 75, 120, 121)
    val DOOR_FINAL_CLOSED_GIDS = setOf(70, 71, 116, 117)

    // NOU: GID-uri pentru scări (Trebuie să fie NON-SOLIDE pentru a permite urcarea)
    val STAIRS_GIDS = setOf(150, 151, 152) // Înlocuiește cu GID-urile reale din Tiled
}
