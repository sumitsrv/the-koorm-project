package org.koorm.ocpd.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koorm.ocpd.data.db.KoormDatabase
import java.io.File
import java.util.Properties

/**
 * Desktop actual: `sqlite-jdbc` against a file under [dbDir].
 *
 * SQLCipher-JDBC integration is a Phase 8 item — for now the supplied
 * passphrase is **ignored** intentionally. Issuing `PRAGMA key` against
 * vanilla sqlite-jdbc would silently store data unencrypted while creating
 * the false impression that encryption was applied; refusing the pragma is
 * safer until the JNI binding lands.
 */
actual class DriverFactory(private val dbDir: File) {
    actual fun create(passphrase: ByteArray?): SqlDriver {
        dbDir.mkdirs()
        val dbFile = File(dbDir, "koorm.db")
        val isNew = !dbFile.exists()
        val driver = JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            properties = Properties()
        )
        if (isNew) {
            KoormDatabase.Schema.create(driver)
        }
        // Phase 2 ships V1 only; migrations land alongside Phase 6/7 schema
        // changes (adding queries to time_block / conversation / etc.).
        // `passphrase` is intentionally unused on Desktop (see kdoc).
        @Suppress("UNUSED_VARIABLE") val ignored = passphrase
        return driver
    }
}
