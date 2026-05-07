package org.koorm.ocpd.audio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * Android [PermissionController].
 *
 * Two construction modes:
 * - **Application context** (`PermissionController(applicationContext)`):
 *   [status] works; [request] returns the current status without prompting,
 *   because we have no Activity to anchor the dialog to.
 * - **Activity** (`PermissionController(activity)`):
 *   [request] suspends until the system dialog resolves. The host Activity
 *   must forward [Activity.onRequestPermissionsResult] to
 *   [onRequestPermissionsResult].
 *
 * Two-Activity-instance edge cases (process death etc.) are out of scope here
 * and will be handled by the Phase 7 onboarding flow's own state machine.
 */
public actual class PermissionController(
    private val context: Context,
) {

    private val activity: Activity? = context as? Activity

    public actual fun status(permission: AudioPermission): PermissionStatus {
        return when (permission) {
            AudioPermission.Microphone ->
                statusFor(Manifest.permission.RECORD_AUDIO)
            AudioPermission.Notifications -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    PermissionStatus.Granted
                } else {
                    statusFor(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    public actual suspend fun request(permission: AudioPermission): PermissionStatus {
        val act = activity ?: return status(permission)
        if (permission == AudioPermission.Notifications &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            return PermissionStatus.Granted
        }
        val androidPerm = when (permission) {
            AudioPermission.Microphone -> Manifest.permission.RECORD_AUDIO
            AudioPermission.Notifications -> Manifest.permission.POST_NOTIFICATIONS
        }
        if (statusFor(androidPerm) == PermissionStatus.Granted) {
            return PermissionStatus.Granted
        }
        val requestCode = nextRequestCode.incrementAndGet()
        return suspendCancellableCoroutine { cont ->
            pending[requestCode] = { granted, neverAskAgain ->
                cont.resume(
                    when {
                        granted -> PermissionStatus.Granted
                        neverAskAgain -> PermissionStatus.PermanentlyDenied
                        else -> PermissionStatus.Denied
                    }
                )
            }
            cont.invokeOnCancellation { pending.remove(requestCode) }
            ActivityCompat.requestPermissions(act, arrayOf(androidPerm), requestCode)
        }
    }

    private fun statusFor(androidPerm: String): PermissionStatus {
        val granted = ContextCompat.checkSelfPermission(context, androidPerm) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) return PermissionStatus.Granted
        // Activity is required to disambiguate Denied vs PermanentlyDenied vs
        // NotDetermined; from a non-Activity context we only know "not granted".
        val act = activity ?: return PermissionStatus.NotDetermined
        return if (ActivityCompat.shouldShowRequestPermissionRationale(act, androidPerm)) {
            PermissionStatus.Denied
        } else {
            // Either NotDetermined (never asked) or PermanentlyDenied. Without
            // tracking prior prompts we can't tell; conservatively report
            // NotDetermined and let the request() flow upgrade to
            // PermanentlyDenied if the system returns "don't ask again".
            PermissionStatus.NotDetermined
        }
    }

    public companion object {
        private val nextRequestCode = AtomicInteger(0xA0DC0)
        private val pending = ConcurrentHashMap<Int, (Boolean, Boolean) -> Unit>()

        /**
         * Forward Activity.onRequestPermissionsResult here so the matching
         * suspended [request] call can resume.
         */
        public fun onRequestPermissionsResult(
            activity: Activity,
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
        ) {
            val callback = pending.remove(requestCode) ?: return
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            val rationale = permissions.firstOrNull()?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            } ?: false
            // Per Android docs: if denied AND shouldShowRationale is false,
            // the user has selected "don't ask again" (i.e. permanently denied).
            val neverAskAgain = !granted && !rationale
            callback(granted, neverAskAgain)
        }
    }
}
