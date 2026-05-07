package org.koorm.ocpd.audio

import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform PCM microphone source.
 *
 * Implementations stream **16 kHz mono int16** little-endian PCM chunks of
 * [AudioConfig.FRAME_SAMPLES] samples each. The contract is intentionally tiny
 * so it can be backed by:
 *
 * - Android: `AudioRecord` running inside [MicrophoneForegroundService]
 *   so a session continues while the app is backgrounded.
 * - iOS: `AVAudioEngine` input tap with `AVAudioSession.Category.record`. No
 *   `UIBackgroundModes=audio`; the session ends on background per the locked
 *   product decision.
 * - Desktop: `javax.sound.sampled.TargetDataLine` on the default mic.
 *
 * Calling [start] twice without an intervening [stop] is undefined; callers
 * should wrap the returned flow in `flowOn(Dispatchers.Default)` if they want
 * to move decoding off the audio callback thread.
 */
public expect class AudioRecorder {

    /**
     * Begin capturing. The returned cold [Flow] emits one [ShortArray] per
     * 100 ms frame. Cancelling collection releases the underlying mic resource;
     * implementations also expose [stop] for explicit teardown.
     */
    public fun start(): Flow<ShortArray>

    /** Releases the capture pipeline if [start] is currently active. */
    public fun stop()

    /** True between a successful [start] collection and a [stop] / cancellation. */
    public val isRecording: Boolean
}
