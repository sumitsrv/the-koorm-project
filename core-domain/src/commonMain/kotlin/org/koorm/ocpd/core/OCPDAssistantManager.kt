package org.koorm.ocpd.core

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koorm.ocpd.models.AntiProcrastinationIntervention
import org.koorm.ocpd.models.CognitiveInsight
import org.koorm.ocpd.models.DailyReview
import org.koorm.ocpd.models.MoodEntry
import org.koorm.ocpd.models.Priority
import org.koorm.ocpd.models.ProcrastinationReason
import org.koorm.ocpd.models.Schedule
import org.koorm.ocpd.models.StuckModeIntervention
import org.koorm.ocpd.models.Task
import org.koorm.ocpd.models.TaskCategory
import org.koorm.ocpd.models.TaskStatus
import org.koorm.ocpd.models.WeeklyInsightReport
import org.koorm.ocpd.services.AIAssistantService
import org.koorm.ocpd.services.BehavioralInsightsService
import org.koorm.ocpd.services.ConfigurationService
import org.koorm.ocpd.services.DataPersistenceService
import org.koorm.ocpd.services.NotificationService
import org.koorm.ocpd.services.TaskBreakdownService
import org.koorm.ocpd.services.TimeManagementService

/**
 * Top-level facade orchestrating tasks, schedules, mood, insights, and AI helpers.
 *
 * In-memory state plus best-effort persistence via [DataPersistenceService].
 * The persistence port is satisfied by either the legacy
 * [org.koorm.ocpd.services.JsonDataPersistenceService] or the SQLDelight
 * implementation in `:core-data` (`SqlDataPersistence`).
 */
