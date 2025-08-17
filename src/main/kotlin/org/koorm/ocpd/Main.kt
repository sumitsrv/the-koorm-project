package org.koorm.ocpd

import org.koorm.ocpd.core.OCPDAssistantManager
import org.koorm.ocpd.models.*
import kotlinx.coroutines.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun main() {
    runBlocking {
        val assistant = OCPDAssistantManager()

        println("🧠 OCPD Assistant - Your Compassionate Productivity Companion")
        println("=" * 60)

        // Demonstrate Phase 1: MVP Features
        demonstrateMVPFeatures(assistant)

        println("\n" + "=" * 60)

        // Demonstrate Phase 2: Smart Assistant Features
        demonstrateSmartFeatures(assistant)

        println("\n" + "=" * 60)

        // Demonstrate Phase 3: Behavioral Insights
        demonstrateBehavioralInsights(assistant)

        println("\n" + "=" * 60)
        println("🎉 OCPD Assistant Demo Complete!")
        println("All three phases have been successfully implemented.")
    }
}

private fun demonstrateMVPFeatures(assistant: OCPDAssistantManager) {
    println("\n🚀 PHASE 1: MVP Features Demo")
    println("-" * 40)

    // Create tasks with smart breakdown
    println("\n1. Smart Task Creation & Breakdown:")
    val writeReportTask = assistant.createTask(
        title = "Write quarterly report",
        description = "Comprehensive analysis of Q4 performance",
        priority = Priority.HIGH,
        category = TaskCategory.WORK,
        dueDate = Instant.now().plus(3, ChronoUnit.DAYS)
    )

    println("✅ Created task: ${writeReportTask.title}")
    if (writeReportTask.subtasks.isNotEmpty()) {
        println("📋 Automatically broken down into subtasks:")
        writeReportTask.subtasks.forEach { subtask ->
            println("   ${subtask.order + 1}. ${subtask.title} (${subtask.estimatedMinutes} min)")
        }
    }

    // Time-boxed scheduling
    println("\n2. Time-Boxed Scheduling:")
    val preferredTime = Instant.now().plus(2, ChronoUnit.HOURS)
    val timeBlock = assistant.scheduleTask(writeReportTask.id, preferredTime)

    if (timeBlock != null) {
        val startTime = LocalDateTime.ofInstant(timeBlock.startTime, ZoneId.systemDefault())
        val endTime = LocalDateTime.ofInstant(timeBlock.endTime, ZoneId.systemDefault())
        println("📅 Scheduled: ${timeBlock.title}")
        println("   ⏰ ${startTime.toLocalTime()} - ${endTime.toLocalTime()}")
        println("   🏷️  Type: ${timeBlock.type}")
    }

    // Create more sample tasks
    val quickTask = assistant.createTask(
        title = "Reply to important emails",
        priority = Priority.MEDIUM,
        category = TaskCategory.WORK
    )

    val personalTask = assistant.createTask(
        title = "Plan weekend trip",
        priority = Priority.LOW,
        category = TaskCategory.PERSONAL
    )

    // Demonstrate "Good Enough" completion
    println("\n3. 'Good Enough' Mode:")
    assistant.markTaskCompleted(quickTask.id, isGoodEnough = true)

    // Complete a task normally
    assistant.markTaskCompleted(personalTask.id, isGoodEnough = false)

    // Daily review
    println("\n4. Daily Review:")
    val review = assistant.getDailyReview()
    println("📊 Daily Summary:")
    println("   ✅ Tasks completed: ${review.tasksCompleted.size}")
    println("   ⏳ Tasks pending: ${review.tasksPending.size}")
    println("   🕐 Productive time: ${review.totalProductiveTime} minutes")

    if (review.achievements.isNotEmpty()) {
        println("   🏆 Achievements:")
        review.achievements.forEach { achievement ->
            println("      • $achievement")
        }
    }

    if (review.tomorrowSuggestions.isNotEmpty()) {
        println("   📋 Tomorrow's focus:")
        review.tomorrowSuggestions.forEach { suggestion ->
            println("      • $suggestion")
        }
    }
}

