package org.koorm.ocpd.audio

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * iOS [PermissionController].
 *
 * - Microphone uses `AVAudioSession.recordPermission`. iOS 17+ also exposes
 *   `AVAudioApplication.requestRecordPermission`; we use the legacy API
 *   intentionally for broader deployment-target coverage. Callers should
 *   ensure `NSMicrophoneUsageDescription` is set in `Info.plist` (Phase 8).
 * - Notifications go through `UNUserNotificationCenter`. We request alert-only
 *   authorization (no badge / sound) to keep the prompt minimal — the
 *   Listening indicator we'll surface in Phase 7 is in-app, not via push.
 *
 * On iOS there is no first-class concept of "permanently denied" distinct
 * from "denied" — both surface as `Denied`. The deep-link to Settings is the
 * UX remedy in either case.
 */
public actual class PermissionController {

    public actual fun status(permission: AudioPermission): PermissionStatus {
        return when (permission) {
            AudioPermission.Microphone -> {
                @Suppress("DEPRECATION")
                when (AVAudioSession.sharedInstance().recordPermission) {
                    AVAudioSessionRecordPermissionGranted -> PermissionStatus.Granted
                    AVAudioSessionRecordPermissionDenied -> PermissionStatus.Denied
                    AVAudioSessionRecordPermissionUndetermined -> PermissionStatus.NotDetermined
                    else -> PermissionStatus.NotDetermined
                }
            }
            AudioPermission.Notifications -> {
                // Synchronous status read isn't available on UN; we cache the
                // last known value when request() is invoked. Default to
                // NotDetermined when never asked.
                lastNotificationStatus
            }
        }
    }

    public actual suspend fun request(permission: AudioPermission): PermissionStatus {
        return when (permission) {
            AudioPermission.Microphone -> requestMicrophone()
            AudioPermission.Notifications -> requestNotifications()
        }
    }

    private suspend fun requestMicrophone(): PermissionStatus =
        suspendCancellableCoroutine { cont ->
            @Suppress("DEPRECATION")
            AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                cont.resume(if (granted) PermissionStatus.Granted else PermissionStatus.Denied)
            }
        }

    private suspend fun requestNotifications(): PermissionStatus =
        suspendCancellableCoroutine { cont ->
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert) { granted, _ ->
                val status = if (granted) PermissionStatus.Granted else PermissionStatus.Denied
                lastNotificationStatus = status
                cont.resume(status)
            }
            // Best-effort cache refresh — UN delivers the granted bool above
            // but does not directly expose "permanently denied" vs "denied".
            center.getNotificationSettingsWithCompletionHandler { settings ->
                if (settings == null) return@getNotificationSettingsWithCompletionHandler
                lastNotificationStatus = when (settings.authorizationStatus) {
                    UNAuthorizationStatusAuthorized,
                    UNAuthorizationStatusProvisional,
                        -> PermissionStatus.Granted
                    UNAuthorizationStatusDenied -> PermissionStatus.Denied
                    UNAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
                    else -> lastNotificationStatus
                }
            }
        }

    private companion object {
        @Volatile
        private var lastNotificationStatus: PermissionStatus = PermissionStatus.NotDetermined
    }
}

// AVAudioApplication exists on iOS 17+ but we deliberately don't gate on it
// here; the legacy AVAudioSession APIs remain functional. Phase 8 may revisit
// once the deployment target is finalized.
