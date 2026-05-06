package org.koorm.ocpd.secure

/**
 * Persistent secret storage for sensitive material.
 *
 * Phase 2 uses this for the SQLCipher database passphrase. Phase 5 will reuse
 * the same surface for cloud-LLM API keys (OpenAI / Anthropic / Gemini).
 *
 * Per-platform actuals:
 * - Android: AndroidKeyStore-backed AES-GCM, ciphertext in SharedPreferences.
 * - iOS:     Keychain Services (`kSecClassGenericPassword`).
 * - Desktop: AES-GCM file with sibling random key file (Phase 8 will replace
 *            the keyfile with DPAPI / Security.framework / libsecret via JNA).
 *
 * Construction is platform-specific (e.g. requires `Context` on Android), so the
 * `expect class` deliberately omits a constructor — wire the actual at app entry.
 */
expect class SecureKeyStore {
    /** Returns the secret bound to [name]; generates a fresh CSPRNG one on first call. */
    fun getOrCreateSecret(name: String, sizeBytes: Int = 32): ByteArray

    fun getString(name: String): String?
    fun setString(name: String, value: String)
    fun remove(name: String)
}

/** Convenience: well-known key for the SQLCipher database passphrase. */
const val KEY_DB_PASSPHRASE: String = "koorm.db.passphrase"

fun SecureKeyStore.databasePassphrase(): ByteArray =
    getOrCreateSecret(KEY_DB_PASSPHRASE, sizeBytes = 32)
