package org.koorm.ocpd.services

import org.koorm.ocpd.models.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

class TimeManagementService(private val userPreferences: UserPreferences) {

    fun createTimeBlock(
        task: Task,
        preferredStartTime: Instant,
        isFlexible: Boolean = true
    ): TimeBlock {
        val duration = task.estimatedDuration.toLong()
        val endTime = preferredStartTime.plus(duration, ChronoUnit.MINUTES)

        return TimeBlock(
            id = UUID.randomUUID().toString(),
            taskId = task.id,
            title = task.title,
            startTime = preferredStartTime,
            endTime = endTime,
            type = determineTimeBlockType(task),
            isFlexible = isFlexible
        )
    }

    fun scheduleTaskWithBuffers(
        task: Task,
        availableSlots: List<Pair<Instant, Instant>>
    ): TimeBlock? {
        val taskDurationMinutes = task.estimatedDuration.toLong()
        val bufferMinutes = 15L // Buffer time between tasks
        val requiredDuration = taskDurationMinutes + bufferMinutes

        for ((slotStart, slotEnd) in availableSlots) {
            val slotDurationMinutes = ChronoUnit.MINUTES.between(slotStart, slotEnd)

            if (slotDurationMinutes >= requiredDuration) {
                return createTimeBlock(task, slotStart, isFlexible = true)
            }
        }

        return null
    }

    fun createPomodoroSession(task: Task): PomodoroSession {
        return PomodoroSession(
            id = UUID.randomUUID().toString(),
            taskId = task.id,
            startTime = Instant.now(),
            plannedDuration = userPreferences.pomodoroLength
        )
    }

    fun optimizeScheduleForOCPD(
        tasks: List<Task>,
        existingSchedule: DailySchedule
    ): DailySchedule {
        val optimizedBlocks = mutableListOf<TimeBlock>()
        optimizedBlocks.addAll(existingSchedule.timeBlocks)

        // Sort tasks by priority and deadline
        val sortedTasks = tasks.sortedWith(
            compareBy<Task> { it.priority.ordinal }
                .thenBy { it.dueDate }
        )

        // Schedule high-priority tasks during peak energy times
        val peakHours = listOf(9, 10, 11, 14, 15) // Typical peak productivity hours

        for (task in sortedTasks) {
            if (task.scheduledTime == null) {
                val idealTime = findIdealTimeSlot(task, peakHours, optimizedBlocks)
                if (idealTime != null) {
                    val timeBlock = createTimeBlock(task, idealTime)
                    optimizedBlocks.add(timeBlock)
                }
            }
        }

        return existingSchedule.copy(timeBlocks = optimizedBlocks)
    }

    private fun findIdealTimeSlot(
        task: Task,
        peakHours: List<Int>,
        existingBlocks: List<TimeBlock>
    ): Instant? {
        val today = Instant.now()
        val startOfDay = LocalDateTime.now()
            .withHour(userPreferences.workingHours.startHour)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneId.systemDefault())
            .toInstant()

        // Try to find a slot during peak hours first for important tasks
        if (task.priority == Priority.HIGH || task.priority == Priority.URGENT) {
            for (hour in peakHours) {
                val candidateTime = startOfDay.plus((hour - userPreferences.workingHours.startHour).toLong(), ChronoUnit.HOURS)
                if (isTimeSlotAvailable(candidateTime, task.estimatedDuration, existingBlocks)) {
                    return candidateTime
                }
            }
        }

        // Fall back to any available slot
        val workingHours = userPreferences.workingHours.endHour - userPreferences.workingHours.startHour
        for (hour in 0 until workingHours) {
            val candidateTime = startOfDay.plus(hour.toLong(), ChronoUnit.HOURS)
            if (isTimeSlotAvailable(candidateTime, task.estimatedDuration, existingBlocks)) {
                return candidateTime
            }
        }

        return null
    }

    private fun isTimeSlotAvailable(
        startTime: Instant,
        durationMinutes: Int,
        existingBlocks: List<TimeBlock>
    ): Boolean {
        val endTime = startTime.plus(durationMinutes.toLong(), ChronoUnit.MINUTES)

        return existingBlocks.none { block ->
            (startTime >= block.startTime && startTime < block.endTime) ||
            (endTime > block.startTime && endTime <= block.endTime) ||
            (startTime <= block.startTime && endTime >= block.endTime)
        }
    }

    private fun determineTimeBlockType(task: Task): TimeBlockType {
        return when {
            task.title.lowercase().contains("meeting") -> TimeBlockType.WORK
            task.priority == Priority.HIGH || task.priority == Priority.URGENT -> TimeBlockType.DEEP_FOCUS
            task.category == TaskCategory.ADMIN -> TimeBlockType.ADMIN
            task.estimatedDuration <= 15 -> TimeBlockType.ADMIN
            else -> TimeBlockType.WORK
        }
    }

    fun suggestBreaks(schedule: DailySchedule): List<TimeBlock> {
        val breaks = mutableListOf<TimeBlock>()
        val workBlocks = schedule.timeBlocks.filter { it.type == TimeBlockType.WORK || it.type == TimeBlockType.DEEP_FOCUS }

        for (i in 0 until workBlocks.size - 1) {
            val currentBlock = workBlocks[i]
            val nextBlock = workBlocks[i + 1]

            val timeBetween = ChronoUnit.MINUTES.between(currentBlock.endTime, nextBlock.startTime)

            if (timeBetween >= 10) {
                breaks.add(
                    TimeBlock(
                        id = UUID.randomUUID().toString(),
                        taskId = null,
                        title = "Break",
                        startTime = currentBlock.endTime,
                        endTime = nextBlock.startTime,
                        type = TimeBlockType.BREAK,
                        isFlexible = true
                    )
                )
            }
        }

        return breaks
    }
}
