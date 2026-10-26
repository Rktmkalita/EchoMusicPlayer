package com.rktmkalita.echo.playback

import android.media.audiofx.Equalizer

object EqualizerController {
    private var equalizer: Equalizer? = null
    private var enabled = false

    fun attach(context: android.content.Context, audioSessionId: Int) {
        if (audioSessionId == 0) return
        val prefs = context.getSharedPreferences("AudioSettings", android.content.Context.MODE_PRIVATE)
        enabled = prefs.getBoolean("enabled", false)
        runCatching {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply { enabled = this@EqualizerController.enabled }
            prefs.getString("preset", "Normal")?.let { preset(it) }
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

    fun setEnabled(value: Boolean) {
        enabled = value
        runCatching { equalizer?.enabled = value }
    }

    fun release() {
        equalizer?.release()
        equalizer = null
    }
}
