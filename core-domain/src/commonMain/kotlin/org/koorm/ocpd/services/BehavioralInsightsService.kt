package org.koorm.ocpd.services

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.koorm.ocpd.models.CognitiveInsight
import org.koorm.ocpd.models.MoodEntry
import org.koorm.ocpd.models.ProcrastinationReason
import org.koorm.ocpd.models.Task
import org.koorm.ocpd.models.TaskStatus
import org.koorm.ocpd.models.WeeklyInsightReport

/**
 * Aggregates tasks, mood, and cognitive insights into a weekly trend report.
 * Heuristics are intentionally conservative; ML-driven version arrives in a later phase.
 */
class BehavioralInsightsService {

    fun generateWeekly(
        tasks: List<Task>,
        moods: List<MoodEntry>,
        insights: List<CognitiveInsight>
    ): WeeklyInsightReport {
        val tz = TimeZone.currentSystemDefault()
        val weekStart = Clock.System.now().minus(7, DateTimeUnit.DAY, tz)
        val recentTasks = tasks.filter { it.createdAt == null || it.createdAt >= weekStart }
        val completed = recentTasks.count { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.GOOD_ENOUGH }
        val total = recentTasks.size.coerceAtLeast(1)
        val completionRate = (completed.toDouble() / total).coerceIn(0.0, 1.0)

        val avgMood = if (moods.isEmpty()) 0.0 else moods.map { it.mood }.average()
        val avgEnergy = if (moods.isEmpty()) 0.0 else moods.map { it.energy }.average()

        val reasonFrequencies = insights.groupingBy { it.procrastinationReason }.eachCount()
        val topReasons = reasonFrequencies.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(3)

        val keyInsights = buildList {
            if (completionRate >= 0.7) add("You completed ${(completionRate * 100).toInt()}% of tasks — strong week.")
            if (completionRate < 0.3 && total > 2) add("Completion rate is low; consider scoping fewer tasks next week.")
            if (avgMood in 0.1..4.0) add("Mood trended low — gentleness over output this week.")
            if (ProcrastinationReason.PERFECTIONISM in topReasons) {
                add("Perfectionism showed up multiple times. 'Good Enough' is your friend.")
            }
            if (ProcrastinationReason.OVERWHELM in topReasons) {
                add("Overwhelm appeared often — smaller subtasks may help.")
            }
        }

        val recommendations = buildList {
            if (ProcrastinationReason.PERFECTIONISM in topReasons) {
                add("Set a 'good enough' criterion before starting each task.")
            }
            if (ProcrastinationReason.OVERWHELM in topReasons) {
                add("Cap daily task list at 3 priorities.")
            }
            if (avgEnergy in 0.1..4.0) {
                add("Schedule a buffer block before deep work sessions.")
            }
            if (isEmpty()) add("Keep your current routines — they're working.")
        }

        return WeeklyInsightReport(
            weekStart = weekStart,
            completionRate = completionRate,
            averageMood = avgMood,
            averageEnergy = avgEnergy,
            tasksCompleted = completed,
            keyInsights = keyInsights,
            recommendations = recommendations,
            topProcrastinationReasons = topReasons
        )
    }
}
