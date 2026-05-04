package org.koorm.ocpd.data.repos

import kotlinx.datetime.Instant
import org.koorm.ocpd.data.db.KoormDatabase
import org.koorm.ocpd.models.CognitiveInsight
import org.koorm.ocpd.models.ProcrastinationReason

class InsightRepository(private val db: KoormDatabase) {
    private val q get() = db.insightsQueries

    fun getAll(): List<CognitiveInsight> = q.selectAll().executeAsList().map { row ->
        CognitiveInsight(
            id = row.id,
            timestamp = Instant.fromEpochMilliseconds(row.timestamp_epoch_ms),
            triggerThought = row.trigger_thought,
            emotionalState = row.emotional_state,
            taskId = row.task_id,
            procrastinationReason = runCatching {
                ProcrastinationReason.valueOf(row.procrastination_reason)
            }.getOrDefault(ProcrastinationReason.UNKNOWN),
            reframe = row.reframe
        )
    }

    fun saveAll(items: List<CognitiveInsight>) {
        db.transaction {
            q.deleteAll()
            items.forEach { upsert(it) }
        }
    }

    fun upsert(insight: CognitiveInsight) {
        q.upsert(
            id = insight.id,
            timestamp_epoch_ms = insight.timestamp.toEpochMilliseconds(),
            trigger_thought = insight.triggerThought,
            emotional_state = insight.emotionalState,
            task_id = insight.taskId,
            procrastination_reason = insight.procrastinationReason.name,
            reframe = insight.reframe
        )
    }
}
