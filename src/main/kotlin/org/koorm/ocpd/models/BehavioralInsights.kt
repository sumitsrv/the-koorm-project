package org.koorm.ocpd.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UserPreferences(
    val notificationTone: NotificationTone = NotificationTone.GENTLE,
    val workingHours: WorkingHours = WorkingHours(),
    val pomodoroLength: Int = 25,
    val shortBreakLength: Int = 5,
    val longBreakLength: Int = 15,
    val perfectionism_mode: Boolean = true,
    val enableGoodEnoughMode: Boolean = true,
    val enableAntiProcrastination: Boolean = true,
    val preferredAntiProcrastinationTechnique: AntiProcrastinationTechnique = AntiProcrastinationTechnique.POMODORO,
    val dailyReviewEnabled: Boolean = true,
    val weeklyInsightsEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val minimalistDesign: Boolean = true
)

@Serializable
enum class NotificationTone {
    GENTLE, COACH, FRIEND, FORMAL, MINIMAL
}

@Serializable
data class WorkingHours(
    val startHour: Int = 9,
    val endHour: Int = 17,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5) // Monday to Friday
)

@Serializable
enum class AntiProcrastinationTechnique {
    POMODORO, TWO_MINUTE_RULE, COMMITMENT_CONTRACT, TIME_BOXING
}

@Serializable
data class CognitiveInsight(
    val id: String,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
    val triggerThought: String,
    val emotionalState: String,
    val taskId: String?,
    val procrastinationReason: ProcrastinationReason,
    val intervention: String? = null,
    val wasHelpful: Boolean? = null,
    val reframedThought: String? = null,
    val followUpAction: String? = null
)

@Serializable
enum class ProcrastinationReason {
    PERFECTIONISM, OVERWHELM, UNCLEAR_REQUIREMENTS, FEAR_OF_FAILURE,
    LACK_OF_MOTIVATION, DISTRACTIONS, ENERGY_LOW, TIME_PRESSURE, OTHER
}

@Serializable
data class BehavioralPattern(
    val id: String,
    val patternType: PatternType,
    val description: String,
    val frequency: Int, // times observed
    val confidence: Double, // 0.0 to 1.0
    val suggestions: List<String>,
    @Serializable(with = InstantSerializer::class)
    val identifiedAt: Instant,
    @Serializable(with = InstantSerializer::class)
    val lastOccurrence: Instant
)

@Serializable
enum class PatternType {
    PROCRASTINATION_TRIGGER, PRODUCTIVE_TIME, TASK_AVOIDANCE,
    PERFECTIONISM_BLOCK, ENERGY_PATTERN, FOCUS_PATTERN
}

@Serializable
data class MoodEntry(
    val id: String,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
    val moodScore: Int, // 1-10
    val energyLevel: Int, // 1-10
    val stressLevel: Int, // 1-10
    val notes: String = "",
    val correlatedTasks: List<String> = emptyList()
)
