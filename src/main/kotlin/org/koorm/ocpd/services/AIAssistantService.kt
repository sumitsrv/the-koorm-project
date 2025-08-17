package org.koorm.ocpd.services

import org.koorm.ocpd.models.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class AIAssistantService {

    // Simulated AI responses for demo - in production this would connect to OpenAI API
    private val taskBreakdownPatterns = mapOf(
        "research" to listOf(
            "Define research scope and objectives",
            "Identify key sources and databases",
            "Gather preliminary information",
            "Analyze and categorize findings",
            "Synthesize key insights",
            "Prepare summary report"
        ),
        "presentation" to listOf(
            "Define audience and objectives",
            "Create outline and structure",
            "Gather supporting materials",
            "Design slides and visuals",
            "Practice and rehearse",
            "Final review and adjustments"
        ),
        "analysis" to listOf(
            "Collect and organize data",
            "Apply analytical framework",
            "Identify patterns and trends",
            "Draw conclusions",
            "Validate findings",
            "Document results"
        )
    )

    suspend fun enhancedTaskBreakdown(task: Task): List<Subtask> {
        // Simulate AI processing delay
        delay(100)

        val titleLower = task.title.lowercase()
        val descriptionLower = task.description.lowercase()

        // Find best matching pattern
        val bestMatch = taskBreakdownPatterns.entries.find { (key, _) ->
            titleLower.contains(key) || descriptionLower.contains(key)
        }

        val steps = bestMatch?.value ?: generateContextualSteps(task)
        val estimatedTimePerStep = calculateOptimalTimeDistribution(task.estimatedDuration, steps.size)

        return steps.mapIndexed { index, step ->
            Subtask(
                id = java.util.UUID.randomUUID().toString(),
                title = step,
                estimatedMinutes = estimatedTimePerStep[index],
                order = index
            )
        }
    }

    private fun generateContextualSteps(task: Task): List<String> {
        val keywords = (task.title + " " + task.description).lowercase().split(" ")

        return when {
            keywords.any { it in listOf("write", "document", "report") } -> listOf(
                "Outline structure and key points",
                "Research and gather information",
                "Write first draft",
                "Review and revise content",
                "Proofread and finalize"
            )
            keywords.any { it in listOf("plan", "organize", "schedule") } -> listOf(
                "Define goals and constraints",
                "Brainstorm options and approaches",
                "Evaluate alternatives",
                "Create detailed plan",
                "Review and adjust timeline"
            )
            keywords.any { it in listOf("learn", "study", "understand") } -> listOf(
                "Survey material and set learning goals",
                "Active reading/watching with notes",
                "Practice with examples or exercises",
                "Test understanding and identify gaps",
                "Review and reinforce key concepts"
            )
            else -> listOf(
                "Understand requirements and scope",
                "Plan approach and gather resources",
                "Execute main work",
                "Review and refine results",
                "Complete and document outcome"
            )
        }
    }

    private fun calculateOptimalTimeDistribution(totalMinutes: Int, stepCount: Int): List<Int> {
        // Distribute time with front-loading for planning and back-loading for review
        val baseTime = totalMinutes / stepCount
        val distribution = when (stepCount) {
            1 -> listOf(totalMinutes)
            2 -> listOf((totalMinutes * 0.6).toInt(), (totalMinutes * 0.4).toInt())
            3 -> listOf(
                (totalMinutes * 0.3).toInt(),
                (totalMinutes * 0.5).toInt(),
                (totalMinutes * 0.2).toInt()
            )
            else -> {
                val planning = (totalMinutes * 0.2).toInt()
                val execution = (totalMinutes * 0.6 / (stepCount - 2)).toInt()
                val review = (totalMinutes * 0.2).toInt()

                listOf(planning) + List(stepCount - 2) { execution } + listOf(review)
            }
        }

        return distribution
    }

    suspend fun generatePersonalizedEncouragement(
        user: UserPreferences,
        recentCompletions: List<Task>,
        currentStruggle: ProcrastinationReason
    ): String {
        delay(50) // Simulate AI processing

        val recentSuccesses = recentCompletions.size
        val baseEncouragement = when (user.notificationTone) {
            NotificationTone.GENTLE -> "Take your time, "
            NotificationTone.COACH -> "You've got this! "
            NotificationTone.FRIEND -> "Hey, remember "
            NotificationTone.FORMAL -> "Please note: "
            NotificationTone.MINIMAL -> ""
        }

        val personalizedMessage = when (currentStruggle) {
            ProcrastinationReason.PERFECTIONISM -> {
                if (recentSuccesses > 0) {
                    "you completed $recentSuccesses task${if (recentSuccesses == 1) "" else "s"} recently, and they didn't need to be perfect to be valuable."
                } else {
                    "perfectionism often prevents us from starting. What if 'good enough' led to something wonderful?"
                }
            }
            ProcrastinationReason.OVERWHELM -> {
                "when things feel overwhelming, focus on just the next small step. You don't have to see the whole staircase."
            }
            ProcrastinationReason.FEAR_OF_FAILURE -> {
                if (recentSuccesses > 0) {
                    "you've succeeded before ($recentSuccesses recent completions!), and you have the skills to handle this too."
                } else {
                    "failure is just learning in disguise. What's the smallest experiment you could try?"
                }
            }
            else -> "every small step forward is progress worth celebrating."
        }

        return baseEncouragement + personalizedMessage
    }

    suspend fun analyzeProductivityContext(
        tasks: List<Task>,
        timeBlocks: List<TimeBlock>,
        insights: List<CognitiveInsight>
    ): ProductivityAnalysis {
        delay(200) // Simulate complex AI analysis

        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.GOOD_ENOUGH }
        val completionRate = if (tasks.isNotEmpty()) completedTasks.size.toDouble() / tasks.size else 0.0

        // Analyze time patterns
        val hourlyCompletions = completedTasks
            .mapNotNull { it.completedAt }
            .groupBy { LocalDateTime.ofInstant(it, ZoneId.systemDefault()).hour }
            .mapValues { it.value.size }

        val peakHour = hourlyCompletions.maxByOrNull { it.value }?.key

        // Analyze procrastination patterns
        val topProcrastinationReason = insights
            .groupBy { it.procrastinationReason }
            .maxByOrNull { it.value.size }?.key

        // Generate insights
        val aiInsights = mutableListOf<String>()

        if (completionRate > 0.8) {
            aiInsights.add("Exceptional productivity! You're completing ${(completionRate * 100).toInt()}% of your tasks.")
        } else if (completionRate < 0.5) {
            aiInsights.add("Consider reducing task load or breaking tasks into smaller pieces.")
        }

        peakHour?.let { hour ->
            aiInsights.add("You're most productive around ${hour}:00. Try scheduling important tasks then.")
        }

        topProcrastinationReason?.let { reason ->
            when (reason) {
                ProcrastinationReason.PERFECTIONISM ->
                    aiInsights.add("Perfectionism is your main challenge. Practice 'good enough' standards.")
                ProcrastinationReason.OVERWHELM ->
                    aiInsights.add("Tasks often feel overwhelming. Focus on smaller breakdowns.")
                else -> aiInsights.add("Main challenge: ${reason.name.lowercase().replace('_', ' ')}")
            }
        }

        return ProductivityAnalysis(
            completionRate = completionRate,
            peakProductivityHour = peakHour,
            mainChallenge = topProcrastinationReason,
            insights = aiInsights,
            recommendations = generateSmartRecommendations(completionRate, peakHour, topProcrastinationReason)
        )
    }

    private fun generateSmartRecommendations(
        completionRate: Double,
        peakHour: Int?,
        mainChallenge: ProcrastinationReason?
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (completionRate < 0.6) {
            recommendations.add("Try limiting yourself to 3 main tasks per day")
            recommendations.add("Use time-boxing to prevent over-commitment")
        }

        peakHour?.let { hour ->
            recommendations.add("Block ${hour}:00-${hour+2}:00 for your most important work")
        }

        when (mainChallenge) {
            ProcrastinationReason.PERFECTIONISM -> {
                recommendations.add("Set 'Version 1.0' goals rather than perfect outcomes")
                recommendations.add("Use the 80/20 rule: 80% done is often enough")
            }
            ProcrastinationReason.OVERWHELM -> {
                recommendations.add("Break tasks into 15-minute chunks")
                recommendations.add("Use the 2-minute rule for quick wins")
            }
            ProcrastinationReason.UNCLEAR_REQUIREMENTS -> {
                recommendations.add("Start each task by clarifying what 'done' looks like")
            }
            else -> {
                recommendations.add("Focus on consistent daily progress over perfect execution")
            }
        }

        return recommendations
    }
}

@Serializable
data class ProductivityAnalysis(
    val completionRate: Double,
    val peakProductivityHour: Int?,
    val mainChallenge: ProcrastinationReason?,
    val insights: List<String>,
    val recommendations: List<String>
)
