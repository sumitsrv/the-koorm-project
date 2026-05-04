package org.koorm.ocpd.services

import org.koorm.ocpd.models.Task
import org.koorm.ocpd.models.TaskStatus

/**
 * Tone-adjusted notification messages. Platform delivery is wired separately.
 * This service produces the *content* of compassionate reminders / celebrations.
 */
class NotificationService {

    fun celebrateCompletion(task: Task): String = when (task.status) {
        TaskStatus.GOOD_ENOUGH ->
            "Nice — '${task.title}' is good enough, and that's a win. Done > perfect."
        TaskStatus.COMPLETED ->
            "You completed '${task.title}'. Take the credit."
        else ->
            "Progress on '${task.title}'. Keep it gentle."
    }

    fun gentleReminder(task: Task): String =
        "When you're ready: '${task.title}'. No pressure on perfection — just a small step is enough."

    fun stuckEncouragement(task: Task): String =
        "Feeling stuck on '${task.title}' is information, not failure. The next step can be tiny."
}
