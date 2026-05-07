package org.koorm.ocpd.audio

/**
 * Minimal permission surface needed by the audio + notification stack.
 *
 * Phase 3 only models *microphone* and *notifications* (the latter is required
 * on Android 13+ to surface the foreground-service "Listening — tap to stop"
 * notification). Later phases (Phase 5 cloud LLMs / Phase 6 reviewer) do not
 * introduce new runtime permissions, so the surface is deliberately fixed.
 */
public enum class AudioPermission {
    Microphone,

    /**
     * Android 13+ runtime permission for posting notifications. iOS and
     * Desktop implementations report this as already granted, since they
     * either don't require an explicit prompt for the FG-style indicator
     * (iOS orange dot is system-managed) or have no equivalent (Desktop).
     */
    Notifications,
}

public enum class PermissionStatus {
    Granted,
    Denied,
    /** User denied with "don't ask again" / iOS denied. UI must deep-link to settings. */
    PermanentlyDenied,
    /** Initial state before any prompt has been shown. */
    NotDetermined,
}

/**
 * Platform-agnostic permission probe + request.
 *
 * Implementations should be safe to construct outside an Activity (e.g. with
 * application context on Android) but [request] may need to be invoked from
 * an Activity-scoped instance to actually show the system dialog.
 */
public expect class PermissionController {

    public fun status(permission: AudioPermission): PermissionStatus

    /**
     * Request the given permission and suspend until the user has answered
     * (granted, denied once, denied permanently, or — on platforms without
     * a prompt — returned the current status synchronously).
     */
    public suspend fun request(permission: AudioPermission): PermissionStatus
}
