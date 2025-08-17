package org.koorm.ocpd.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val subtasks: List<Subtask> = emptyList(),
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val estimatedDuration: Int = 30, // minutes
    val actualDuration: Int? = null,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val dueDate: Instant? = null,
    @Serializable(with = InstantSerializer::class)
    val scheduledTime: Instant? = null,
    @Serializable(with = InstantSerializer::class)
    val completedAt: Instant? = null,
    val perfectionism_threshold: Int = 80, // "Good enough" threshold
    val tags: List<String> = emptyList(),
    val category: TaskCategory = TaskCategory.PERSONAL,
    val isBreakdownNeeded: Boolean = false,
    val procrastinationTriggers: List<String> = emptyList()
)

@Serializable
data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val estimatedMinutes: Int = 10,
    val order: Int
)

@Serializable
enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

@Serializable
enum class TaskStatus {
    NOT_STARTED, IN_PROGRESS, BLOCKED, COMPLETED, GOOD_ENOUGH
}

@Serializable
enum class TaskCategory {
    WORK, PERSONAL, HEALTH, LEARNING, CREATIVE, ADMIN
}
