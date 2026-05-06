package org.koorm.ocpd

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koorm.ocpd.core.OCPDAssistantManager
import org.koorm.ocpd.data.DriverFactory
import org.koorm.ocpd.data.SqlDataPersistence
import org.koorm.ocpd.data.openKoormDatabase
import org.koorm.ocpd.secure.SecureKeyStore
import org.koorm.ocpd.ui.OCPDAssistantApp
import java.io.File

fun main() {
    val dataDir = File(System.getProperty("user.home") ?: ".", ".koorm")
    val secure = SecureKeyStore(File(dataDir, "secure"))
    val database = openKoormDatabase(DriverFactory(File(dataDir, "db")), secure)
    val persistence = SqlDataPersistence(database)
    val manager = OCPDAssistantManager(persistence = persistence)

    application {
        Window(onCloseRequest = ::exitApplication, title = "OCPD Assistant") {
            MaterialTheme {
                Surface {
                    OCPDAssistantApp(manager)
                }
            }
        }
    }
}
