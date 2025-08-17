package org.koorm.ocpd

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.koorm.ocpd.ui.OCPDAssistantApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        CanvasBasedWindow(
            canvasElementId = "ComposeTarget",
            title = "OCPD Assistant"
        ) {
            OCPDAssistantApp()
        }
    } catch (e: Exception) {
        console.error("Failed to initialize application:", e.message)
        // Fallback error handling
        js("""
            document.getElementById('loading').style.display = 'none';
            document.getElementById('error-message').style.display = 'block';
        """)
    }
}
