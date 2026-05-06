package org.koorm.ocpd.data.repos

import kotlinx.datetime.Instant
import org.koorm.ocpd.data.db.KoormDatabase
import org.koorm.ocpd.models.Priority
import org.koorm.ocpd.models.Subtask
import org.koorm.ocpd.models.Task
import org.koorm.ocpd.models.TaskCategory
import org.koorm.ocpd.models.TaskStatus

/**
 * Read/write `Task` + its `Subtask` rows. Phase 2 keeps the API list-based
 * because the manager surface is list-shaped today; Flow-based observation
 * lands in Phase 7 when the Compose UI starts driving from queries.
 */
class TaskRepository(private val db: KoormDatabase) {
    private val tasks get() = db.tasksQueries

    fun getAll(): List<Task> {
        return tasks.selectAll().executeAsList().map { row ->
            val subtasks = tasks.selectSubtasksForTask(row.id).executeAsList().map { s ->
                Subtask(
                    id = s.id,
                    title = s.title,
                    order = s.sort_order.toInt(),
                    estimatedMinutes = s.estimated_minutes.toInt(),
                    completed = s.completed != 0L
                )
            }
            Task(
                id = row.id,
                title = row.title,
                description = row.description,
                priority = runCatching { Priority.valueOf(row.priority) }.getOrDefault(Priority.MEDIUM),
                category = runCatching { TaskCategory.valueOf(row.category) }.getOrDefault(TaskCategory.PERSONAL),
                estimatedDuration = row.estimated_duration.toInt(),
                subtasks = subtasks,
                status = runCatching { TaskStatus.valueOf(row.status) }.getOrDefault(TaskStatus.PENDING),
                completedAt = row.completed_at_epoch_ms?.let { Instant.fromEpochMilliseconds(it) },
                dueDate = row.due_date_epoch_ms?.let { Instant.fromEpochMilliseconds(it) },
                createdAt = row.created_at_epoch_ms?.let { Instant.fromEpochMilliseconds(it) },
                isBreakdownNeeded = row.is_breakdown_needed != 0L,
                goodEnoughCriteria = row.good_enough_criteria
            )
        }
    }

    fun saveAll(items: List<Task>) {
        db.transaction {
            tasks.deleteAll()
            items.forEach { upsert(it) }
        }
    }

    fun upsert(task: Task) {
        db.transaction {
            tasks.upsert(
                id = task.id,
                title = task.title,
                description = task.description,
                priority = task.priority.name,
                category = task.category.name,
                estimated_duration = task.estimatedDuration.toLong(),
                status = task.status.name,
                completed_at_epoch_ms = task.completedAt?.toEpochMilliseconds(),
                due_date_epoch_ms = task.dueDate?.toEpochMilliseconds(),
                created_at_epoch_ms = task.createdAt?.toEpochMilliseconds(),
                is_breakdown_needed = if (task.isBreakdownNeeded) 1L else 0L,
                good_enough_criteria = task.goodEnoughCriteria
            )
            tasks.deleteSubtasksForTask(task.id)
            task.subtasks.forEach { sub ->
                tasks.upsertSubtask(
                    id = sub.id,
                    task_id = task.id,
                    title = sub.title,
                    sort_order = sub.order.toLong(),
                    estimated_minutes = sub.estimatedMinutes.toLong(),
                    completed = if (sub.completed) 1L else 0L
                )
            }
        }
    }

    fun deleteById(id: String) {
        tasks.deleteById(id)
    }
}
