package com.example.model

data class KeepAwakeSession(
    val isActive: Boolean = false,
    val isInfinite: Boolean = false,
    val remainingSeconds: Int = 1200, // Default 20 minutes (1200 sec)
    val totalSeconds: Int = 1200,
    val customMessage: String = DEFAULT_MESSAGE,
    val presetName: String = "20 Min"
) {
    val progressRatio: Float
        get() = if (isInfinite || totalSeconds <= 0) 1f else remainingSeconds.toFloat() / totalSeconds.toFloat()

    val formattedRemainingTime: String
        get() {
            if (isInfinite) return "∞ Infinite"
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format("%02d:%02d", mins, secs)
        }

    companion object {
        const val DEFAULT_MESSAGE = "App is running in background to help you don't turn off the screen by itself."
    }
}

enum class DurationPreset(val label: String, val minutes: Int, val isInfinite: Boolean = false) {
    MIN_5("5 Min", 5),
    MIN_15("15 Min", 15),
    MIN_20("20 Min", 20),
    MIN_30("30 Min", 30),
    MIN_60("60 Min", 60),
    INFINITE("Infinite", 0, isInfinite = true)
}
