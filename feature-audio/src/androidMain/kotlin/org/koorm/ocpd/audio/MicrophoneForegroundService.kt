package org.koorm.ocpd.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service that keeps an [AudioRecorder] alive while the app is
 * backgrounded.
 *
 * Lifecycle:
 * - [start] from any context -> service starts in `microphone` FG type and
 *   posts the persistent "Listening — tap to stop" notification.
 * - The owning ViewModel obtains the running [AudioRecorder] via the
 *   [recorder] static accessor; the service does NOT consume audio itself.
 *   This split keeps STT / Reviewer wiring (Phase 4 / 6) in the app process,
 *   while still satisfying Android 14+ FG-microphone rules.
 * - [stop] from notification action or in-app stop button -> service stops,
 *   recorder released, notification dismissed.
 *
 * The notification's content intent is intentionally generic — the app
 * supplies the destination Activity at start-time via [Intent.putExtra]
 * with key [EXTRA_CONTENT_INTENT_PI] (a [PendingIntent]). If absent, the
 * notification is non-tappable but still visible (tap to launch is a polish
 * concern; the legal/UX requirement is that the user knows the mic is hot).
 */
public class MicrophoneForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopRecorder()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val contentPi = intent?.getParcelableExtra<PendingIntent>(EXTRA_CONTENT_INTENT_PI)
                val notification = buildNotification(this, contentPi)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIF_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
                    )
                } else {
                    startForeground(NOTIF_ID, notification)
                }
                if (recorderInstance == null) {
                    recorderInstance = AudioRecorder(applicationContext)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopRecorder()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopRecorder() {
        recorderInstance?.stop()
        recorderInstance = null
    }

    public companion object {
        public const val CHANNEL_ID: String = "koorm.mic"
        public const val NOTIF_ID: Int = 0xC00
        public const val ACTION_START: String = "org.koorm.ocpd.audio.ACTION_START"
        public const val ACTION_STOP: String = "org.koorm.ocpd.audio.ACTION_STOP"
        public const val EXTRA_CONTENT_INTENT_PI: String = "org.koorm.ocpd.audio.EXTRA_PI"

        @Volatile
        private var recorderInstance: AudioRecorder? = null

        /**
         * Returns the [AudioRecorder] owned by the running service, or `null`
         * if the service is not currently in the foreground. Callers should
         * use [start] first, then poll this from the UI layer.
         */
        public val recorder: AudioRecorder? get() = recorderInstance

        public fun start(context: Context, contentIntent: PendingIntent? = null) {
            val ctx = context.applicationContext
            val intent = Intent(ctx, MicrophoneForegroundService::class.java).apply {
                action = ACTION_START
                if (contentIntent != null) putExtra(EXTRA_CONTENT_INTENT_PI, contentIntent)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }

        public fun stop(context: Context) {
            val ctx = context.applicationContext
            val intent = Intent(ctx, MicrophoneForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            ctx.startService(intent)
        }

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val mgr = context.getSystemService(NotificationManager::class.java) ?: return
            if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Microphone session",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Indicates that a listening session is active."
                setShowBadge(false)
            }
            mgr.createNotificationChannel(channel)
        }

        private fun buildNotification(
            context: Context,
            contentIntent: PendingIntent?,
        ): Notification {
            val stopIntent = Intent(context, MicrophoneForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            val stopPi = PendingIntent.getService(
                context,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Listening")
                .setContentText("Tap to stop the session")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .addAction(
                    android.R.drawable.ic_media_pause,
                    "Stop",
                    stopPi,
                )
            if (contentIntent != null) builder.setContentIntent(contentIntent)
            return builder.build()
        }
    }
}
