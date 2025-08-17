package org.koorm.ocpd.services

import org.koorm.ocpd.models.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

class BehavioralInsightsService {

    private val insights = mutableListOf<CognitiveInsight>()
    private val patterns = mutableListOf<BehavioralPattern>()
    private val moodEntries = mutableListOf<MoodEntry>()

    fun recordCognitiveInsight(
        triggerThought: String,
        emotionalState: String,
        taskId: String?,
        reason: ProcrastinationReason
    ): CognitiveInsight {
        val insight = CognitiveInsight(
            id = UUID.randomUUID().toString(),
            timestamp = Instant.now(),
            triggerThought = triggerThought,
            emotionalState = emotionalState,
            taskId = taskId,
            procrastinationReason = reason
        )

        insights.add(insight)
        analyzeForPatterns()

        return insight
    }

    fun generateCBTReframe(insight: CognitiveInsight): String {
        return when (insight.procrastinationReason) {
            ProcrastinationReason.PERFECTIONISM -> {
                "Instead of '${insight.triggerThought}', try: 'I can start with a rough version and improve it later. Progress matters more than perfection.'"
            }
            ProcrastinationReason.OVERWHELM -> {
                "Instead of feeling overwhelmed, try: 'I can break this into smaller pieces and tackle just one small part right now.'"
            }
            ProcrastinationReason.UNCLEAR_REQUIREMENTS -> {
                "When requirements are unclear, try: 'I can start by clarifying what I know and identifying specific questions to ask.'"
            }
            ProcrastinationReason.FEAR_OF_FAILURE -> {
                "Instead of fearing failure, try: 'This is an opportunity to learn. Even if it's not perfect, I'll gain valuable experience.'"
            }
            ProcrastinationReason.LACK_OF_MOTIVATION -> {
                "When motivation is low, try: 'I can commit to just 10 minutes. Often starting is the hardest part.'"
            }
            else -> "Remember: You're capable of handling this challenge. Focus on the next small step you can take."
        }
    }

    fun analyzeProductivityPatterns(
        completedTasks: List<Task>,
        timeBlocks: List<TimeBlock>
    ): List<BehavioralPattern> {
        val patterns = mutableListOf<BehavioralPattern>()

        // Analyze peak productivity hours
        val completionsByHour = completedTasks
            .mapNotNull { it.completedAt }
            .groupBy { LocalDateTime.ofInstant(it, ZoneId.systemDefault()).hour }
            .mapValues { it.value.size }

        val peakHour = completionsByHour.maxByOrNull { it.value }?.key
        if (peakHour != null) {
            patterns.add(
                BehavioralPattern(
                    id = UUID.randomUUID().toString(),
                    patternType = PatternType.PRODUCTIVE_TIME,
                    description = "Most productive around ${peakHour}:00",
                    frequency = completionsByHour[peakHour] ?: 0,
                    confidence = 0.8,
                    suggestions = listOf("Schedule important tasks around ${peakHour}:00"),
                    identifiedAt = Instant.now(),
                    lastOccurrence = Instant.now()
                )
            )
        }

        // Analyze perfectionism patterns
        val perfectionismBlocks = insights.count { it.procrastinationReason == ProcrastinationReason.PERFECTIONISM }
        if (perfectionismBlocks > 3) {
            patterns.add(
                BehavioralPattern(
                    id = UUID.randomUUID().toString(),
                    patternType = PatternType.PERFECTIONISM_BLOCK,
                    description = "Frequent perfectionism-related delays",
                    frequency = perfectionismBlocks,
                    confidence = 0.9,
                    suggestions = listOf(
                        "Use 'Good Enough' mode more often",
                        "Set time limits for tasks",
                        "Practice the 80/20 rule"
                    ),
                    identifiedAt = Instant.now(),
                    lastOccurrence = Instant.now()
                )
            )
        }

        return patterns
    }

    fun generateWeeklyInsights(
        tasks: List<Task>,
        timeBlocks: List<TimeBlock>,
        moodEntries: List<MoodEntry>
    ): WeeklyInsightReport {
        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.GOOD_ENOUGH }
        val avgMood = if (moodEntries.isNotEmpty()) moodEntries.map { it.moodScore }.average() else 7.0
        val avgEnergy = if (moodEntries.isNotEmpty()) moodEntries.map { it.energyLevel }.average() else 7.0

