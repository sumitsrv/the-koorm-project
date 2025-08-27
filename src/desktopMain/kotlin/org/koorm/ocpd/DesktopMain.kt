package org.koorm.ocpd

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koorm.ocpd.ui.OCPDAssistantApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "OCPD Assistant") {
        MaterialTheme {
            Surface {
                OCPDAssistantApp()
            }
        }
    }
}
