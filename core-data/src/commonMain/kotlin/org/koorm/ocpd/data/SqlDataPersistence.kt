package org.koorm.ocpd.data

import org.koorm.ocpd.data.db.KoormDatabase
import org.koorm.ocpd.data.repos.InsightRepository
import org.koorm.ocpd.data.repos.MoodRepository
import org.koorm.ocpd.data.repos.TaskRepository
import org.koorm.ocpd.models.CognitiveInsight
import org.koorm.ocpd.models.MoodEntry
import org.koorm.ocpd.models.Task
import org.koorm.ocpd.services.DataPersistenceService

/**
 * SQLDelight-backed [DataPersistenceService]. Drop-in replacement for the
 * legacy [org.koorm.ocpd.services.JsonDataPersistenceService]; the manager
 * keeps its current bulk save/load surface while the actual rows are stored
 * encrypted (via the DriverFactory's SQLCipher passphrase on Android).
 *
 * The bulk `save*` methods replace the entire collection in a single
 * transaction — matching the legacy JSON behaviour exactly so callers don't
 * need to be re-architected for delta updates yet. Phase 7 will introduce
 * Flow-based observation for the UI to drive off granular row changes.
 */
class SqlDataPersistence(private val database: KoormDatabase) : DataPersistenceService {

    private val taskRepository = TaskRepository(database)
    private val moodRepository = MoodRepository(database)
    private val insightRepository = InsightRepository(database)

    override fun saveTasks(tasks: List<Task>): Boolean = runCatching {
        taskRepository.saveAll(tasks); true
    }.getOrDefault(false)

    override fun loadTasks(): List<Task> = runCatching { taskRepository.getAll() }.getOrDefault(emptyList())

    override fun saveMoods(entries: List<MoodEntry>): Boolean = runCatching {
        moodRepository.saveAll(entries); true
    }.getOrDefault(false)

    override fun loadMoods(): List<MoodEntry> = runCatching { moodRepository.getAll() }.getOrDefault(emptyList())

    override fun saveInsights(insights: List<CognitiveInsight>): Boolean = runCatching {
        insightRepository.saveAll(insights); true
    }.getOrDefault(false)

    override fun loadInsights(): List<CognitiveInsight> =
        runCatching { insightRepository.getAll() }.getOrDefault(emptyList())
}
