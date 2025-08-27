package org.koorm.ocpd

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.koorm.ocpd.ui.OCPDAssistantApp
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        CanvasBasedWindow(
            canvasElementId = "ComposeTarget",
            title = "OCPD Assistant"
        ) {
            OCPDAssistantApp()
        }
        // Notify loader script that Compose started (best-effort)
        try {
            js("if (window.__OCPD_APP_STARTED__) window.__OCPD_APP_STARTED__();")
        } catch (_: dynamic) { }
    } catch (e: Exception) {
        console.error("Failed to initialize application:", e)
        // Fallback: manipulate DOM directly without dynamic js string interpolation
        val loading = document.getElementById("loading") as? HTMLElement
        val error = document.getElementById("error-message") as? HTMLElement
        val title = document.getElementById("error-title") as? HTMLElement
        val body = document.getElementById("error-body") as? HTMLElement
        val details = document.getElementById("error-details") as? HTMLElement
        loading?.style?.display = "none"
        error?.style?.display = "block"
        title?.textContent = "Initialization Error"
        body?.textContent = "An unexpected error prevented the app from starting. See console for details."
        details?.let {
            it.style.display = "block"
            it.textContent = e.message ?: "Unknown error"
        }
    }
}
