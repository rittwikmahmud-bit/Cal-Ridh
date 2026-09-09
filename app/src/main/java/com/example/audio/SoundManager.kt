package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Offline, zero-dependency audio synthesizer creating sweet, kid-friendly xylophone
 * and chime sound effects for Riddhya's Calculator.
 */
class SoundManager {
  private val sampleRate = 22050
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  var isSoundEnabled: Boolean = true

  // Pre-synthesized audio buffers for low latency
  private val digitSounds = Array(10) { digit ->
    val freqs = doubleArrayOf(
      261.63, // 0 -> C4
      293.66, // 1 -> D4
      329.63, // 2 -> E4
      349.23, // 3 -> F4
      392.00, // 4 -> G4
      440.00, // 5 -> A4
      493.88, // 6 -> B4
      523.25, // 7 -> C5
      587.33, // 8 -> D5
      659.25  // 9 -> E5
    )
    generateChimeTone(freqs[digit], durationMs = 130)
  }

  private val dotSound = generateChimeTone(783.99, durationMs = 120) // G5
  private val operatorSound = generateDuoTone(440.0, 659.25, durationMs = 110)
  private val deleteSound = generateBubblePop()
  private val clearSound = generateSweepDown()
  private val fanfareSound = generateFanfare()

  fun playDigit(digit: Int) {
    if (!isSoundEnabled || digit !in 0..9) return
    playSound(digitSounds[digit])
  }

  fun playDot() {
    if (!isSoundEnabled) return
    playSound(dotSound)
  }

  fun playOperator() {
    if (!isSoundEnabled) return
    playSound(operatorSound)
  }

  fun playDelete() {
    if (!isSoundEnabled) return
    playSound(deleteSound)
  }

  fun playClear() {
    if (!isSoundEnabled) return
    playSound(clearSound)
  }

  fun playFanfare() {
    if (!isSoundEnabled) return
    playSound(fanfareSound)
  }

  private fun playSound(audioData: ShortArray) {
    scope.launch {
      try {
        val bufferSize = audioData.size * 2
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
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(bufferSize)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        audioTrack.write(audioData, 0, audioData.size)
        audioTrack.play()
        // Release after playback finishes
        kotlinx.coroutines.delay((audioData.size * 1000L / sampleRate) + 50L)
        audioTrack.stop()
        audioTrack.release()
      } catch (_: Exception) {
        // Graceful fallback if audio hardware is temporarily unavailable
      }
    }
  }

  private fun generateChimeTone(frequency: Double, durationMs: Int): ShortArray {
    val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(totalSamples)
    val decayRate = 6.0

    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      // Fundamental + gentle warm second harmonic
      val envelope = exp(-decayRate * (t / (durationMs / 1000.0)))
      val sample = (sin(2.0 * PI * frequency * t) * 0.75 +
          sin(2.0 * PI * frequency * 2.0 * t) * 0.25) * envelope
      buffer[i] = (sample * 16000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return buffer
  }

  private fun generateDuoTone(freq1: Double, freq2: Double, durationMs: Int): ShortArray {
    val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(totalSamples)
    val decayRate = 7.0

    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      val envelope = exp(-decayRate * (t / (durationMs / 1000.0)))
      val sample = (sin(2.0 * PI * freq1 * t) * 0.5 + sin(2.0 * PI * freq2 * t) * 0.5) * envelope
      buffer[i] = (sample * 16000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return buffer
  }

  private fun generateBubblePop(): ShortArray {
    val durationMs = 90
    val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(totalSamples)

    for (i in 0 until totalSamples) {
      val t = i.toDouble() / sampleRate
      // Frequency slides rapidly up like a bubble popping: 300Hz to 600Hz
      val prog = i.toDouble() / totalSamples
      val freq = 320.0 + 380.0 * prog
      val envelope = (1.0 - prog) * (1.0 - prog)
      val sample = sin(2.0 * PI * freq * t) * envelope
      buffer[i] = (sample * 18000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return buffer
  }

  private fun generateSweepDown(): ShortArray {
    val durationMs = 140
    val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val buffer = ShortArray(totalSamples)

    for (i in 0 until totalSamples) {
      val prog = i.toDouble() / totalSamples
      val t = i.toDouble() / sampleRate
      val freq = 480.0 * (1.0 - 0.6 * prog)
      val envelope = exp(-4.0 * prog)
      val sample = sin(2.0 * PI * freq * t) * envelope
      buffer[i] = (sample * 16000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return buffer
  }

  private fun generateFanfare(): ShortArray {
    // 3 bouncy arpeggio notes in sequence: C5 (523Hz), E5 (659Hz), G5 (783Hz), C6 (1046Hz)
    val noteDurationMs = 70
    val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
    val totalSamples = (sampleRate * ((notes.size * noteDurationMs + 100) / 1000.0)).toInt()
    val buffer = ShortArray(totalSamples)

    notes.forEachIndexed { noteIndex, freq ->
      val startIndex = (sampleRate * (noteIndex * noteDurationMs / 1000.0)).toInt()
      val samplesInNote = (sampleRate * (140 / 1000.0)).toInt() // slight overlap ring

      for (i in 0 until samplesInNote) {
        val dest = startIndex + i
        if (dest < totalSamples) {
          val t = i.toDouble() / sampleRate
          val envelope = exp(-5.5 * (i.toDouble() / samplesInNote))
          val sample = (sin(2.0 * PI * freq * t) * 0.8 + sin(2.0 * PI * freq * 2.0 * t) * 0.2) * envelope
          val existing = buffer[dest].toInt()
          val newVal = existing + (sample * 12000).toInt()
          buffer[dest] = newVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
      }
    }
    return buffer
  }
}
