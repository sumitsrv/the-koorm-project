package org.koorm.ocpd.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class Priority { URGENT, HIGH, MEDIUM, LOW }

@Serializable
enum class TaskCategory { PERSONAL, WORK, HEALTH, LEARNING, SOCIAL }

@Serializable
enum class TaskStatus { PENDING, IN_PROGRESS, COMPLETED, GOOD_ENOUGH, CANCELLED }

@Serializable
data class Subtask(
    val id: String,
    val title: String,
    val order: Int,
    val estimatedMinutes: Int,
    val completed: Boolean = false
)

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val estimatedDuration: Int = 30, // minutes
    val subtasks: List<Subtask> = emptyList(),
    val status: TaskStatus = TaskStatus.PENDING,
    val completedAt: Instant? = null,
    val dueDate: Instant? = null,
    val createdAt: Instant? = null,
    val isBreakdownNeeded: Boolean = false,
    val goodEnoughCriteria: String? = null
)
