package org.koorm.ocpd.services

import org.koorm.ocpd.models.*
import java.time.Instant

class NotificationService(private val userPreferences: UserPreferences) {

    fun generateTaskReminder(task: Task, timeUntilDue: Long): String {
        return when (userPreferences.notificationTone) {
            NotificationTone.GENTLE -> generateGentleReminder(task, timeUntilDue)
            NotificationTone.COACH -> generateCoachReminder(task, timeUntilDue)
            NotificationTone.FRIEND -> generateFriendlyReminder(task, timeUntilDue)
            NotificationTone.FORMAL -> generateFormalReminder(task, timeUntilDue)
            NotificationTone.MINIMAL -> generateMinimalReminder(task)
        }
    }

    private fun generateGentleReminder(task: Task, timeUntilDue: Long): String {
        val timeText = formatTimeUntilDue(timeUntilDue)
        return when {
            timeUntilDue > 3600 -> "When you're ready, '${task.title}' is waiting for you. $timeText remaining."
            timeUntilDue > 1800 -> "Just a gentle reminder: '${task.title}' could use some attention soon."
            else -> "No pressure, but '${task.title}' is coming up. You've got this!"
        }
    }

    private fun generateCoachReminder(task: Task, timeUntilDue: Long): String {
        return when {
            timeUntilDue > 3600 -> "Ready to tackle '${task.title}'? Let's break it down into small wins."
            timeUntilDue > 1800 -> "Time to focus on '${task.title}'. Remember, progress over perfection."
            else -> "Final stretch for '${task.title}'. You're capable of great things!"
        }
    }

    private fun generateFriendlyReminder(task: Task, timeUntilDue: Long): String {
        return when {
            timeUntilDue > 3600 -> "Hey! How about we work on '${task.title}' together? I'm here to help."
            timeUntilDue > 1800 -> "Just checking in - '${task.title}' is on your list. No stress, just when you're ready!"
            else -> "You've got this! '${task.title}' is almost here, but I believe in you."
        }
    }

    private fun generateFormalReminder(task: Task, timeUntilDue: Long): String {
        val timeText = formatTimeUntilDue(timeUntilDue)
        return "Task reminder: '${task.title}' - $timeText remaining."
    }

    private fun generateMinimalReminder(task: Task): String {
        return task.title
    }

    fun generateStartEncouragement(task: Task): String {
        return when (userPreferences.notificationTone) {
            NotificationTone.GENTLE -> "Ready to begin your next step? Even 5 minutes of progress counts."
            NotificationTone.COACH -> "Let's start with just the first small step. You can always build momentum."
            NotificationTone.FRIEND -> "How about we just start? We can figure it out as we go!"
            NotificationTone.FORMAL -> "Beginning task: ${task.title}"
            NotificationTone.MINIMAL -> "Start ${task.title}?"
        }
    }

    fun generateCompletionCelebration(task: Task): String {
        return when (userPreferences.notificationTone) {
            NotificationTone.GENTLE -> "Well done! You completed '${task.title}'. Take a moment to appreciate your progress."
            NotificationTone.COACH -> "Excellent work on '${task.title}'! This is how we build consistent progress."
            NotificationTone.FRIEND -> "Amazing! You finished '${task.title}'! I'm so proud of you!"
            NotificationTone.FORMAL -> "Task completed: ${task.title}"
            NotificationTone.MINIMAL -> "✓ ${task.title}"
        }
    }

    fun generateGoodEnoughEncouragement(): String {
        return when (userPreferences.notificationTone) {
            NotificationTone.GENTLE -> "Sometimes 'good enough' is perfectly fine. You've made meaningful progress."
            NotificationTone.COACH -> "Remember: done is better than perfect. You're building great habits!"
            NotificationTone.FRIEND -> "You know what? That's totally good enough! You should be proud."
            NotificationTone.FORMAL -> "Task marked as complete at acceptable quality threshold."
            NotificationTone.MINIMAL -> "Good enough ✓"
        }
    }

    private fun formatTimeUntilDue(seconds: Long): String {
        return when {
            seconds > 86400 -> "${seconds / 86400} days"
            seconds > 3600 -> "${seconds / 3600} hours"
            seconds > 60 -> "${seconds / 60} minutes"
            else -> "$seconds seconds"
        }
    }
}
