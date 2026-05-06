package org.koorm.ocpd.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * User-tuneable preferences. Persisted via [FileOperations] for Phase 0;
 * migrates to SQLDelight `setting` table in Phase 2.
 */
class ConfigurationService(
    private val files: FileOperations = createFileOperations()
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Serializable
    data class Preferences(
        val toneEncouraging: Boolean = true,
        val perfectionismGuard: Boolean = true,
        val defaultTaskMinutes: Int = 30,
        val maxDailyTasks: Int = 5,
        val notificationsEnabled: Boolean = true,
        val autoBreakdownThresholdMinutes: Int = 45
    )

    private var cached: Preferences? = null

    fun load(): Preferences {
        cached?.let { return it }
        val raw = if (files.fileExists(FILE)) files.readTextFile(FILE) else null
        val prefs = if (raw == null) Preferences()
        else runCatching { json.decodeFromString<Preferences>(raw) }.getOrDefault(Preferences())
        cached = prefs
        return prefs
    }

    fun save(prefs: Preferences): Boolean {
        cached = prefs
        return files.writeTextFile(FILE, json.encodeToString(prefs))
    }

    companion object {
        private const val FILE = "preferences.json"
    }
}
