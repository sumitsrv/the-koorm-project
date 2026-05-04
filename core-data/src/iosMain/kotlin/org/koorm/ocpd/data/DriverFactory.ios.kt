package org.koorm.ocpd.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koorm.ocpd.data.db.KoormDatabase

/**
 * iOS actual.
 *
 * `PRAGMA key` is issued after open; it only encrypts the database when the
 * SQLCipher iOS pod is linked into the app. Phase 8 lands the Xcode project
 * and the SQLCipher pod wiring; until then, the database is unencrypted on
 * iOS and the passphrase is silently no-op.
 */
actual class DriverFactory {
    actual fun create(passphrase: ByteArray?): SqlDriver {
        val driver = NativeSqliteDriver(KoormDatabase.Schema, "koorm.db")
        if (passphrase != null) {
            val hex = passphrase.joinToString(separator = "") { byte ->
                ((byte.toInt() and 0xFF) + 0x100).toString(16).substring(1)
            }
            driver.execute(identifier = null, sql = "PRAGMA key = \"x'$hex'\";", parameters = 0)
        }
        return driver
    }
}
