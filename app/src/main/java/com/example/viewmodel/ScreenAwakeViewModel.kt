package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.PreferencesManager
import com.example.model.KeepAwakeSession
import com.example.service.KeepAwakeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenAwakeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    val sessionState: StateFlow<KeepAwakeSession> = KeepAwakeService.sessionState

    private val _selectedMinutes = MutableStateFlow(prefs.lastMinutes)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    private val _isInfinite = MutableStateFlow(prefs.isLastInfinite)
    val isInfinite: StateFlow<Boolean> = _isInfinite.asStateFlow()

    private val _customMessage = MutableStateFlow(prefs.customMessage)
    val customMessage: StateFlow<String> = _customMessage.asStateFlow()

    fun selectPreset(minutes: Int, infinite: Boolean) {
        _selectedMinutes.value = minutes
        _isInfinite.value = infinite
        prefs.lastMinutes = minutes
        prefs.isLastInfinite = infinite
    }

    fun adjustMinutes(deltaMinutes: Int) {
        val newMins = (_selectedMinutes.value + deltaMinutes).coerceIn(1, 480)
        _selectedMinutes.value = newMins
        _isInfinite.value = false
        prefs.lastMinutes = newMins
        prefs.isLastInfinite = false

        // If service is currently active, update running timer
        if (sessionState.value.isActive && !sessionState.value.isInfinite) {
            KeepAwakeService.startKeepAwake(
                context = getApplication(),
                minutes = newMins,
                isInfinite = false,
                customMessage = _customMessage.value
            )
        }
    }

    fun updateCustomMessage(message: String) {
        val trimmed = message.ifBlank { KeepAwakeSession.DEFAULT_MESSAGE }
        _customMessage.value = trimmed
        prefs.customMessage = trimmed

        if (sessionState.value.isActive) {
            KeepAwakeService.updateCustomMessage(getApplication(), trimmed)
        }
    }

    fun toggleKeepAwake() {
        val context = getApplication<Application>()
        if (sessionState.value.isActive) {
            KeepAwakeService.stopKeepAwake(context)
        } else {
            KeepAwakeService.startKeepAwake(
                context = context,
                minutes = _selectedMinutes.value,
                isInfinite = _isInfinite.value,
                customMessage = _customMessage.value
            )
            prefs.addActiveMinutes(if (_isInfinite.value) 30 else _selectedMinutes.value)
        }
    }

    val totalActiveMinutes: Int
        get() = prefs.totalActiveMinutes
}
