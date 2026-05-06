package org.koorm.ocpd.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koorm.ocpd.models.PomodoroSession
import org.koorm.ocpd.models.Schedule
import org.koorm.ocpd.models.Task
import org.koorm.ocpd.models.TimeBlock
import org.koorm.ocpd.models.TimeBlockType

/**
 * Generates buffer-aware time blocks and Pomodoro sessions.
 *
 * Buffer-awareness: every block is followed by a [BUFFER_MINUTES] BUFFER block
 * to prevent OCPD-driven over-scheduling. Lunch is reserved.
 */
class TimeManagementService {

    fun buildDefaultSchedule(date: LocalDate = todayLocal(), tasks: List<Task>): Schedule {
        val blocks = mutableListOf<TimeBlock>()
        var cursor = LocalTime(9, 0)
        val endOfDay = LocalTime(17, 0)
        val lunch = LocalTime(12, 30)

        var idx = 0
        for (task in tasks.sortedBy { it.priority.ordinal }) {
            if (cursor >= endOfDay) break
            // Lunch break
            if (cursor < lunch && cursor.minute == 30 && cursor.hour == 12) {
                blocks += TimeBlock(
                    id = "lunch-${date}",
                    title = "Lunch",
                    startTime = lunch,
                    endTime = LocalTime(13, 30),
                    type = TimeBlockType.BREAK
                )
                cursor = LocalTime(13, 30)
                continue
            }
            val duration = task.estimatedDuration.coerceAtMost(90)
            val end = addMinutes(cursor, duration).coerceAtMost(endOfDay)
            blocks += TimeBlock(
                id = "tb-${date}-$idx",
                title = task.title,
                startTime = cursor,
                endTime = end,
                type = TimeBlockType.DEEP_WORK,
                taskId = task.id
            )
            idx++
            cursor = addMinutes(end, BUFFER_MINUTES)
            if (cursor < endOfDay) {
                blocks += TimeBlock(
                    id = "buf-${date}-$idx",
                    title = "Buffer",
                    startTime = end,
                    endTime = cursor,
                    type = TimeBlockType.BUFFER
                )
                idx++
            }
        }
        return Schedule(date = date, timeBlocks = blocks)
    }

    fun startPomodoro(taskId: String?): PomodoroSession = PomodoroSession(
        id = "pom-${Clock.System.now().toEpochMilliseconds()}",
        taskId = taskId,
        startTimeEpochMs = Clock.System.now().toEpochMilliseconds()
    )

    private fun addMinutes(time: LocalTime, minutes: Int): LocalTime {
        val total = time.hour * 60 + time.minute + minutes
        val h = (total / 60).coerceIn(0, 23)
        val m = (total % 60).coerceIn(0, 59)
        return LocalTime(h, m)
    }

    private fun todayLocal(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    companion object {
        private const val BUFFER_MINUTES = 10
    }
}
