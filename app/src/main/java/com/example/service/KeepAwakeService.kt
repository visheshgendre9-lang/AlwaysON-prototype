package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.PreferencesManager
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
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private lateinit var prefs: PreferencesManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopKeepAwake()
            }
            ACTION_ADD_5_MINS -> {
                val current = _sessionState.value
                if (current.isActive && !current.isInfinite) {
                    val newSecs = current.remainingSeconds + 300
                    val totalSecs = current.totalSeconds + 300
                    _sessionState.value = current.copy(
                        remainingSeconds = newSecs,
                        totalSeconds = totalSecs
                    )
                    updateNotification(_sessionState.value)
                    applySystemTimeout(newSecs / 60, false)
                }
            }
            ACTION_UPDATE_MESSAGE -> {
                val newMessage = intent?.getStringExtra(EXTRA_CUSTOM_MESSAGE)
                if (!newMessage.isNullOrBlank()) {
                    val current = _sessionState.value
                    val updatedMsg = newMessage
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
        applySystemTimeout(minutes, isInfinite)
        addKeepAwakeOverlay()

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

        startForegroundWithNotification(session)

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

    private fun startForegroundWithNotification(session: KeepAwakeSession) {
        val notification = buildNotification(session)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopKeepAwake() {
        timerJob?.cancel()
        timerJob = null
        releaseWakeLock()
        restoreSystemTimeout()
        removeKeepAwakeOverlay()

        _sessionState.value = _sessionState.value.copy(isActive = false, remainingSeconds = 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun addKeepAwakeOverlay() {
        if (overlayView != null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                val params = WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
                val view = View(this)
                windowManager?.addView(view, params)
                overlayView = view
            }
        } catch (_: Exception) {}
    }

    private fun removeKeepAwakeOverlay() {
        try {
            overlayView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (_: Exception) {}
        overlayView = null
        windowManager = null
    }

    private fun applySystemTimeout(minutes: Int, isInfinite: Boolean) {
        try {
            if (Settings.System.canWrite(applicationContext)) {
                val currentSystemTimeout = Settings.System.getInt(
                    contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    30000
                )
                // Store original timeout if not set yet to extreme high
                if (currentSystemTimeout < 12 * 60 * 60 * 1000) {
                    prefs.originalTimeoutMs = currentSystemTimeout
                }

                val targetTimeoutMs = if (isInfinite) {
                    24 * 60 * 60 * 1000 // 24 hours
                } else {
                    (minutes * 60 * 1000).coerceAtLeast(60000)
                }

                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    targetTimeoutMs
                )
            }
        } catch (_: Exception) {}
    }

    private fun restoreSystemTimeout() {
        try {
            if (Settings.System.canWrite(applicationContext)) {
                val orig = prefs.originalTimeoutMs
                if (orig > 0) {
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_OFF_TIMEOUT,
                        orig
                    )
                }
            }
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeFlags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE

        wakeLock = powerManager.newWakeLock(
            wakeFlags,
            "ScreenAwake::KeepScreenAliveWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(24 * 60 * 60 * 1000L) // Max 24 hours safety
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
            manager?.createNotificationChannel(channel)
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

        val add5Intent = Intent(this, KeepAwakeService::class.java).apply {
            action = ACTION_ADD_5_MINS
        }
        val pendingAdd5Intent = PendingIntent.getService(
            this, 2, add5Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subText = if (session.isInfinite) "Status: Always On" else "Time Left: ${session.formattedRemainingTime}"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Keeping On ($subText)")
            .setContentText(session.customMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(session.customMessage))
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingOpenIntent)

        if (!session.isInfinite) {
            builder.addAction(
                android.R.drawable.ic_input_add,
                "+5 Min",
                pendingAdd5Intent
            )
        }

        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Turn Off",
            pendingStopIntent
        )

        return builder.build()
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
        const val ACTION_ADD_5_MINS = "com.example.action.ADD_5_MINS"
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
