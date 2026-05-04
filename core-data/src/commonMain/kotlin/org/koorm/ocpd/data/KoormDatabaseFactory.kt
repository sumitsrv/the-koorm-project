package org.koorm.ocpd.data

import org.koorm.ocpd.data.db.KoormDatabase
import org.koorm.ocpd.secure.SecureKeyStore
import org.koorm.ocpd.secure.databasePassphrase

/**
 * Single entry-point for opening the encrypted SQLDelight database.
 *
 * Pulls the SQLCipher passphrase from [SecureKeyStore] (generating one on
 * first run) and hands the derived [DriverFactory] back to the caller.
 */
fun openKoormDatabase(driverFactory: DriverFactory, secureKeyStore: SecureKeyStore): KoormDatabase {
    val driver = driverFactory.create(passphrase = secureKeyStore.databasePassphrase())
    return KoormDatabase(driver)
}