private fun demonstrateSmartFeatures(assistant: OCPDAssistantManager) {
    println("\n🤖 PHASE 2: Smart Assistant Features Demo")
    println("-" * 40)

    // Natural language task input
    println("\n1. Natural Language Task Creation:")
    val nlTasks = listOf(
        "Prepare for Monday's important client meeting",
        "Learn about machine learning for next week",
        "Write creative blog post about productivity",
        "Schedule urgent doctor appointment asap"
    )

    nlTasks.forEach { input ->
        println("🗣️  Input: \"$input\"")
        val task = assistant.parseNaturalLanguageTask(input)
        println("   ✅ Created: ${task.title}")
        println("   🎯 Priority: ${task.priority}")
        println("   📂 Category: ${task.category}")
        println("   ⏱️  Estimated: ${task.estimatedDuration} minutes")
        println()
    }

    // Anti-procrastination interventions
    println("2. Anti-Procrastination Techniques:")
    val allTasks = assistant.getAllTasks()
    val targetTask = allTasks.find { it.status == TaskStatus.NOT_STARTED }

    if (targetTask != null) {
        println("🎯 Targeting task: ${targetTask.title}")

        // Demonstrate different techniques
        val techniques = AntiProcrastinationTechnique.entries
        techniques.forEach { technique ->
            // This would normally be based on user preference
            val session = when (technique) {
                AntiProcrastinationTechnique.POMODORO ->
                    assistant.triggerAntiProcrastinationIntervention(targetTask.id)
                AntiProcrastinationTechnique.TWO_MINUTE_RULE -> {
                    println("🔧 Two-Minute Rule: \"If this takes less than 2 minutes, do it now!\"")
                    null
                }
                AntiProcrastinationTechnique.TIME_BOXING -> {
                    println("📦 Time Boxing: \"Work for exactly 30 minutes, then stop.\"")
                    null
                }
                AntiProcrastinationTechnique.COMMITMENT_CONTRACT -> {
                    println("🤝 Commitment: \"I commit to 25 minutes of focused work.\"")
                    null
                }
            }

            if (session != null) {
                println("${technique.name}: ${session.message}")
            }
        }

        // Stuck Mode intervention
        println("\n3. 'Stuck Mode' Intervention:")
        val stuckIntervention = assistant.enableStuckMode(targetTask.id)
        println("🆘 You seem stuck on: ${stuckIntervention.task.title}")
        println("💡 Here are some gentle suggestions:")
        stuckIntervention.suggestions.forEach { suggestion ->
            println("   • $suggestion")
        }
        println("🤗 Encouragement: ${stuckIntervention.encouragement}")
    }
}

private fun demonstrateBehavioralInsights(assistant: OCPDAssistantManager) {
    println("\n🧠 PHASE 3: Behavioral Insights Demo")
    println("-" * 40)

    // Record cognitive insights (simulate user journaling)
    println("\n1. Cognitive Insight Tracking:")
    val cognitiveThoughts = listOf(
        Triple("I can't start until I have the perfect plan", "anxious", ProcrastinationReason.PERFECTIONISM),
        Triple("This task feels too overwhelming", "stressed", ProcrastinationReason.OVERWHELM),
        Triple("I don't know exactly what they want", "confused", ProcrastinationReason.UNCLEAR_REQUIREMENTS),
        Triple("What if I mess this up completely?", "fearful", ProcrastinationReason.FEAR_OF_FAILURE)
    )

    val allTasks = assistant.getAllTasks()
    val sampleTask = allTasks.firstOrNull()

    cognitiveThoughts.forEach { (thought, emotion, reason) ->
        println("💭 Thought: \"$thought\"")
        println("😟 Emotion: $emotion")
        assistant.recordProcrastinationThought(thought, emotion, sampleTask?.id, reason)
        println("   🔄 CBT Reframe provided automatically")
        println()
    }

    // Weekly insights
    println("2. Weekly Behavioral Analysis:")
    val weeklyReport = assistant.generateWeeklyInsights()

    println("📈 Weekly Report:")
    println("   📊 Completion Rate: ${(weeklyReport.completionRate * 100).toInt()}%")
    println("   😊 Average Mood: ${weeklyReport.averageMood.toInt()}/10")
    println("   ⚡ Average Energy: ${weeklyReport.averageEnergy.toInt()}/10")

    if (weeklyReport.keyInsights.isNotEmpty()) {
        println("   🔍 Key Insights:")
        weeklyReport.keyInsights.forEach { insight ->
            println("      • $insight")
        }
    }

    if (weeklyReport.patterns.isNotEmpty()) {
        println("   🔄 Patterns Detected:")
        weeklyReport.patterns.forEach { pattern ->
            println("      • ${pattern.description} (confidence: ${(pattern.confidence * 100).toInt()}%)")
        }
    }

    if (weeklyReport.recommendations.isNotEmpty()) {
        println("   💡 Recommendations:")
        weeklyReport.recommendations.forEach { recommendation ->
            println("      • $recommendation")
        }
    }

    // Mood tracking demonstration
    println("\n3. Mood & Productivity Correlation:")
    println("📝 Mood tracking helps identify patterns between emotional state and productivity")
    println("🎯 The app learns when you're most productive and adjusts suggestions accordingly")

    // Integration possibilities
    println("\n4. Future Integration Options:")
    println("🔗 Google Calendar sync for seamless scheduling")
    println("💬 Optional therapist sharing for professional support")
    println("📱 Mobile notifications with customizable tone")
    println("🌙 Dark mode and minimalist design for reduced overwhelm")
}

private operator fun String.times(count: Int): String = this.repeat(count)
