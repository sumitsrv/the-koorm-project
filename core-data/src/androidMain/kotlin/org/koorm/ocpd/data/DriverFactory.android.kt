package org.koorm.ocpd.data

import android.content.Context
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.koorm.ocpd.data.db.KoormDatabase

/**
 * Android actual: AndroidSqliteDriver wired with SQLCipher's
 * [SupportOpenHelperFactory] when a passphrase is supplied. With no
 * passphrase, falls back to plaintext SQLite — caller-controlled so unit
 * tests on JVM can opt out.
 */
actual class DriverFactory(private val context: Context) {
    actual fun create(passphrase: ByteArray?): SqlDriver {
        // SQLCipher's SupportOpenHelperFactory mutates the byte array
        // internally, so pass a defensive copy and wipe ours.
        val factory = if (passphrase != null) {
            // Loads the prebuilt SQLCipher native libs bundled with the AAR.
            net.zetetic.database.sqlcipher.SQLiteDatabase.loadLibs(context)
            SupportOpenHelperFactory(passphrase.copyOf())
        } else {
            FrameworkSQLiteOpenHelperFactory()
        }
        return AndroidSqliteDriver(
            schema = KoormDatabase.Schema,
            context = context,
            name = "koorm.db",
            factory = factory
        )
    }
}
