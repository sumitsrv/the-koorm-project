package org.koorm.ocpd.services

// Platform-agnostic file operations interface
interface FileOperations {
    fun readTextFile(filename: String): String?
    fun writeTextFile(filename: String, content: String): Boolean
    fun fileExists(filename: String): Boolean
}

// Expect declaration for platform-specific implementation
expect fun createFileOperations(): FileOperations
