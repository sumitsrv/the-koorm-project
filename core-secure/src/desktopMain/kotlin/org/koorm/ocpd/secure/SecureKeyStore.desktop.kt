package org.koorm.ocpd.secure

import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop actual — file-based AES-GCM with a sibling random key file.
 *
 * The keyfile is written with restrictive permissions where the JVM exposes
 * them (POSIX-style; on Windows we fall back to the default ACL). This is
 * acceptable for Phase 2 because the SQLCipher passphrase is the only thing
 * stored, and accessing it requires the user's own filesystem privileges.
 *
 * Phase 8 will swap this for platform-probing JNA actuals — DPAPI on Windows,
 * `Security.framework` on macOS, libsecret on Linux — falling back to this
 * file-based path only when no system vault is reachable.
 */
actual class SecureKeyStore(baseDir: File) {

    private val storeDir: File = baseDir.also { it.mkdirs() }
    private val storeFile = File(storeDir, "secure.properties")
    private val keyFile = File(storeDir, "secure.key")

    private val masterKey: SecretKeySpec by lazy { loadOrCreateMasterKey() }

    actual fun getOrCreateSecret(name: String, sizeBytes: Int): ByteArray {
        getString(name)?.let { return Base64.getDecoder().decode(it) }
        val random = ByteArray(sizeBytes).also { SecureRandom().nextBytes(it) }
        setString(name, Base64.getEncoder().encodeToString(random))
        return random
    }

    actual fun getString(name: String): String? {
        val packed = readProperty(name) ?: return null
        val parts = packed.split(":")
        if (parts.size != 2) return null
        val iv = Base64.getDecoder().decode(parts[0])
        val ct = Base64.getDecoder().decode(parts[1])
        val cipher = Cipher.getInstance(GCM)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ct), Charsets.UTF_8)
    }

    actual fun setString(name: String, value: String) {
        val cipher = Cipher.getInstance(GCM)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        val iv = cipher.iv
        val ct = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val packed = "${Base64.getEncoder().encodeToString(iv)}:${Base64.getEncoder().encodeToString(ct)}"
        writeProperty(name, packed)
    }

    actual fun remove(name: String) {
        val props = readAllProps().toMutableMap()
        if (props.remove(name) != null) writeAllProps(props)
    }

    private fun loadOrCreateMasterKey(): SecretKeySpec {
        if (keyFile.exists()) {
            return SecretKeySpec(keyFile.readBytes(), "AES")
        }
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        keyFile.writeBytes(raw)
        runCatching {
            keyFile.setReadable(false, false); keyFile.setReadable(true, true)
            keyFile.setWritable(false, false); keyFile.setWritable(true, true)
        } // best-effort; Windows ACL is not POSIX
        return SecretKeySpec(raw, "AES")
    }

    private fun readProperty(name: String): String? = readAllProps()[name]

    private fun writeProperty(name: String, value: String) {
        val props = readAllProps().toMutableMap()
        props[name] = value
        writeAllProps(props)
    }

    private fun readAllProps(): Map<String, String> {
        if (!storeFile.exists()) return emptyMap()
        return storeFile.readLines()
            .filter { it.contains('=') && !it.startsWith('#') }
            .associate { line ->
                val idx = line.indexOf('=')
                line.substring(0, idx) to line.substring(idx + 1)
            }
    }

    private fun writeAllProps(props: Map<String, String>) {
        val text = props.entries.joinToString("\n") { (k, v) -> "$k=$v" }
        storeFile.writeText(text)
    }

    private companion object {
        const val GCM = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
    }
}
