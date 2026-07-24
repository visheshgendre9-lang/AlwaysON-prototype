package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.KeepAwakeSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KeepAwakeService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopKeepAwake()
            }
            ACTION_UPDATE_MESSAGE -> {
                val newMessage = intent?.getStringExtra(EXTRA_CUSTOM_MESSAGE)
                if (!newMessage.isNullOrBlank()) {
                    val current = _sessionState.value
                    val updatedMsg = newMessage ?: KeepAwakeSession.DEFAULT_MESSAGE
                    _sessionState.value = current.copy(customMessage = updatedMsg)
                    if (current.isActive) {
                        updateNotification(current.copy(customMessage = updatedMsg))
                    }
                }
            }
            ACTION_START -> {
                val minutes = intent?.getIntExtra(EXTRA_MINUTES, 20) ?: 20
                val isInfinite = intent?.getBooleanExtra(EXTRA_INFINITE, false) ?: false
                val customMessage = intent?.getStringExtra(EXTRA_CUSTOM_MESSAGE)
                    ?: KeepAwakeSession.DEFAULT_MESSAGE

                startKeepAwake(minutes, isInfinite, customMessage)
            }
        }

        return START_STICKY
    }

    private fun startKeepAwake(minutes: Int, isInfinite: Boolean, customMessage: String) {
        acquireWakeLock()

        val totalSecs = if (isInfinite) 0 else minutes * 60
        val session = KeepAwakeSession(
            isActive = true,
            isInfinite = isInfinite,
            remainingSeconds = totalSecs,
            totalSeconds = totalSecs,
            customMessage = customMessage,
            presetName = if (isInfinite) "Infinite" else "$minutes Min"
        )
        _sessionState.value = session

        startForeground(NOTIFICATION_ID, buildNotification(session))

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            if (isInfinite) {
                var elapsedSecs = 0
                while (_sessionState.value.isActive) {
                    delay(1000L)
                    elapsedSecs++
                    val updated = _sessionState.value.copy(remainingSeconds = elapsedSecs)
                    _sessionState.value = updated
                    if (elapsedSecs % 10 == 0) {
                        updateNotification(updated)
                    }
                }
            } else {
                var remaining = totalSecs
                while (remaining > 0 && _sessionState.value.isActive) {
                    delay(1000L)
                    remaining--
                    val updated = _sessionState.value.copy(remainingSeconds = remaining)
                    _sessionState.value = updated
                    if (remaining % 5 == 0 || remaining <= 10) {
                        updateNotification(updated)
                    }
                }
                if (_sessionState.value.isActive) {
                    stopKeepAwake()
                }
            }
        }
    }

    private fun stopKeepAwake() {
        timerJob?.cancel()
        timerJob = null
        releaseWakeLock()

        _sessionState.value = _sessionState.value.copy(isActive = false, remainingSeconds = 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        // Using SCREEN_BRIGHT_WAKE_LOCK or FULL_WAKE_LOCK with ON_AFTER_RELEASE
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "ScreenAwake::KeepScreenAliveWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(12 * 60 * 60 * 1000L) // Safety max 12 hrs
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Keep Alive Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing notification while keep-awake service keeps display turned on."
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(session: KeepAwakeSession): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, KeepAwakeService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subText = if (session.isInfinite) "Status: Always On" else "Time Left: ${session.formattedRemainingTime}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Keeping On ($subText)")
            .setContentText(session.customMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(session.customMessage))
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingOpenIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Turn Off",
                pendingStopIntent
            )
            .build()
    }

    private fun updateNotification(session: KeepAwakeSession) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(session))
    }

    override fun onDestroy() {
        stopKeepAwake()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "screen_awake_foreground_channel"
        const val NOTIFICATION_ID = 8801

        const val ACTION_START = "com.example.action.START_KEEP_AWAKE"
        const val ACTION_STOP = "com.example.action.STOP_KEEP_AWAKE"
        const val ACTION_UPDATE_MESSAGE = "com.example.action.UPDATE_MESSAGE"

        const val EXTRA_MINUTES = "extra_minutes"
        const val EXTRA_INFINITE = "extra_infinite"
        const val EXTRA_CUSTOM_MESSAGE = "extra_custom_message"

        private val _sessionState = MutableStateFlow(KeepAwakeSession())
        val sessionState: StateFlow<KeepAwakeSession> = _sessionState.asStateFlow()

        fun startKeepAwake(context: Context, minutes: Int, isInfinite: Boolean, customMessage: String) {
            val intent = Intent(context, KeepAwakeService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MINUTES, minutes)
                putExtra(EXTRA_INFINITE, isInfinite)
                putExtra(EXTRA_CUSTOM_MESSAGE, customMessage)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopKeepAwake(context: Context) {
            val intent = Intent(context, KeepAwakeService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateCustomMessage(context: Context, message: String) {
            val intent = Intent(context, KeepAwakeService::class.java).apply {
                action = ACTION_UPDATE_MESSAGE
                putExtra(EXTRA_CUSTOM_MESSAGE, message)
            }
            context.startService(intent)
        }
    }
}
