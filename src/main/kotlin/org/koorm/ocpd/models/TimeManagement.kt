package org.koorm.ocpd.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class TimeBlock(
    val id: String,
    val taskId: String?,
    val title: String,
    @Serializable(with = InstantSerializer::class)
    val startTime: Instant,
    @Serializable(with = InstantSerializer::class)
    val endTime: Instant,
    val type: TimeBlockType = TimeBlockType.WORK,
    val isFlexible: Boolean = true,
    @Serializable(with = InstantSerializer::class)
    val actualStartTime: Instant? = null,
    @Serializable(with = InstantSerializer::class)
    val actualEndTime: Instant? = null,
    val notes: String = ""
)

@Serializable
enum class TimeBlockType {
    WORK, BREAK, DEEP_FOCUS, ADMIN, BUFFER, PERSONAL
}

@Serializable
data class PomodoroSession(
    val id: String,
    val taskId: String,
    @Serializable(with = InstantSerializer::class)
    val startTime: Instant,
    val plannedDuration: Int = 25, // minutes
    val actualDuration: Int? = null,
    val completedSuccessfully: Boolean = false,
    val interruptionCount: Int = 0,
    val notes: String = ""
)

@Serializable
data class DailySchedule(
    val date: String, // YYYY-MM-DD format
    val timeBlocks: List<TimeBlock>,
    val goals: List<String> = emptyList(),
    val actualProductiveHours: Double? = null,
    val energyLevel: Int? = null, // 1-10 scale
    val moodScore: Int? = null, // 1-10 scale
    val reviewNotes: String = ""
)