        val insights = mutableListOf<String>()

        // Task completion insights
        val completionRate = if (tasks.isNotEmpty()) completedTasks.size.toDouble() / tasks.size else 0.0
        insights.add("You completed ${(completionRate * 100).toInt()}% of your tasks this week")

        // Procrastination pattern insights
        val lastWeek = Instant.now().minus(7, ChronoUnit.DAYS)
        val procrastinationReasons = this.insights
            .filter { it.timestamp > lastWeek }
            .groupBy { it.procrastinationReason }
            .mapValues { it.value.size }

        val topReason = procrastinationReasons.maxByOrNull { it.value }
        if (topReason != null) {
            insights.add("Your main challenge this week was ${topReason.key.name.lowercase().replace('_', ' ')}")
        }

        // Mood and productivity correlation
        if (avgMood > 7 && completionRate > 0.7) {
            insights.add("Great week! High mood and productivity go hand in hand for you")
        } else if (avgMood < 5 && completionRate < 0.5) {
            insights.add("Tough week detected. Consider adjusting your goals and practicing self-compassion")
        }

        return WeeklyInsightReport(
            weekStart = Instant.now().minus(7, ChronoUnit.DAYS),
            weekEnd = Instant.now(),
            completionRate = completionRate,
            averageMood = avgMood,
            averageEnergy = avgEnergy,
            keyInsights = insights,
            patterns = analyzeProductivityPatterns(completedTasks, timeBlocks),
            recommendations = generateRecommendations(procrastinationReasons, avgMood, completionRate)
        )
    }

    private fun generateRecommendations(
        procrastinationReasons: Map<ProcrastinationReason, Int>,
        avgMood: Double,
        completionRate: Double
    ): List<String> {
        val recommendations = mutableListOf<String>()

        val topReason = procrastinationReasons.maxByOrNull { it.value }?.key
        when (topReason) {
            ProcrastinationReason.PERFECTIONISM -> {
                recommendations.add("Try setting 'good enough' standards for non-critical tasks")
                recommendations.add("Use time-boxing to limit perfectionist tendencies")
            }
            ProcrastinationReason.OVERWHELM -> {
                recommendations.add("Break larger tasks into smaller, 15-minute chunks")
                recommendations.add("Consider using the 2-minute rule for quick tasks")
            }
            ProcrastinationReason.UNCLEAR_REQUIREMENTS -> {
                recommendations.add("Spend 5 minutes clarifying requirements before starting tasks")
                recommendations.add("Create a 'Questions to Ask' list for unclear tasks")
            }
            else -> {
                recommendations.add("Continue building consistent habits")
            }
        }

        if (avgMood < 6) {
            recommendations.add("Consider mood-boosting activities before difficult tasks")
            recommendations.add("Practice self-compassion when motivation is low")
        }

        if (completionRate < 0.6) {
            recommendations.add("Try reducing daily task load by 20%")
            recommendations.add("Focus on 1-3 key tasks per day")
        }

        return recommendations
    }

    private fun analyzeForPatterns() {
        // This would contain more sophisticated pattern recognition
        // For now, we'll keep it simple and add pattern detection
        // when there are enough data points
    }

    fun recordMoodEntry(moodScore: Int, energyLevel: Int, stressLevel: Int, notes: String = ""): MoodEntry {
        val entry = MoodEntry(
            id = UUID.randomUUID().toString(),
            timestamp = Instant.now(),
            moodScore = moodScore,
            energyLevel = energyLevel,
            stressLevel = stressLevel,
            notes = notes
        )

        moodEntries.add(entry)
        return entry
    }
}

data class WeeklyInsightReport(
    val weekStart: Instant,
    val weekEnd: Instant,
    val completionRate: Double,
    val averageMood: Double,
    val averageEnergy: Double,
    val keyInsights: List<String>,
    val patterns: List<BehavioralPattern>,
    val recommendations: List<String>
)
