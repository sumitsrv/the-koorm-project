package org.koorm.ocpd.services

// JavaScript-specific implementation using browser localStorage
class JsFileOperations : FileOperations {
    override fun readTextFile(filename: String): String? {
        return try {
            js("localStorage.getItem(filename)") as? String
        } catch (e: Exception) {
            null
        }
    }

    override fun writeTextFile(filename: String, content: String): Boolean {
        return try {
            js("localStorage.setItem(filename, content)")
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun fileExists(filename: String): Boolean {
        return try {
            js("localStorage.getItem(filename) !== null") as Boolean
        } catch (e: Exception) {
            false
        }
    }
}

// JavaScript-specific actual implementation
actual fun createFileOperations(): FileOperations = JsFileOperations()
