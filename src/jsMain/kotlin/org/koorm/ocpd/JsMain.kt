package org.koorm.ocpd

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.koorm.ocpd.ui.OCPDAssistantApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        OCPDAssistantApp()
    }
}
