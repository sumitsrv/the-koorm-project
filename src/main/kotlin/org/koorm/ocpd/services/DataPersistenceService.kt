package org.koorm.ocpd.services

import org.koorm.ocpd.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.time.Instant
import java.io.File

class DataPersistenceService {

    private val dataDir = File("ocpd_data")
    private val tasksFile = File(dataDir, "tasks.json")
    private val schedulesFile = File(dataDir, "schedules.json")
    private val insightsFile = File(dataDir, "insights.json")
    private val json = Json { prettyPrint = true }

    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }

    // Task persistence
    fun saveTasks(tasks: List<Task>) {
        try {
            val content = json.encodeToString(tasks)
            tasksFile.writeText(content)
        } catch (e: Exception) {
            println("Failed to save tasks: ${e.message}")
        }
    }

    fun loadTasks(): List<Task> {
        return if (tasksFile.exists()) {
            try {
                val content = tasksFile.readText()
                json.decodeFromString<List<Task>>(content)
            } catch (e: Exception) {
                println("Failed to load tasks: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Schedule persistence
    fun saveSchedules(schedules: Map<String, DailySchedule>) {
        try {
            val content = json.encodeToString(schedules)
            schedulesFile.writeText(content)
        } catch (e: Exception) {
            println("Failed to save schedules: ${e.message}")
        }
    }

    fun loadSchedules(): Map<String, DailySchedule> {
        return if (schedulesFile.exists()) {
            try {
                val content = schedulesFile.readText()
                json.decodeFromString<Map<String, DailySchedule>>(content)
            } catch (e: Exception) {
                println("Failed to load schedules: ${e.message}")
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    // Insights persistence
    fun saveInsights(insights: List<CognitiveInsight>) {
        try {
            val content = json.encodeToString(insights)
            insightsFile.writeText(content)
        } catch (e: Exception) {
            println("Failed to save insights: ${e.message}")
        }
    }

    fun loadInsights(): List<CognitiveInsight> {
        return if (insightsFile.exists()) {
            try {
                val content = insightsFile.readText()
                json.decodeFromString<List<CognitiveInsight>>(content)
            } catch (e: Exception) {
                println("Failed to load insights: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Backup and export functionality
    fun exportUserData(): String {
        val userData = mapOf(
            "tasks" to loadTasks(),
            "schedules" to loadSchedules(),
            "insights" to loadInsights(),
            "exportedAt" to Instant.now()
        )

        return json.encodeToString(userData)
    }

    fun clearAllData() {
        listOf(tasksFile, schedulesFile, insightsFile).forEach { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        println("All user data cleared")
    }
}
