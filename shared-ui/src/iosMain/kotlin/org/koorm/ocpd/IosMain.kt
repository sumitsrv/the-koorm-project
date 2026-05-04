package org.koorm.ocpd

import androidx.compose.ui.window.ComposeUIViewController
import org.koorm.ocpd.core.OCPDAssistantManager
import org.koorm.ocpd.ui.OCPDAssistantApp

/**
 * iOS root view-controller. The caller (Swift) constructs the manager so the
 * Phase 8 Xcode project can wire up SQLCipher/Keychain alongside the iOS
 * `DriverFactory`. Until then, callers can pass `OCPDAssistantManager()` for
 * an in-memory experience.
 */
fun MainViewController(manager: OCPDAssistantManager) =
    ComposeUIViewController { OCPDAssistantApp(manager) }
