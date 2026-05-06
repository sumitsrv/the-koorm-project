package org.koorm.ocpd.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class ProcrastinationReason {
    PERFECTIONISM,
    OVERWHELM,
    UNCLEAR_GOALS,
    FEAR_OF_FAILURE,
    LACK_OF_ENERGY,
    DISTRACTION,
    BOREDOM,
    UNKNOWN
}

@Serializable
data class MoodEntry(
    val id: String,
    val timestamp: Instant,
    val mood: Int, // 1..10
    val energy: Int, // 1..10
    val note: String = ""
)

@Serializable
data class CognitiveInsight(
    val id: String,
    val timestamp: Instant,
    val triggerThought: String,
    val emotionalState: String,
    val taskId: String? = null,
    val procrastinationReason: ProcrastinationReason = ProcrastinationReason.UNKNOWN,
    val reframe: String? = null
)

@Serializable
data class WeeklyInsightReport(
    val weekStart: Instant,
    val completionRate: Double, // 0.0..1.0
    val averageMood: Double, // 0.0..10.0
    val averageEnergy: Double, // 0.0..10.0
    val tasksCompleted: Int = 0,
    val keyInsights: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val topProcrastinationReasons: List<ProcrastinationReason> = emptyList()
)

data class DailyReview(
    val date: Instant,
    val tasksCompleted: List<Task>,
    val achievements: List<String>,
    val reflectionPrompts: List<String> = emptyList()
)

data class StuckModeIntervention(
    val task: Task,
    val suggestions: List<String>,
    val encouragement: String
)

data class AntiProcrastinationIntervention(
    val task: Task,
    val message: String,
    val duration: Int // minutes
)
