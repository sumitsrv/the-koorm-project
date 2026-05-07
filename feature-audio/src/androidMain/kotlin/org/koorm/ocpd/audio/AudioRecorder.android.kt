package org.koorm.ocpd.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.atomic.AtomicReference

/**
 * Android [AudioRecorder] backed by [AudioRecord].
 *
 * The class itself only constructs and manages the [AudioRecord]; the
 * foreground-service lifecycle that keeps capture alive while backgrounded is
 * owned by [MicrophoneForegroundService], which simply hosts an instance of
 * this recorder for as long as the service runs.
 *
 * Caller MUST hold [Manifest.permission.RECORD_AUDIO] before invoking [start].
 * [PermissionController] is the supported way to obtain it.
 */
public actual class AudioRecorder(
    @Suppress("UNUSED_PARAMETER") private val context: Context,
) {
    private val current: AtomicReference<AudioRecord?> = AtomicReference(null)

    public actual val isRecording: Boolean
        get() = current.get()?.recordingState == AudioRecord.RECORDSTATE_RECORDING

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public actual fun start(): Flow<ShortArray> = callbackFlow {
        val minBuffer = AudioRecord.getMinBufferSize(
            AudioConfig.SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        // AudioRecord requires the buffer to be at least minBuffer; we round up
        // to the next multiple of FRAME_SAMPLES * 2 (bytes per short) so reads
        // align with our 100 ms frame cadence without copy gymnastics.
        val frameBytes = AudioConfig.FRAME_SAMPLES * 2
        val bufferBytes = ((minBuffer + frameBytes - 1) / frameBytes) * frameBytes

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            AudioConfig.SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferBytes,
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            close(IllegalStateException("AudioRecord failed to initialize"))
            return@callbackFlow
        }
        if (!current.compareAndSet(null, recorder)) {
            recorder.release()
            close(IllegalStateException("AudioRecorder already running"))
            return@callbackFlow
        }

        recorder.startRecording()
        val frame = ShortArray(AudioConfig.FRAME_SAMPLES)
        try {
            while (!isClosedForSend) {
                val read = recorder.read(frame, 0, frame.size)
                if (read <= 0) {
                    // ERROR_INVALID_OPERATION (-3) etc.; exit cleanly so the
                    // caller's collector terminates rather than spinning.
                    break
                }
                // Copy because we reuse `frame` on the next iteration.
                trySend(frame.copyOf(read)).isSuccess
            }
        } finally {
            // No-op if already stopped via stop(); guard with try/catch since
            // calling stop() on a STOPPED recorder throws on some OEMs.
            try { recorder.stop() } catch (_: IllegalStateException) {}
            recorder.release()
            current.compareAndSet(recorder, null)
        }

        awaitClose { /* stop() handles teardown imperatively if invoked */ }
    }.flowOn(Dispatchers.IO)

    public actual fun stop() {
        val r = current.getAndSet(null) ?: return
        try { r.stop() } catch (_: IllegalStateException) {}
        r.release()
    }
}
