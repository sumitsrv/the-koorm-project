package org.koorm.ocpd.services

import java.io.File

// Desktop-specific implementation using Java File API
class DesktopFileOperations : FileOperations {
    override fun readTextFile(filename: String): String? {
        return try {
            val file = File(filename)
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            null
        }
    }

    override fun writeTextFile(filename: String, content: String): Boolean {
        return try {
            File(filename).writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun fileExists(filename: String): Boolean {
        return File(filename).exists()
    }
}

// Desktop-specific actual implementation
actual fun createFileOperations(): FileOperations = DesktopFileOperations()
