package org.koorm.ocpd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import org.koorm.ocpd.core.OCPDAssistantManager
import org.koorm.ocpd.data.DriverFactory
import org.koorm.ocpd.data.SqlDataPersistence
import org.koorm.ocpd.data.openKoormDatabase
import org.koorm.ocpd.secure.SecureKeyStore
import org.koorm.ocpd.ui.OCPDAssistantApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext
        val secure = SecureKeyStore(app)
        val database = openKoormDatabase(DriverFactory(app), secure)
        val persistence = SqlDataPersistence(database)
        val manager = OCPDAssistantManager(persistence = persistence)

        setContent {
            MaterialTheme {
                Surface { OCPDAssistantApp(manager) }
            }
        }
    }
}
