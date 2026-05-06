package org.koorm.ocpd.data.repos

import kotlinx.datetime.Instant
import org.koorm.ocpd.data.db.KoormDatabase
import org.koorm.ocpd.models.MoodEntry

class MoodRepository(private val db: KoormDatabase) {
    private val q get() = db.moodQueries

    fun getAll(): List<MoodEntry> = q.selectAll().executeAsList().map { row ->
        MoodEntry(
            id = row.id,
            timestamp = Instant.fromEpochMilliseconds(row.timestamp_epoch_ms),
            mood = row.mood.toInt(),
            energy = row.energy.toInt(),
            note = row.note
        )
    }

    fun saveAll(items: List<MoodEntry>) {
        db.transaction {
            q.deleteAll()
            items.forEach { upsert(it) }
        }
    }

    fun upsert(entry: MoodEntry) {
        q.upsert(
            id = entry.id,
            timestamp_epoch_ms = entry.timestamp.toEpochMilliseconds(),
            mood = entry.mood.toLong(),
            energy = entry.energy.toLong(),
            note = entry.note
        )
    }
}
