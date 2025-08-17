package org.koorm.ocpd.services

import org.koorm.ocpd.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class ConfigurationService {

    private val configFile = File("user_preferences.json")
    private val json = Json { prettyPrint = true }

    fun loadUserPreferences(): UserPreferences {
        return if (configFile.exists()) {
            try {
                val content = configFile.readText()
                json.decodeFromString<UserPreferences>(content)
            } catch (e: Exception) {
                println("Failed to load preferences, using defaults: ${e.message}")
                UserPreferences()
            }
        } else {
            UserPreferences()
        }
    }

    fun saveUserPreferences(preferences: UserPreferences) {
        try {
            val content = json.encodeToString(preferences)
            configFile.writeText(content)
            println("✅ Preferences saved successfully")
        } catch (e: Exception) {
            println("❌ Failed to save preferences: ${e.message}")
        }
    }

    fun updateNotificationTone(tone: NotificationTone): UserPreferences {
        val current = loadUserPreferences()
        val updated = current.copy(notificationTone = tone)
        saveUserPreferences(updated)
        return updated
    }

    fun updateWorkingHours(startHour: Int, endHour: Int): UserPreferences {
        val current = loadUserPreferences()
        val updated = current.copy(
            workingHours = current.workingHours.copy(
                startHour = startHour,
                endHour = endHour
            )
        )
        saveUserPreferences(updated)
        return updated
    }

    fun toggleGoodEnoughMode(): UserPreferences {
        val current = loadUserPreferences()
        val updated = current.copy(enableGoodEnoughMode = !current.enableGoodEnoughMode)
        saveUserPreferences(updated)
        return updated
    }

    fun updatePomodoroSettings(workLength: Int, shortBreak: Int, longBreak: Int): UserPreferences {
        val current = loadUserPreferences()
        val updated = current.copy(
            pomodoroLength = workLength,
            shortBreakLength = shortBreak,
            longBreakLength = longBreak
        )
        saveUserPreferences(updated)
        return updated
    }
}
