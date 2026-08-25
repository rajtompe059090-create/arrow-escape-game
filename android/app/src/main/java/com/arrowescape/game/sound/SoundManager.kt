package com.arrowescape.game.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Native Android Sound & Haptics Engine for Arrow Escape.
 * Synthesizes pure procedural audio tones (Sine waves with envelopes)
 * and ambient background music without requiring external asset files.
 */
object SoundManager {
    private const val TAG = "SoundManager"
    private const val SAMPLE_RATE = 44100

    private var context: Context? = null
    private var vibrator: Vibrator? = null

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = false
    var isHapticsEnabled: Boolean = true

    private var bgmJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrate(durationMs: Long = 25) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed: ${e.message}")
        }
    }

    fun setMusic(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            startBgm()
        } else {
            stopBgm()
        }
    }

    private fun startBgm() {
        if (bgmJob != null && bgmJob?.isActive == true) return
        val notes = doubleArrayOf(261.63, 329.63, 392.00, 523.25, 440.00, 329.63, 392.00, 261.63) // C - E - G - C5 - A - E - G - C

        bgmJob = scope.launch {
            var step = 0
            while (isActive && isMusicEnabled) {
                val freq = notes[step % notes.size]
                step++
                playTone(
                    startFreq = freq,
                    endFreq = freq,
                    durationMs = 1800,
                    volume = 0.04f,
                    waveType = WaveType.SINE
                )
                delay(2200)
            }
        }
    }

    private fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
    }

    // ==========================================
    // PROCEDURAL SOUND EFFECTS
    // ==========================================

    fun playTap() {
        vibrate(12)
        if (!isSoundEnabled) return
        scope.launch {
            playTone(startFreq = 440.0, endFreq = 880.0, durationMs = 60, volume = 0.20f)
        }
    }

    fun playEscape() {
        vibrate(25)
        if (!isSoundEnabled) return
        scope.launch {
            playTone(startFreq = 320.0, endFreq = 740.0, durationMs = 220, volume = 0.28f)
        }
    }

    fun playBlocked() {
        vibrate(60)
        if (!isSoundEnabled) return
        scope.launch {
            playTone(startFreq = 160.0, endFreq = 90.0, durationMs = 160, volume = 0.35f, waveType = WaveType.SQUARE)
        }
    }

    fun playHint() {
        vibrate(30)
        if (!isSoundEnabled) return
        scope.launch {
            playTone(startFreq = 523.25, endFreq = 659.25, durationMs = 120, volume = 0.25f)
            delay(80)
            playTone(startFreq = 659.25, endFreq = 783.99, durationMs = 160, volume = 0.25f)
        }
    }

    fun playLevelComplete() {
        vibrate(50)
        if (!isSoundEnabled) return
        scope.launch {
            val fanfare = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5 - E5 - G5 - C6
            for (freq in fanfare) {
                playTone(startFreq = freq, endFreq = freq, durationMs = 140, volume = 0.30f)
                delay(120)
            }
        }
    }

    fun playGameOver() {
        vibrate(80)
        if (!isSoundEnabled) return
        scope.launch {
            val notes = doubleArrayOf(392.00, 349.23, 329.63, 261.63) // G4 - F4 - E4 - C4
            for (freq in notes) {
                playTone(startFreq = freq, endFreq = freq * 0.95, durationMs = 200, volume = 0.30f)
                delay(180)
            }
        }
    }

    private enum class WaveType { SINE, SQUARE }

    private fun playTone(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Float,
        waveType: WaveType = WaveType.SINE
    ) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
            if (numSamples <= 0) return

            val generatedSnd = ShortArray(numSamples)
            var currentPhase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress

                currentPhase += 2.0 * Math.PI * currentFreq / SAMPLE_RATE
                if (currentPhase > 2.0 * Math.PI) {
                    currentPhase -= 2.0 * Math.PI
                }

                // Envelope: Attack & Decay
                val envelope = when {
                    progress < 0.15 -> (progress / 0.15)
                    progress > 0.70 -> ((1.0 - progress) / 0.30)
                    else -> 1.0
                }

                val rawSample = if (waveType == WaveType.SINE) {
                    sin(currentPhase)
                } else {
                    if (sin(currentPhase) >= 0) 0.8 else -0.8
                }

                val sample = (rawSample * envelope * volume * Short.MAX_VALUE).toInt()
                generatedSnd[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(bufferSize, generatedSnd.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()

            // Release AudioTrack after playback finishes
            scope.launch {
                delay(durationMs.toLong() + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(TAG, "playTone error: ${e.message}")
        }
    }
}
