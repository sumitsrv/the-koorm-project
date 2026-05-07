package org.koorm.ocpd.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.TargetDataLine
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Desktop [AudioRecorder] backed by [javax.sound.sampled.TargetDataLine].
 *
 * Opens the system default capture device at 16 kHz / mono / 16-bit signed
 * little-endian PCM. If the system default does not support that format
 * directly the JVM's [AudioSystem] will perform automatic format conversion
 * via [AudioSystem.getTargetDataLine]; if that also fails [start] terminates
 * the flow with [LineUnavailableException].
 */
public actual class AudioRecorder {

    @Volatile
    private var line: TargetDataLine? = null

    public actual val isRecording: Boolean
        get() = line?.isRunning ?: false

    public actual fun start(): Flow<ShortArray> = callbackFlow {
        val format = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            AudioConfig.SAMPLE_RATE_HZ.toFloat(),
            16,
            AudioConfig.CHANNELS,
            2 * AudioConfig.CHANNELS,
            AudioConfig.SAMPLE_RATE_HZ.toFloat(),
            /* bigEndian = */ false,
        )
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!AudioSystem.isLineSupported(info)) {
            close(LineUnavailableException("No matching capture line for $format"))
            return@callbackFlow
        }
        val target = try {
            AudioSystem.getLine(info) as TargetDataLine
        } catch (t: Throwable) {
            close(t)
            return@callbackFlow
        }
        try {
            target.open(format, AudioConfig.FRAME_SAMPLES * 2 * 8)
            target.start()
        } catch (t: Throwable) {
            target.close()
            close(t)
            return@callbackFlow
        }
        line = target

        val frameBytes = AudioConfig.FRAME_SAMPLES * 2
        val buffer = ByteArray(frameBytes)
        val shortBuf = ShortArray(AudioConfig.FRAME_SAMPLES)
        try {
            while (!isClosedForSend) {
                val read = target.read(buffer, 0, buffer.size)
                if (read <= 0) break
                val samples = read / 2
                ByteBuffer.wrap(buffer, 0, read)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer()
                    .get(shortBuf, 0, samples)
                trySend(shortBuf.copyOf(samples)).isSuccess
            }
        } finally {
            try { target.stop() } catch (_: Throwable) {}
            try { target.close() } catch (_: Throwable) {}
            line = null
        }

        awaitClose { /* stop() handles teardown imperatively if invoked */ }
    }.flowOn(Dispatchers.IO)

    public actual fun stop() {
        val l = line ?: return
        line = null
        try { l.stop() } catch (_: Throwable) {}
        try { l.close() } catch (_: Throwable) {}
    }
}
