package org.koorm.ocpd.core

import org.koorm.ocpd.models.*
import org.koorm.ocpd.services.*
import kotlinx.coroutines.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

class OCPDAssistantManager {

    private val userPreferences = UserPreferences()
    private val taskBreakdownService = TaskBreakdownService()
    private val notificationService = NotificationService(userPreferences)
    private val timeManagementService = TimeManagementService(userPreferences)
    private val behavioralInsightsService = BehavioralInsightsService()

    private val tasks = mutableListOf<Task>()
    private val schedules = mutableMapOf<String, DailySchedule>()
    private val pomodoroSessions = mutableListOf<PomodoroSession>()

    // Phase 1: MVP Features

    fun createTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        category: TaskCategory = TaskCategory.PERSONAL,
        dueDate: Instant? = null
    ): Task {
        val estimatedDuration = taskBreakdownService.suggestTimeEstimate(title, description)

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = priority,
            category = category,
            estimatedDuration = estimatedDuration,
            createdAt = Instant.now(),
            dueDate = dueDate,
            isBreakdownNeeded = shouldBreakdownTask(title, estimatedDuration)
        )

        val taskWithBreakdown = if (task.isBreakdownNeeded) {
            taskBreakdownService.breakdownTask(task)
        } else task

        tasks.add(taskWithBreakdown)
        return taskWithBreakdown
    }

    private fun shouldBreakdownTask(title: String, estimatedDuration: Int): Boolean {
        return estimatedDuration > 45 ||
               title.lowercase().contains("project") ||
               title.lowercase().contains("research") ||
               title.lowercase().contains("write")
    }

    fun scheduleTask(taskId: String, preferredTime: Instant): TimeBlock? {
        val task = tasks.find { it.id == taskId } ?: return null
        val today = LocalDate.ofInstant(preferredTime, ZoneId.systemDefault()).toString()

        val schedule = schedules.getOrPut(today) {
            DailySchedule(date = today, timeBlocks = emptyList())
        }

        val timeBlock = timeManagementService.createTimeBlock(task, preferredTime)
        val updatedSchedule = schedule.copy(timeBlocks = schedule.timeBlocks + timeBlock)
        schedules[today] = updatedSchedule

        return timeBlock
    }

    fun markTaskCompleted(taskId: String, isGoodEnough: Boolean = false): Task? {
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        if (taskIndex == -1) return null

        val task = tasks[taskIndex]
        val updatedTask = task.copy(
            status = if (isGoodEnough) TaskStatus.GOOD_ENOUGH else TaskStatus.COMPLETED,
            completedAt = Instant.now()
        )

        tasks[taskIndex] = updatedTask

        val celebrationMessage = if (isGoodEnough) {
            notificationService.generateGoodEnoughEncouragement()
        } else {
            notificationService.generateCompletionCelebration(updatedTask)
        }

        println(celebrationMessage)
        return updatedTask
    }

    fun getDailyReview(date: String = LocalDate.now().toString()): DailyReview {
        val todaysTasks = tasks.filter { task ->
            LocalDate.ofInstant(task.createdAt, ZoneId.systemDefault()).toString() == date ||
            task.scheduledTime?.let { LocalDate.ofInstant(it, ZoneId.systemDefault()).toString() } == date
        }

        val completed = todaysTasks.filter { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.GOOD_ENOUGH }
        val pending = todaysTasks.filter { it.status == TaskStatus.NOT_STARTED || it.status == TaskStatus.IN_PROGRESS }

        return DailyReview(
            date = date,
            tasksCompleted = completed,
            tasksPending = pending,
            totalProductiveTime = calculateProductiveTime(date),
            achievements = generateAchievements(completed),
            tomorrowSuggestions = generateTomorrowSuggestions(pending)
        )
    }

    // Phase 2: Smart Assistant Features

    fun parseNaturalLanguageTask(input: String): Task {
        // Simple NLP parsing - in production, this would use more sophisticated NLP
        val lowerInput = input.lowercase()

        val priority = when {
            lowerInput.contains("urgent") || lowerInput.contains("asap") -> Priority.URGENT
            lowerInput.contains("important") || lowerInput.contains("high") -> Priority.HIGH
            lowerInput.contains("low") || lowerInput.contains("when possible") -> Priority.LOW
            else -> Priority.MEDIUM
        }

        val category = when {
            lowerInput.contains("work") || lowerInput.contains("office") || lowerInput.contains("meeting") -> TaskCategory.WORK
            lowerInput.contains("health") || lowerInput.contains("exercise") || lowerInput.contains("doctor") -> TaskCategory.HEALTH
            lowerInput.contains("learn") || lowerInput.contains("study") || lowerInput.contains("course") -> TaskCategory.LEARNING
            lowerInput.contains("creative") || lowerInput.contains("design") || lowerInput.contains("write") -> TaskCategory.CREATIVE
            else -> TaskCategory.PERSONAL
        }

        // Extract due date (simple patterns)
        var dueDate: Instant? = null
        if (lowerInput.contains("tomorrow")) {
            dueDate = Instant.now().plus(1, ChronoUnit.DAYS)
        } else if (lowerInput.contains("next week")) {
            dueDate = Instant.now().plus(7, ChronoUnit.DAYS)
        }

        return createTask(
            title = extractTaskTitle(input),
            description = input,
            priority = priority,
            category = category,
            dueDate = dueDate
        )
    }

    private fun extractTaskTitle(input: String): String {
        // Simple title extraction - would be more sophisticated in production
        val cleaned = input.replace(Regex("(urgent|important|tomorrow|next week|asap)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(for|on|by)\\s+\\w+"), "")
            .trim()

        return if (cleaned.length > 50) cleaned.take(47) + "..." else cleaned
    }

    fun triggerAntiProcrastinationIntervention(taskId: String): AntiProcrastinationSession {
        val task = tasks.find { it.id == taskId } ?: throw IllegalArgumentException("Task not found")

        return when (userPreferences.preferredAntiProcrastinationTechnique) {
            AntiProcrastinationTechnique.POMODORO -> startPomodoroSession(task)
            AntiProcrastinationTechnique.TWO_MINUTE_RULE -> applyTwoMinuteRule(task)
            AntiProcrastinationTechnique.TIME_BOXING -> createTimeBox(task)
            AntiProcrastinationTechnique.COMMITMENT_CONTRACT -> createCommitmentContract(task)
        }
    }

    private fun startPomodoroSession(task: Task): AntiProcrastinationSession {
        val session = timeManagementService.createPomodoroSession(task)
        pomodoroSessions.add(session)

        return AntiProcrastinationSession(
            type = AntiProcrastinationTechnique.POMODORO,
            message = "Starting 25-minute focused session on '${task.title}'. You can do anything for 25 minutes!",
            duration = 25,
            task = task
        )
    }

    private fun applyTwoMinuteRule(task: Task): AntiProcrastinationSession {
        return AntiProcrastinationSession(
            type = AntiProcrastinationTechnique.TWO_MINUTE_RULE,
            message = "Let's try just 2 minutes on '${task.title}'. If it takes less than 2 minutes, finish it. If not, you can stop and plan it properly.",
            duration = 2,
            task = task
        )
    }

    private fun createTimeBox(task: Task): AntiProcrastinationSession {
        val timeBoxDuration = minOf(task.estimatedDuration, 45) // Max 45 minutes
        return AntiProcrastinationSession(
            type = AntiProcrastinationTechnique.TIME_BOXING,
            message = "You have $timeBoxDuration minutes for '${task.title}'. Work until the timer ends, then take a break.",
            duration = timeBoxDuration,
            task = task
        )
    }

    private fun createCommitmentContract(task: Task): AntiProcrastinationSession {
        return AntiProcrastinationSession(
            type = AntiProcrastinationTechnique.COMMITMENT_CONTRACT,
            message = "Commit to working on '${task.title}' for 30 minutes. If you don't, you owe yourself a small penalty (like skipping a treat).",
            duration = 30,
            task = task
        )
    }

    // Phase 3: Behavioral Insights

    fun recordProcrastinationThought(
        thought: String,
        emotion: String,
        taskId: String?,
        reason: ProcrastinationReason
    ): CognitiveInsight {
        val insight = behavioralInsightsService.recordCognitiveInsight(thought, emotion, taskId, reason)
        val reframe = behavioralInsightsService.generateCBTReframe(insight)

        println("Reframed thought: $reframe")
        return insight
    }

    fun generateWeeklyInsights(): WeeklyInsightReport {
        val allSchedules = schedules.values.toList()
        val allTimeBlocks = allSchedules.flatMap { it.timeBlocks }

        return behavioralInsightsService.generateWeeklyInsights(
            tasks = tasks,
            timeBlocks = allTimeBlocks,
            moodEntries = emptyList() // Would be populated with actual mood data
        )
    }

    fun enableStuckMode(taskId: String): StuckModeIntervention {
        val task = tasks.find { it.id == taskId } ?: throw IllegalArgumentException("Task not found")

        return StuckModeIntervention(
            task = task,
            suggestions = listOf(
                "Break this task into the smallest possible first step",
                "Set a 10-minute timer and just start anywhere",
                "Ask yourself: What's the easiest part of this task?",
                "Talk through the task out loud or write about why you're stuck",
                "Change your environment - move to a different location"
            ),
            encouragement = notificationService.generateStartEncouragement(task)
        )
    }

    // Helper methods

    private fun calculateProductiveTime(date: String): Int {
        val schedule = schedules[date] ?: return 0
        return schedule.timeBlocks
            .filter { it.type == TimeBlockType.WORK || it.type == TimeBlockType.DEEP_FOCUS }
            .sumOf {
                ChronoUnit.MINUTES.between(it.startTime, it.endTime).toInt()
            }
    }

    private fun generateAchievements(completedTasks: List<Task>): List<String> {
        val achievements = mutableListOf<String>()

        if (completedTasks.isNotEmpty()) {
            achievements.add("Completed ${completedTasks.size} task${if (completedTasks.size == 1) "" else "s"}")
        }

        val goodEnoughTasks = completedTasks.count { it.status == TaskStatus.GOOD_ENOUGH }
        if (goodEnoughTasks > 0) {
            achievements.add("Practiced 'good enough' mindset $goodEnoughTasks time${if (goodEnoughTasks == 1) "" else "s"}")
        }

        val highPriorityCompleted = completedTasks.count { it.priority == Priority.HIGH || it.priority == Priority.URGENT }
        if (highPriorityCompleted > 0) {
            achievements.add("Tackled $highPriorityCompleted high-priority task${if (highPriorityCompleted == 1) "" else "s"}")
        }

        return achievements
    }

    private fun generateTomorrowSuggestions(pendingTasks: List<Task>): List<String> {
        return pendingTasks.take(3).map { task ->
            when (task.priority) {
                Priority.URGENT -> "🔥 Priority: ${task.title}"
                Priority.HIGH -> "⭐ Important: ${task.title}"
                else -> "📝 Consider: ${task.title}"
            }
        }
    }

    // Getters for UI/API access
    fun getAllTasks(): List<Task> = tasks.toList()
    fun getTaskById(id: String): Task? = tasks.find { it.id == id }
    fun getTodaySchedule(): DailySchedule = schedules[LocalDate.now().toString()]
        ?: DailySchedule(LocalDate.now().toString(), emptyList())
}

data class DailyReview(
    val date: String,
    val tasksCompleted: List<Task>,
    val tasksPending: List<Task>,
    val totalProductiveTime: Int,
    val achievements: List<String>,
    val tomorrowSuggestions: List<String>
)

data class AntiProcrastinationSession(
    val type: AntiProcrastinationTechnique,
    val message: String,
    val duration: Int,
    val task: Task
)

data class StuckModeIntervention(
    val task: Task,
    val suggestions: List<String>,
    val encouragement: String
)
