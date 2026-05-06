package org.koorm.ocpd.secure

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Foundation.dataWithBase64EncodedString
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.darwin.OSStatus

/**
 * iOS actual — **Phase 2 placeholder**.
 *
 * Stores base64-encoded secrets in `NSUserDefaults`, which is **not** secure
 * storage on iOS (sandboxed but readable from a backed-up device). This is a
 * temporary shim so the iOS target compiles and the SQLCipher passphrase has
 * a stable home; it will be replaced by Keychain Services
 * (`kSecClassGenericPassword`) in a focused follow-up once we have a Mac build
 * host to verify the cinterop calls. Tracked under Phase 2.x of the plan.
 */
@OptIn(ExperimentalForeignApi::class)
actual class SecureKeyStore(@Suppress("unused") private val service: String = "org.koorm.ocpd") {

    private val defaults: NSUserDefaults get() = NSUserDefaults.standardUserDefaults

    actual fun getOrCreateSecret(name: String, sizeBytes: Int): ByteArray {
        val existing = defaults.stringForKey(secretKey(name))
        if (existing != null) {
            val data = NSData.dataWithBase64EncodedString(existing, 0u)
            if (data != null) return data.toByteArray()
        }
        val random = generateRandomBytes(sizeBytes)
        val encoded = random.toNSData().base64EncodedStringWithOptions(0u)
        defaults.setObject(encoded, secretKey(name))
        return random
    }

    actual fun getString(name: String): String? = defaults.stringForKey(stringKey(name))

    actual fun setString(name: String, value: String) {
        defaults.setObject(value, stringKey(name))
    }

    actual fun remove(name: String) {
        defaults.removeObjectForKey(secretKey(name))
        defaults.removeObjectForKey(stringKey(name))
    }

    private fun secretKey(name: String): String = "koorm.secret.$name"
    private fun stringKey(name: String): String = "koorm.string.$name"

    private fun generateRandomBytes(size: Int): ByteArray = memScoped {
        val out = ByteArray(size)
        val status: OSStatus = SecRandomCopyBytes(
            kSecRandomDefault,
            size.toULong(),
            out.refTo(0).getPointer(this)
        )
        check(status == errSecSuccess) { "SecRandomCopyBytes failed: $status" }
        out
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(
        bytes = this@toNSData.refTo(0).getPointer(this),
        length = this@toNSData.size.toULong()
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = memScoped {
    val len = length.toInt()
    val out = ByteArray(len)
    if (len > 0) {
        platform.posix.memcpy(out.refTo(0).getPointer(this), bytes, len.toULong())
    }
    out
}
