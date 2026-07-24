package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.KeepAwakeSession

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("screen_awake_prefs", Context.MODE_PRIVATE)

    var customMessage: String
        get() = prefs.getString(KEY_CUSTOM_MESSAGE, KeepAwakeSession.DEFAULT_MESSAGE) ?: KeepAwakeSession.DEFAULT_MESSAGE
        set(value) = prefs.edit().putString(KEY_CUSTOM_MESSAGE, value).apply()

    var lastMinutes: Int
        get() = prefs.getInt(KEY_LAST_MINUTES, 20)
        set(value) = prefs.edit().putInt(KEY_LAST_MINUTES, value).apply()

    var isLastInfinite: Boolean
        get() = prefs.getBoolean(KEY_LAST_INFINITE, false)
        set(value) = prefs.edit().putBoolean(KEY_LAST_INFINITE, value).apply()

    var originalTimeoutMs: Int
        get() = prefs.getInt(KEY_ORIGINAL_TIMEOUT, 30000)
        set(value) = prefs.edit().putInt(KEY_ORIGINAL_TIMEOUT, value).apply()

    var totalActiveMinutes: Int
        get() = prefs.getInt(KEY_TOTAL_ACTIVE_MINUTES, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_ACTIVE_MINUTES, value).apply()

    fun addActiveMinutes(mins: Int) {
        totalActiveMinutes += mins
    }

    companion object {
        private const val KEY_CUSTOM_MESSAGE = "key_custom_message"
        private const val KEY_LAST_MINUTES = "key_last_minutes"
        private const val KEY_LAST_INFINITE = "key_last_infinite"
        private const val KEY_ORIGINAL_TIMEOUT = "key_original_timeout"
        private const val KEY_TOTAL_ACTIVE_MINUTES = "key_total_active_minutes"
    }
}
