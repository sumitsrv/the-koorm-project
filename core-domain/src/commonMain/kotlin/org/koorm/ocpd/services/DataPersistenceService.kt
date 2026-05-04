package org.koorm.ocpd.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koorm.ocpd.models.CognitiveInsight
import org.koorm.ocpd.models.MoodEntry
import org.koorm.ocpd.models.Task

/**
 * Persistence port for the manager. Phase 0 used a JSON-on-FileOperations
 * implementation directly; Phase 2 introduces this interface so SQLDelight
 * can be swapped in via [org.koorm.ocpd.data] without changing callers.
 */
interface DataPersistenceService {
    fun saveTasks(tasks: List<Task>): Boolean
    fun loadTasks(): List<Task>
    fun saveMoods(entries: List<MoodEntry>): Boolean
    fun loadMoods(): List<MoodEntry>
    fun saveInsights(insights: List<CognitiveInsight>): Boolean
    fun loadInsights(): List<CognitiveInsight>
}

/**
 * Original Phase 0 implementation — JSON files via [FileOperations].
 *
 * Retained as a fallback (e.g. unit tests, environments without SQLDelight
 * native libs) and as the legacy migration source. New deployments should
 * use the SQLDelight-backed implementation in `core-data`.
 */
class JsonDataPersistenceService(
    private val files: FileOperations = createFileOperations()
) : DataPersistenceService {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override fun saveTasks(tasks: List<Task>): Boolean =
        files.writeTextFile(TASKS_FILE, json.encodeToString(tasks))

    override fun loadTasks(): List<Task> {
        if (!files.fileExists(TASKS_FILE)) return emptyList()
        val raw = files.readTextFile(TASKS_FILE) ?: return emptyList()
        return runCatching { json.decodeFromString<List<Task>>(raw) }.getOrDefault(emptyList())
    }

    override fun saveMoods(entries: List<MoodEntry>): Boolean =
        files.writeTextFile(MOODS_FILE, json.encodeToString(entries))

    override fun loadMoods(): List<MoodEntry> {
        if (!files.fileExists(MOODS_FILE)) return emptyList()
        val raw = files.readTextFile(MOODS_FILE) ?: return emptyList()
        return runCatching { json.decodeFromString<List<MoodEntry>>(raw) }.getOrDefault(emptyList())
    }

    override fun saveInsights(insights: List<CognitiveInsight>): Boolean =
        files.writeTextFile(INSIGHTS_FILE, json.encodeToString(insights))

    override fun loadInsights(): List<CognitiveInsight> {
        if (!files.fileExists(INSIGHTS_FILE)) return emptyList()
        val raw = files.readTextFile(INSIGHTS_FILE) ?: return emptyList()
        return runCatching { json.decodeFromString<List<CognitiveInsight>>(raw) }.getOrDefault(emptyList())
    }

    private companion object {
        const val TASKS_FILE = "tasks.json"
        const val MOODS_FILE = "moods.json"
        const val INSIGHTS_FILE = "insights.json"
    }
}
