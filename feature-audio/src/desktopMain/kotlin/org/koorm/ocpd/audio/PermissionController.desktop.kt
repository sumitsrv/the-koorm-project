package org.koorm.ocpd.audio

/**
 * Desktop [PermissionController].
 *
 * Desktop OSes don't gate microphone access through an in-app prompt the way
 * mobile platforms do — macOS shows a system prompt the first time the
 * process opens the input device (handled by the JVM/CoreAudio bridge), and
 * Linux/Windows generally allow access unconditionally to the user session.
 * We therefore report `Granted` upfront and let actual capture failures
 * (e.g. macOS denied via System Settings) surface through [AudioRecorder.start].
 *
 * Notifications likewise have no per-app runtime gate on desktop, so they
 * report `Granted`. Phase 7's UI uses an in-app indicator only on desktop.
 */
public actual class PermissionController {

    public actual fun status(permission: AudioPermission): PermissionStatus =
        PermissionStatus.Granted

    public actual suspend fun request(permission: AudioPermission): PermissionStatus =
        PermissionStatus.Granted
}
