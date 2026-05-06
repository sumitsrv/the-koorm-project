package org.koorm.ocpd.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android actual: AES-GCM with a 256-bit master key in AndroidKeyStore.
 * Per-entry ciphertext (with its IV) lives in a private SharedPreferences file.
 */
actual class SecureKeyStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val keyStore: KeyStore by lazy { KeyStore.getInstance(ANDROID_KS).apply { load(null) } }

    private fun masterKey(): SecretKey {
        (keyStore.getEntry(MASTER_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KS)
        kg.init(
            KeyGenParameterSpec.Builder(
                MASTER_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return kg.generateKey()
    }

    actual fun getOrCreateSecret(name: String, sizeBytes: Int): ByteArray {
        getString(name)?.let { return Base64.decode(it, Base64.NO_WRAP) }
        val random = ByteArray(sizeBytes).also { SecureRandom().nextBytes(it) }
        setString(name, Base64.encodeToString(random, Base64.NO_WRAP))
        return random
    }

    actual fun getString(name: String): String? {
        val packed = prefs.getString(name, null) ?: return null
        val parts = packed.split(":")
        if (parts.size != 2) return null
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ct = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(GCM)
        cipher.init(Cipher.DECRYPT_MODE, masterKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ct).decodeToString()
    }

    actual fun setString(name: String, value: String) {
        val cipher = Cipher.getInstance(GCM)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey())
        val iv = cipher.iv
        val ct = cipher.doFinal(value.encodeToByteArray())
        prefs.edit()
            .putString(
                name,
                "${Base64.encodeToString(iv, Base64.NO_WRAP)}:${Base64.encodeToString(ct, Base64.NO_WRAP)}"
            )
            .apply()
    }

    actual fun remove(name: String) {
        prefs.edit().remove(name).apply()
    }

    private companion object {
        const val ANDROID_KS = "AndroidKeyStore"
        const val MASTER_ALIAS = "koorm.master"
        const val PREFS = "koorm.secure"
        const val GCM = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
    }
}