class OCPDAssistantManager(
    private val taskBreakdown: TaskBreakdownService = TaskBreakdownService(),
    private val timeManagement: TimeManagementService = TimeManagementService(),
    private val notifications: NotificationService = NotificationService(),
    private val insights: BehavioralInsightsService = BehavioralInsightsService(),
    private val ai: AIAssistantService = AIAssistantService(),
    private val persistence: DataPersistenceService? = null,
    private val configuration: ConfigurationService? = null
) {
    private val tasks = mutableMapOf<String, Task>()
    private val moods = mutableListOf<MoodEntry>()
    private val cognitiveInsights = mutableListOf<CognitiveInsight>()
    private var idCounter = 0L

    init {
        persistence?.loadTasks()?.forEach { tasks[it.id] = it }
        persistence?.loadMoods()?.let { moods.addAll(it) }
        persistence?.loadInsights()?.let { cognitiveInsights.addAll(it) }
    }

    // ---------- Task lifecycle ----------

    fun createTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        category: TaskCategory = TaskCategory.PERSONAL,
        estimatedDuration: Int = configuration?.load()?.defaultTaskMinutes ?: 30
    ): Task {
        val now = Clock.System.now()
        val id = nextId("task")
        val skeleton = Task(
            id = id,
            title = title,
            description = description,
            priority = priority,
            category = category,
            estimatedDuration = estimatedDuration,
            createdAt = now
        )
        val needsBreakdown = taskBreakdown.needsBreakdown(skeleton)
        val task = if (needsBreakdown) {
            skeleton.copy(
                isBreakdownNeeded = true,
                subtasks = taskBreakdown.generateSubtasks(skeleton)
            )
        } else {
            skeleton
        }
        tasks[id] = task
        persistTasks()
        return task
    }

    fun parseNaturalLanguageTask(input: String): Task {
        val hints = ai.parseNaturalLanguageHints(input)
        val priority = runCatching { Priority.valueOf(hints.priority) }.getOrDefault(Priority.MEDIUM)
        val category = runCatching { TaskCategory.valueOf(hints.category) }.getOrDefault(TaskCategory.PERSONAL)
        val due = when (hints.dueHint) {
            "TOMORROW" -> Clock.System.now().plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            "TODAY" -> Clock.System.now()
            "NEXT_WEEK" -> Clock.System.now().plus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            else -> null
        }
        val created = createTask(
            title = hints.title,
            description = "",
            priority = priority,
            category = category
        )
        val withDue = created.copy(dueDate = due)
        tasks[withDue.id] = withDue
        persistTasks()
        return withDue
    }

    fun markTaskCompleted(taskId: String, isGoodEnough: Boolean = false): Task {
        val existing = tasks[taskId] ?: error("Unknown task: $taskId")
        val updated = existing.copy(
            status = if (isGoodEnough) TaskStatus.GOOD_ENOUGH else TaskStatus.COMPLETED,
            completedAt = Clock.System.now()
        )
        tasks[taskId] = updated
        persistTasks()
        return updated
    }

    fun getAllTasks(): List<Task> = tasks.values.sortedBy { it.priority.ordinal }

    // ---------- Interventions ----------

    fun triggerAntiProcrastinationIntervention(taskId: String): AntiProcrastinationIntervention {
        val task = tasks[taskId] ?: error("Unknown task: $taskId")
        return AntiProcrastinationIntervention(
            task = task,
            message = "Start tiny on '${task.title}'. A 5-minute attempt is success.",
            duration = 5
        )
    }

    fun enableStuckMode(taskId: String): StuckModeIntervention {
        val task = tasks[taskId] ?: error("Unknown task: $taskId")
        return StuckModeIntervention(
            task = task,
            suggestions = ai.stuckSuggestions(task),
            encouragement = ai.encouragement(task)
        )
    }

    // ---------- Cognitive / mood tracking ----------

    fun recordProcrastinationThought(
        triggerThought: String,
        emotionalState: String,
        taskId: String? = null,
        reason: ProcrastinationReason = ProcrastinationReason.UNKNOWN
    ): CognitiveInsight {
        val insight = CognitiveInsight(
            id = nextId("insight"),
            timestamp = Clock.System.now(),
            triggerThought = triggerThought,
            emotionalState = emotionalState,
            taskId = taskId,
            procrastinationReason = reason
        )
        cognitiveInsights += insight
        persistence?.saveInsights(cognitiveInsights.toList())
        return insight
    }

    fun recordMood(mood: Int, energy: Int, note: String = ""): MoodEntry {
        val entry = MoodEntry(
            id = nextId("mood"),
            timestamp = Clock.System.now(),
            mood = mood.coerceIn(1, 10),
            energy = energy.coerceIn(1, 10),
            note = note
        )
        moods += entry
        persistence?.saveMoods(moods.toList())
        return entry
    }

    // ---------- Reviews ----------

    fun getDailyReview(): DailyReview {
        val tz = TimeZone.currentSystemDefault()
        val startOfDay = Clock.System.now().minus(24, DateTimeUnit.HOUR, tz)
        val completedToday = tasks.values.filter {
            (it.status == TaskStatus.COMPLETED || it.status == TaskStatus.GOOD_ENOUGH) &&
                (it.completedAt?.let { c -> c >= startOfDay } ?: false)
        }
        val achievements = buildList {
            if (completedToday.isNotEmpty()) {
                add("Completed ${completedToday.size} task${if (completedToday.size == 1) "" else "s"} today.")
            }
            val goodEnough = completedToday.count { it.status == TaskStatus.GOOD_ENOUGH }
            if (goodEnough > 0) add("Practiced 'good enough' $goodEnough time${if (goodEnough == 1) "" else "s"}.")
            val urgent = completedToday.count { it.priority == Priority.URGENT }
            if (urgent > 0) add("Cleared $urgent urgent task${if (urgent == 1) "" else "s"}.")
            if (isEmpty()) add("Showed up today — that counts.")
        }
        return DailyReview(
            date = Clock.System.now(),
            tasksCompleted = completedToday,
            achievements = achievements
        )
    }

    fun generateWeeklyInsights(): WeeklyInsightReport =
        insights.generateWeekly(tasks.values.toList(), moods.toList(), cognitiveInsights.toList())

    fun getTodaySchedule(): Schedule =
        timeManagement.buildDefaultSchedule(tasks = tasks.values.filter { it.status == TaskStatus.PENDING })

    // ---------- Internal ----------

    private fun nextId(prefix: String): String {
        idCounter += 1
        val ts = Clock.System.now().toEpochMilliseconds()
        return "$prefix-$ts-$idCounter"
    }

    private fun persistTasks() {
        persistence?.saveTasks(tasks.values.toList())
    }
}
