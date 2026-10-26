package com.rktmkalita.echo.playback

import android.media.audiofx.Equalizer

object EqualizerController {
    private var equalizer: Equalizer? = null

    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0) return
        runCatching {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
        }
    }

    fun preset(name: String) {
        val levels = when (name) {
            "Bass Boost" -> shortArrayOf(900, 600, 250, 0, 0)
            "Rock" -> shortArrayOf(700, 350, -100, 450, 700)
            "Pop" -> shortArrayOf(-100, 250, 650, 250, -100)
            "Jazz" -> shortArrayOf(400, 200, -150, 250, 500)
            "Classical" -> shortArrayOf(500, 250, -100, 300, 500)
            "Vocal" -> shortArrayOf(-250, 100, 500, 350, -100)
            else -> shortArrayOf(0, 0, 0, 0, 0)
        }
        applyLevels(levels)
    }

    fun applyLevels(levels: ShortArray) {
        equalizer?.let { effect ->
            val count = minOf(effect.numberOfBands.toInt(), levels.size)
            for (index in 0 until count) effect.setBandLevel(index.toShort(), levels[index])
        }
    }

    fun release() {
        equalizer?.release()
        equalizer = null
    }
}
