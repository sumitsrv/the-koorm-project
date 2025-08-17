package org.koorm.ocpd

import org.koorm.ocpd.core.OCPDAssistantManager
import org.koorm.ocpd.models.*
import org.koorm.ocpd.services.*
import java.time.Instant
import kotlin.test.*

class OCPDAssistantTest {

    private lateinit var assistant: OCPDAssistantManager

    @BeforeTest
    fun setup() {
        assistant = OCPDAssistantManager()
    }

    @Test
    fun testTaskCreationAndBreakdown() {
        val task = assistant.createTask(
            title = "Write comprehensive project proposal",
            description = "Create detailed proposal for new client project",
            priority = Priority.HIGH,
            category = TaskCategory.WORK
        )

        assertNotNull(task)
        assertEquals("Write comprehensive project proposal", task.title)
        assertEquals(Priority.HIGH, task.priority)
        assertTrue(task.isBreakdownNeeded)
        assertTrue(task.subtasks.isNotEmpty())

        // Verify subtasks are properly ordered
        task.subtasks.forEachIndexed { index, subtask ->
            assertEquals(index, subtask.order)
            assertTrue(subtask.estimatedMinutes > 0)
        }
    }

    @Test
    fun testNaturalLanguageTaskParsing() {
        val task = assistant.parseNaturalLanguageTask("Urgent: Prepare for important client meeting tomorrow")

        assertEquals(Priority.URGENT, task.priority)
        assertEquals(TaskCategory.WORK, task.category)
        assertNotNull(task.dueDate)
        assertTrue(task.title.contains("client meeting"))
    }

    @Test
    fun testGoodEnoughCompletion() {
        val task = assistant.createTask("Test task", priority = Priority.LOW)
        val completedTask = assistant.markTaskCompleted(task.id, isGoodEnough = true)

        assertNotNull(completedTask)
        assertEquals(TaskStatus.GOOD_ENOUGH, completedTask.status)
        assertNotNull(completedTask.completedAt)
    }

    @Test
    fun testAntiProcrastinationIntervention() {
        val task = assistant.createTask("Procrastinated task", priority = Priority.MEDIUM)
        val intervention = assistant.triggerAntiProcrastinationIntervention(task.id)

        assertNotNull(intervention)
        assertTrue(intervention.message.isNotEmpty())
        assertTrue(intervention.duration > 0)
        assertEquals(task, intervention.task)
    }

    @Test
    fun testStuckModeIntervention() {
        val task = assistant.createTask("Difficult task", priority = Priority.HIGH)
        val stuckIntervention = assistant.enableStuckMode(task.id)

        assertEquals(task, stuckIntervention.task)
        assertTrue(stuckIntervention.suggestions.isNotEmpty())
        assertTrue(stuckIntervention.encouragement.isNotEmpty())
    }

    @Test
    fun testCognitiveInsightTracking() {
        val task = assistant.createTask("Test task for insights")
        val insight = assistant.recordProcrastinationThought(
            "I can't start until it's perfect",
            "anxious",
            task.id,
            ProcrastinationReason.PERFECTIONISM
        )

        assertNotNull(insight)
        assertEquals("I can't start until it's perfect", insight.triggerThought)
        assertEquals("anxious", insight.emotionalState)
        assertEquals(ProcrastinationReason.PERFECTIONISM, insight.procrastinationReason)
    }

    @Test
    fun testDailyReview() {
        // Create and complete some tasks
        val task1 = assistant.createTask("Task 1", priority = Priority.HIGH)
        val task2 = assistant.createTask("Task 2", priority = Priority.MEDIUM)

        assistant.markTaskCompleted(task1.id)
        assistant.markTaskCompleted(task2.id, isGoodEnough = true)

        val review = assistant.getDailyReview()

        assertEquals(2, review.tasksCompleted.size)
        assertTrue(review.achievements.isNotEmpty())
    }

    @Test
    fun testWeeklyInsightsGeneration() {
        // Create some tasks and insights
        val task = assistant.createTask("Weekly test task")
        assistant.recordProcrastinationThought(
            "This is too hard",
            "overwhelmed",
            task.id,
            ProcrastinationReason.OVERWHELM
        )

        val weeklyReport = assistant.generateWeeklyInsights()

        assertNotNull(weeklyReport)
        assertTrue(weeklyReport.completionRate >= 0.0)
        assertTrue(weeklyReport.completionRate <= 1.0)
    }
}
