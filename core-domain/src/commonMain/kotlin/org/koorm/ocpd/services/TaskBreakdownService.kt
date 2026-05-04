package org.koorm.ocpd.services

import org.koorm.ocpd.models.Subtask
import org.koorm.ocpd.models.Task
import org.koorm.ocpd.models.TaskCategory

/**
 * Heuristic, OCPD-aware task breakdown.
 *
 * Splits tasks into ordered subtasks with conservative time estimates.
 * Tasks shorter than [BREAKDOWN_THRESHOLD_MINUTES] do not need breakdown unless
 * the title hints at multi-step structure.
 */
class TaskBreakdownService {

    fun needsBreakdown(task: Task): Boolean {
        if (task.estimatedDuration >= BREAKDOWN_THRESHOLD_MINUTES) return true
        val t = task.title.lowercase()
        return MULTI_STEP_HINTS.any { it in t }
    }

    fun generateSubtasks(task: Task): List<Subtask> {
        val pattern = matchPattern(task)
        val templates = pattern ?: genericTemplate(task)
        val perStep = (task.estimatedDuration / templates.size).coerceAtLeast(5)
        return templates.mapIndexed { index, title ->
            Subtask(
                id = "${task.id}-sub-$index",
                title = title,
                order = index,
                estimatedMinutes = perStep
            )
        }
    }

    private fun matchPattern(task: Task): List<String>? {
        val t = task.title.lowercase()
        return when {
            "proposal" in t || "report" in t || "document" in t -> listOf(
                "Outline key points and structure",
                "Research and gather supporting material",
                "Draft initial version (rough is fine)",
                "Self-review with 'good enough' lens",
                "Final polish (cap at 15 min)"
            )
            "email" in t || "message" in t -> listOf(
                "Identify the single key ask",
                "Draft a concise version",
                "Trim to essentials",
                "Send"
            )
            "presentation" in t || "slides" in t || "deck" in t -> listOf(
                "Define audience and one takeaway",
                "List 3-5 supporting points",
                "Build draft slides",
                "Rehearse once",
                "Finalize (no more edits after)"
            )
            "meeting" in t || "call" in t -> listOf(
                "Confirm time and attendees",
                "Prepare 3 talking points",
                "Set a clear desired outcome",
                "Attend and take brief notes"
            )
            else -> null
        }
    }

    private fun genericTemplate(task: Task): List<String> = when (task.category) {
        TaskCategory.WORK -> listOf(
            "Clarify the goal in one sentence",
            "Identify the smallest first step",
            "Do the first step",
            "Review progress",
            "Wrap up at 'good enough'"
        )
        TaskCategory.LEARNING -> listOf(
            "Skim the material first",
            "Note 3 questions",
            "Focused study (single pass)",
            "Summarize in your own words"
        )
        else -> listOf(
            "Plan a simple first step",
            "Begin (5-minute starter)",
            "Continue until 'good enough'",
            "Mark complete"
        )
    }

    companion object {
        private const val BREAKDOWN_THRESHOLD_MINUTES = 45
        private val MULTI_STEP_HINTS = listOf(
            "comprehensive", "detailed", "full", "complete",
            "proposal", "report", "presentation", "plan", "review",
            "research", "analysis", "redesign"
        )
    }
}
