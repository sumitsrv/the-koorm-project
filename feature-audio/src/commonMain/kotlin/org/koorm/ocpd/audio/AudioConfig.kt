package org.koorm.ocpd.audio

/**
 * PCM audio capture parameters used uniformly across platforms.
 *
 * 16 kHz mono int16 is the format whisper.cpp expects (Phase 4) and is well
 * supported by every target platform's microphone API at the system level.
 */
public object AudioConfig {
    public const val SAMPLE_RATE_HZ: Int = 16_000
    public const val CHANNELS: Int = 1

    /**
     * Frame size used for delivering [ShortArray] chunks via [AudioRecorder.start].
     *
     * 1600 samples == 100 ms at 16 kHz, which is a comfortable cadence for
     * downstream VAD and STT — small enough to feel "live", large enough that
     * per-frame overhead is negligible.
     */
    public const val FRAME_SAMPLES: Int = 1_600
}
