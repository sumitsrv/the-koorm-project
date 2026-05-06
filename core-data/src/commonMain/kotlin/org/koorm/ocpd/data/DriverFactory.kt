package org.koorm.ocpd.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Constructs the `SqlDriver` backing [org.koorm.ocpd.data.db.KoormDatabase].
 *
 * Per-platform actuals install the SQLCipher passphrase right after open:
 * - Android wires a `SupportOpenHelperFactory` from `net.zetetic:sqlcipher-android`.
 * - iOS issues `PRAGMA key` against the native driver (effective only when
 *   SQLCipher is linked — pod wiring lands in Phase 8 alongside the Xcode project).
 * - Desktop currently uses vanilla `sqlite-jdbc`; SQLCipher-JDBC binding is on
 *   the Phase 8 hardening list, so the passphrase is accepted but ignored
 *   for now to avoid corrupting an unencrypted DB on first run.
 */
expect class DriverFactory {
    fun create(passphrase: ByteArray?): SqlDriver
}
