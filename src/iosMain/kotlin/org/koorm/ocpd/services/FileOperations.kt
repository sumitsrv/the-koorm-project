package org.koorm.ocpd.services

import platform.Foundation.*
import platform.darwin.NSObject

// iOS-specific implementation using app's Documents directory
class IosFileOperations : FileOperations {
    private val documentsPath: String by lazy {
        val documentDirectory = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: ""
        "$documentDirectory/ocpd_data"
    }

    init {
        // Create ocpd_data directory if it doesn't exist
        val fileManager = NSFileManager.defaultManager
        val dataDir = NSURL.fileURLWithPath(documentsPath)
        if (!fileManager.fileExistsAtPath(documentsPath)) {
            fileManager.createDirectoryAtURL(
                url = dataDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
    }

    override fun readTextFile(filename: String): String? {
        return try {
            val cleanFilename = filename.substringAfter("/")
            val filePath = "$documentsPath/$cleanFilename"
            val fileManager = NSFileManager.defaultManager

            if (fileManager.fileExistsAtPath(filePath)) {
                NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun writeTextFile(filename: String, content: String): Boolean {
        return try {
            val cleanFilename = filename.substringAfter("/")
            val filePath = "$documentsPath/$cleanFilename"

            // Create parent directories if needed
            val parentPath = NSURL.fileURLWithPath(filePath).URLByDeletingLastPathComponent?.path
            if (parentPath != null) {
                val fileManager = NSFileManager.defaultManager
                if (!fileManager.fileExistsAtPath(parentPath)) {
                    fileManager.createDirectoryAtPath(
                        path = parentPath,
                        withIntermediateDirectories = true,
                        attributes = null,
                        error = null
                    )
                }
            }

            (content as NSString).writeToFile(filePath, true, NSUTF8StringEncoding, null)
        } catch (e: Exception) {
            false
        }
    }

    override fun fileExists(filename: String): Boolean {
        return try {
            val cleanFilename = filename.substringAfter("/")
            val filePath = "$documentsPath/$cleanFilename"
            NSFileManager.defaultManager.fileExistsAtPath(filePath)
        } catch (e: Exception) {
            false
        }
    }
}

// iOS-specific actual implementation
actual fun createFileOperations(): FileOperations = IosFileOperations()
