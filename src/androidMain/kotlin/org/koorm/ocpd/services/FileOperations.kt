package org.koorm.ocpd.services

import android.content.Context
import java.io.File

// Android-specific implementation using app's internal storage
class AndroidFileOperations(private val context: Context) : FileOperations {
    private val dataDir = File(context.filesDir, "ocpd_data")

    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }

    override fun readTextFile(filename: String): String? {
        return try {
            val file = File(dataDir, filename.substringAfter("/"))
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            null
        }
    }

    override fun writeTextFile(filename: String, content: String): Boolean {
        return try {
            val file = File(dataDir, filename.substringAfter("/"))
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun fileExists(filename: String): Boolean {
        return try {
            File(dataDir, filename.substringAfter("/")).exists()
        } catch (e: Exception) {
            false
        }
    }
}

// We'll need to initialize this with context from the Android app
private lateinit var androidFileOps: AndroidFileOperations

fun initializeAndroidFileOperations(context: Context) {
    androidFileOps = AndroidFileOperations(context)
}

// Android-specific actual implementation
actual fun createFileOperations(): FileOperations = androidFileOps
