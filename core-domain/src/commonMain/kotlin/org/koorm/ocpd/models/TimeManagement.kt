package org.koorm.ocpd.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
enum class TimeBlockType { DEEP_WORK, SHALLOW_WORK, BREAK, BUFFER, MEETING, PERSONAL, EXERCISE }

@Serializable
data class TimeBlock(
    val id: String,
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val type: TimeBlockType = TimeBlockType.DEEP_WORK,
    val taskId: String? = null,
    val notes: String = ""
)

@Serializable
data class Schedule(
    val date: LocalDate,
    val timeBlocks: List<TimeBlock> = emptyList()
)

@Serializable
data class PomodoroSession(
    val id: String,
    val taskId: String?,
    val startTimeEpochMs: Long,
    val workMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val completed: Boolean = false
)
