package org.koorm.ocpd.services

import org.koorm.ocpd.models.*
import java.time.Instant
import java.util.UUID

class TaskBreakdownService {

    private val taskPatterns = mapOf(
        "write" to listOf("Outline", "Research", "Draft", "Edit", "Review", "Submit"),
        "prepare" to listOf("Gather materials", "Review requirements", "Plan approach", "Execute", "Review"),
        "study" to listOf("Preview material", "Read/watch", "Take notes", "Practice", "Review", "Test knowledge"),
        "plan" to listOf("Define goals", "Research options", "Create timeline", "Identify resources", "Draft plan"),
        "organize" to listOf("Assess current state", "Sort items", "Create categories", "Arrange", "Maintain"),
        "clean" to listOf("Gather supplies", "Clear surface", "Deep clean", "Organize", "Final check"),
        "meeting" to listOf("Review agenda", "Prepare materials", "Check tech setup", "Attend meeting", "Follow up"),
        "project" to listOf("Define scope", "Break into phases", "Assign resources", "Execute phases", "Review & close")
    )

    fun breakdownTask(task: Task): Task {
        if (task.subtasks.isNotEmpty() || !task.isBreakdownNeeded) {
            return task
        }

        val subtasks = generateSubtasks(task.title, task.description, task.estimatedDuration)
        return task.copy(subtasks = subtasks)
    }

    private fun generateSubtasks(title: String, description: String, totalDuration: Int): List<Subtask> {
        val titleLower = title.lowercase()
        val descriptionLower = description.lowercase()

        // Find matching pattern
        val pattern = taskPatterns.entries.find { (key, _) ->
            titleLower.contains(key) || descriptionLower.contains(key)
        }?.value

        val baseSteps = pattern ?: generateGenericSteps(title)
        val estimatedTimePerStep = maxOf(5, totalDuration / baseSteps.size)

        return baseSteps.mapIndexed { index, step ->
            Subtask(
                id = UUID.randomUUID().toString(),
                title = step,
                estimatedMinutes = estimatedTimePerStep,
                order = index
            )
        }
    }

    private fun generateGenericSteps(title: String): List<String> {
        return listOf(
            "Plan approach for: $title",
            "Gather necessary resources",
            "Begin initial work",
            "Complete main portion",
            "Review and finalize"
        )
    }

    fun suggestTimeEstimate(title: String, description: String): Int {
        val titleWords = title.split(" ").size
        val descriptionWords = description.split(" ").size

        // Basic heuristic: more words = more complex task
        val baseTime = when {
            titleWords <= 3 && descriptionWords <= 10 -> 15
            titleWords <= 5 && descriptionWords <= 30 -> 30
            titleWords <= 8 && descriptionWords <= 60 -> 60
            else -> 120
        }

        // Adjust based on keywords
        val complexityKeywords = listOf("research", "analyze", "design", "implement", "comprehensive")
        val hasComplexity = complexityKeywords.any {
            title.lowercase().contains(it) || description.lowercase().contains(it)
        }

        return if (hasComplexity) (baseTime * 1.5).toInt() else baseTime
    }
}
