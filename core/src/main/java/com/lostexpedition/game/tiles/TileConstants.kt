package com.lostexpedition.game.tiles

object TileConstants {
    // Dimensiuni
    const val TILE_SIZE = 48f

    // GID-uri Solide (Copiate din Desktop)
    const val GRASS_TILE_SOLID = 82
    const val WALL_TILE_SOLID = 33
    const val WALL_LEVEL3 = 64

    // Stânci (RockTile)
    val ROCK_GIDS = setOf(216, 97, 232, 217, 233, 249, 234, 235, 236, 221)

    // Uși Închise (Solide)
    val DOOR_CLOSED_GIDS = setOf(56, 57, 88, 89)

    // Uși Deschise (Level 3 - Special check)
    val DOOR_OPEN_GIDS_L3 = setOf(74, 75, 120, 121)

    // Ușa de final Level 3 (cea care trebuie verificată)
    val DOOR_FINAL_CLOSED_GIDS = setOf(70, 71, 116, 117)
}
