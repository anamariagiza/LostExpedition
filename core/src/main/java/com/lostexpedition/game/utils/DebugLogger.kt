package com.lostexpedition.game.utils

/**
 * DebugLogger - Centralized debug logging system
 *
 * Provides consistent logging with timestamps and log levels.
 * Can be enabled/disabled globally for release builds.
 *
 * @author LostExpedition Team
 */
object DebugLogger {

    /** Enable/disable all debug logging */
    var isEnabled: Boolean = true

    /** Enable/disable verbose logging */
    var isVerbose: Boolean = false

    /**
     * Log a standard debug message
     * @param tag The component or class name
     * @param message The message to log
     */
    fun log(tag: String, message: String) {
        if (isEnabled) {
            println("[DEBUG][$tag] $message")
        }
    }

    /**
     * Log a warning message
     * @param tag The component or class name
     * @param message The warning message
     */
    fun warn(tag: String, message: String) {
        if (isEnabled) {
            println("[WARN][$tag] $message")
        }
    }

    /**
     * Log an error message
     * @param tag The component or class name
     * @param message The error message
     */
    fun error(tag: String, message: String) {
        if (isEnabled) {
            println("[ERROR][$tag] $message")
        }
    }

    /**
     * Log an error with exception
     * @param tag The component or class name
     * @param message The error message
     * @param throwable The exception
     */
    fun error(tag: String, message: String, throwable: Throwable) {
        if (isEnabled) {
            println("[ERROR][$tag] $message")
            throwable.printStackTrace()
        }
    }

    /**
     * Log a verbose message (only when verbose mode is enabled)
     * @param tag The component or class name
     * @param message The verbose message
     */
    fun verbose(tag: String, message: String) {
        if (isEnabled && isVerbose) {
            println("[VERBOSE][$tag] $message")
        }
    }

    /**
     * Log game state information
     * @param stateName The name of the game state
     * @param action The action being performed (enter, exit, update, etc.)
     */
    fun logState(stateName: String, action: String) {
        if (isEnabled) {
            println("[STATE][$stateName] $action")
        }
    }

    /**
     * Log entity information
     * @param entityType The type of entity
     * @param entityId Optional entity identifier
     * @param message The message to log
     */
    fun logEntity(entityType: String, entityId: String? = null, message: String) {
        if (isEnabled) {
            val idPart = entityId?.let { "#$it" } ?: ""
            println("[ENTITY][$entityType$idPart] $message")
        }
    }

    /**
     * Log performance metrics
     * @param component The component being measured
     * @param metric The metric name
     * @param value The metric value
     */
    fun logPerformance(component: String, metric: String, value: Any) {
        if (isEnabled && isVerbose) {
            println("[PERF][$component] $metric: $value")
        }
    }
}
