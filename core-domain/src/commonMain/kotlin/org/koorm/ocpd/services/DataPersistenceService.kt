package org.koorm.ocpd.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koorm.ocpd.models.CognitiveInsight
import org.koorm.ocpd.models.MoodEntry
import org.koorm.ocpd.models.Task

/**
 * Lightweight JSON-on-FileOperations persistence for Phase 0.
 *
 * Replaced in Phase 2 by SQLDelight repositories. Kept simple intentionally —
 * this is a bridge so the Phase 0 baseline compiles end-to-end.
 */
class DataPersistenceService(
    private val files: FileOperations = createFileOperations()
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    fun saveTasks(tasks: List<Task>): Boolean =
        files.writeTextFile(TASKS_FILE, json.encodeToString(tasks))

    fun loadTasks(): List<Task> {
        if (!files.fileExists(TASKS_FILE)) return emptyList()
        val raw = files.readTextFile(TASKS_FILE) ?: return emptyList()
        return runCatching { json.decodeFromString<List<Task>>(raw) }.getOrDefault(emptyList())
    }

    fun saveMoods(entries: List<MoodEntry>): Boolean =
        files.writeTextFile(MOODS_FILE, json.encodeToString(entries))

    fun loadMoods(): List<MoodEntry> {
        if (!files.fileExists(MOODS_FILE)) return emptyList()
        val raw = files.readTextFile(MOODS_FILE) ?: return emptyList()
        return runCatching { json.decodeFromString<List<MoodEntry>>(raw) }.getOrDefault(emptyList())
    }

    fun saveInsights(insights: List<CognitiveInsight>): Boolean =
        files.writeTextFile(INSIGHTS_FILE, json.encodeToString(insights))

    fun loadInsights(): List<CognitiveInsight> {
        if (!files.fileExists(INSIGHTS_FILE)) return emptyList()
        val raw = files.readTextFile(INSIGHTS_FILE) ?: return emptyList()
        return runCatching { json.decodeFromString<List<CognitiveInsight>>(raw) }.getOrDefault(emptyList())
    }

    companion object {
        private const val TASKS_FILE = "tasks.json"
        private const val MOODS_FILE = "moods.json"
        private const val INSIGHTS_FILE = "insights.json"
    }
}
