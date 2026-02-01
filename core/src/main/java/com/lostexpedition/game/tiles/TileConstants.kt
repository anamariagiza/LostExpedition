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
    // ==================== TILE DIMENSIONS ====================
    /** Tile size in pixels */
    const val TILE_SIZE = 48f

    /** Tile width in pixels (for Tile class compatibility) */
    const val TILE_WIDTH = 32

    /** Tile height in pixels (for Tile class compatibility) */
    const val TILE_HEIGHT = 32

    // ==================== SOLID TILE GIDs ====================
    /** Grass tile GID that is solid */
    const val GRASS_TILE_SOLID = 82

    /** Wall tile GID that is solid */
    const val WALL_TILE_SOLID = 33

    /** Wall tile GID for Level 3 */
    const val WALL_LEVEL3 = 64

    // ==================== TILE TYPE GID SETS ====================
    /** Rock tile GIDs (always solid) */
    val ROCK_GIDS = setOf(216, 97, 232, 217, 233, 249, 234, 235, 236, 221)

    /** Closed door GIDs (solid) */
    val DOOR_CLOSED_GIDS = setOf(56, 57, 88, 89)

    /** Open door GIDs for Level 3 (non-solid) */
    val DOOR_OPEN_GIDS_L3 = setOf(74, 75, 120, 121)

    /** Final closed door GIDs for Level 3 */
    val DOOR_FINAL_CLOSED_GIDS = setOf(70, 71, 116, 117)

    /** Trap tile GIDs */
    val TRAP_GIDS = setOf<Int>() // Add trap GIDs when defined

    // ==================== ANIMATION SPEEDS (Like Java Version) ====================
    /** Very slow animation speed (seconds per frame) */
    const val ANIM_SPEED_VERY_SLOW = 0.3f

    /** Slow animation speed (for idle animations) */
    const val ANIM_SPEED_SLOW = 0.2f

    /** Normal animation speed (for walking) */
    const val ANIM_SPEED_NORMAL = 0.15f

    /** Fast animation speed (for running) */
    const val ANIM_SPEED_FAST = 0.1f

    /** Very fast animation speed (for attacks) */
    const val ANIM_SPEED_VERY_FAST = 0.08f

    // ==================== PLAYER ANIMATION SPEEDS ====================
    /** Player idle animation speed */
    const val PLAYER_IDLE_SPEED = ANIM_SPEED_SLOW

    /** Player walk animation speed */
    const val PLAYER_WALK_SPEED = ANIM_SPEED_NORMAL

    /** Player run animation speed */
    const val PLAYER_RUN_SPEED = ANIM_SPEED_FAST

    /** Player attack animation speed */
    const val PLAYER_ATTACK_SPEED = ANIM_SPEED_VERY_FAST

    /** Player jump animation speed */
    const val PLAYER_JUMP_SPEED = ANIM_SPEED_NORMAL

    /** Player hurt animation speed */
    const val PLAYER_HURT_SPEED = ANIM_SPEED_FAST

    // ==================== ENEMY ANIMATION SPEEDS ====================
    /** Enemy idle/patrol animation speed */
    const val ENEMY_IDLE_SPEED = ANIM_SPEED_NORMAL

    /** Enemy chase animation speed */
    const val ENEMY_CHASE_SPEED = ANIM_SPEED_FAST

    /** Enemy attack animation speed */
    const val ENEMY_ATTACK_SPEED = ANIM_SPEED_FAST

    // ==================== SPRITE SHEET DEFAULTS ====================
    /** Default sprite width */
    const val DEFAULT_SPRITE_WIDTH = 64

    /** Default sprite height */
    const val DEFAULT_SPRITE_HEIGHT = 64

    /** Default frame count for single-frame sprites */
    const val DEFAULT_FRAME_COUNT = 1
}
