package org.koorm.ocpd.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import kotlin.math.max
import kotlin.math.min

/**
 * iOS [AudioRecorder] backed by [AVAudioEngine].
 *
 * Per the locked product decisions: **session-based**, no
 * `UIBackgroundModes=audio`. The engine is torn down explicitly when the flow
 * is cancelled or [stop] is called; the OS automatically interrupts capture
 * on app backgrounding, which surfaces as a clean termination — Phase 7's
 * "Tap to resume" UI handles the user-visible recovery.
 *
 * Design notes:
 * - We install the tap with the input node's **native** output format (which
 *   Core Audio guarantees we can use), then convert to 16 kHz mono int16 in
 *   software. Installing taps with mismatched formats is unreliable across
 *   iOS versions and device classes, so we don't.
 * - The native input is float32 at the device sample rate (44.1 or 48 kHz on
 *   iPhone/iPad). We do simple linear-stride decimation; whisper.cpp performs
 *   its own high-quality resampling internally (Phase 4), so this is fine for
 *   feeding STT.
 * - For Phase 3 the goal is just "PCM frames flowing"; precise per-frame size
 *   accuracy is not promised — emitted frames will average ≈100 ms but vary
 *   slightly with the device's HW buffer cadence.
 */
@OptIn(ExperimentalForeignApi::class)
public actual class AudioRecorder {

    private var engine: AVAudioEngine? = null

    public actual val isRecording: Boolean
        get() = engine?.running ?: false

    public actual fun start(): Flow<ShortArray> = callbackFlow {
        val session = AVAudioSession.sharedInstance()
        runCatching {
            session.setCategory(AVAudioSessionCategoryRecord, error = null)
            session.setMode(AVAudioSessionModeMeasurement, error = null)
            session.setActive(true, error = null)
        }.onFailure {
            close(it)
            return@callbackFlow
        }

        val newEngine = AVAudioEngine()
        val input = newEngine.inputNode
        val nativeFormat = input.outputFormatForBus(0u)
        val nativeSampleRate = nativeFormat.sampleRate
        val targetSampleRate = AudioConfig.SAMPLE_RATE_HZ.toDouble()
        // Decimation step: e.g. 48 000 / 16 000 == 3. We pick the nearest
        // integer step >= 1 to keep the inner loop branch-free.
        val step = max(1, (nativeSampleRate / targetSampleRate).toInt())

        // ~100 ms worth of native samples per tap callback.
        val tapBufferFrames = (nativeSampleRate * 0.1).toInt().coerceAtLeast(256)

        input.installTapOnBus(
            bus = 0u,
            bufferSize = tapBufferFrames.toUInt(),
            format = nativeFormat,
        ) { buffer: AVAudioPCMBuffer?, _ ->
            if (buffer == null) return@installTapOnBus
            val frames = buffer.frameLength.toInt()
            if (frames <= 0) return@installTapOnBus
            val channels = nativeFormat.channelCount.toInt().coerceAtLeast(1)
            val srcPtr = buffer.floatChannelData?.get(0) ?: return@installTapOnBus

            val outLen = (frames + step - 1) / step
            val out = ShortArray(outLen)
            var oi = 0
            var i = 0
            while (i < frames && oi < outLen) {
                // Read first channel; if the device delivers stereo we discard
                // the second channel rather than averaging — measurement mode
                // already requests mono where supported.
                val sample = srcPtr[i.toLong() * channels]
                // Float32 [-1, 1] -> int16 with clamping.
                val scaled = (sample * 32767.0f)
                val clamped = max(-32768.0f, min(32767.0f, scaled))
                out[oi++] = clamped.toInt().toShort()
                i += step
            }
            trySend(out)
        }

        runCatching {
            newEngine.prepare()
            newEngine.startAndReturnError(null)
        }.onFailure {
            input.removeTapOnBus(0u)
            close(it)
            return@callbackFlow
        }

        engine = newEngine

        awaitClose { stop() }
    }

    public actual fun stop() {
        val e = engine ?: return
        runCatching { e.inputNode.removeTapOnBus(0u) }
        runCatching { e.stop() }
        runCatching { AVAudioSession.sharedInstance().setActive(false, error = null) }
        engine = null
    }
}
