package org.koorm.ocpd.services

import org.koorm.ocpd.models.Task

/**
 * Local heuristic stand-in for the future LLM-backed assistant.
 *
 * Phase 0 ships only deterministic helpers; real LLM integration arrives
 * in Phase 5 via the LlmClient interface in feature-llm.
 */
class AIAssistantService {

    fun encouragement(task: Task): String {
        val seed = (task.id.hashCode() and Int.MAX_VALUE) % ENCOURAGEMENTS.size
        return ENCOURAGEMENTS[seed].replace("{title}", task.title)
    }

    fun stuckSuggestions(task: Task): List<String> = listOf(
        "Set a 5-minute timer and start any part of '${task.title}' — even the wrong part.",
        "Restate the goal in one sentence. If you can't, that's the first subtask.",
        "Lower the bar: what's the 50% version of '${task.title}'?",
        "Talk it out loud or write a single paragraph about why it feels stuck."
    )

    fun parseNaturalLanguageHints(input: String): NaturalLanguageHints {
        val lower = input.lowercase()
        val priority = when {
            "urgent" in lower || "asap" in lower || "critical" in lower -> "URGENT"
            "important" in lower || "high" in lower -> "HIGH"
            "low" in lower || "whenever" in lower -> "LOW"
            else -> "MEDIUM"
        }
        val category = when {
            listOf("client", "meeting", "email", "report", "boss", "office", "work").any { it in lower } -> "WORK"
            listOf("doctor", "gym", "exercise", "run", "health").any { it in lower } -> "HEALTH"
            listOf("study", "learn", "course", "book", "read").any { it in lower } -> "LEARNING"
            listOf("friend", "family", "dinner", "call mom", "social").any { it in lower } -> "SOCIAL"
            else -> "PERSONAL"
        }
        val dueHint = when {
            "tomorrow" in lower -> "TOMORROW"
            "today" in lower -> "TODAY"
            "next week" in lower -> "NEXT_WEEK"
            else -> null
        }
        // Trim leading "Urgent:" / "Important:" prefixes for the title
        val title = input
            .replace(Regex("^(?i)\\s*(urgent|important|asap|critical|high|low)\\s*:?\\s*"), "")
            .trim()
            .ifEmpty { input.trim() }
        return NaturalLanguageHints(title = title, priority = priority, category = category, dueHint = dueHint)
    }

    data class NaturalLanguageHints(
        val title: String,
        val priority: String,
        val category: String,
        val dueHint: String?
    )

    companion object {
        private val ENCOURAGEMENTS = listOf(
            "'{title}' doesn't have to be perfect — it has to be done.",
            "Tiny progress on '{title}' is still progress.",
            "Start ugly. You can refine later (or not).",
            "You've handled harder things than '{title}'.",
            "Five minutes on '{title}' counts."
        )
    }
}
