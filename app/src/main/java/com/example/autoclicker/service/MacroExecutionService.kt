package com.example.autoclicker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.autoclicker.R
import com.example.autoclicker.ui.MainActivity
import timber.log.Timber

/**
 * 매크로 실행을 위한 포그라운드 서비스입니다.
 * 이 서비스는 백그라운드에서 계속 실행되며 사용자에게 알림을 표시합니다.
 */
class MacroExecutionService : Service() {

    companion object {
        private const val TAG = "MacroExecutionService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "macro_execution_channel"

        const val ACTION_START_MACRO = "com.example.autoclicker.START_MACRO"
        const val ACTION_STOP_MACRO = "com.example.autoclicker.STOP_MACRO"
        const val EXTRA_MACRO_ID = "macro_id"
        const val EXTRA_MACRO_NAME = "macro_name"
    }

    private var isMacroRunning = false
    private var currentMacroId: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Timber.tag(TAG).d("MacroExecutionService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_START_MACRO -> {
                val macroId = intent.getStringExtra(EXTRA_MACRO_ID) ?: "Unknown"
                val macroName = intent.getStringExtra(EXTRA_MACRO_NAME) ?: "매크로"
                startMacroExecution(macroId, macroName)
                START_STICKY
            }
            ACTION_STOP_MACRO -> {
                stopMacroExecution()
                START_STICKY
            }
            else -> START_STICKY
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMacroExecution()
        Timber.tag(TAG).d("MacroExecutionService destroyed")
    }

    /**
     * 매크로 실행을 시작합니다.
     */
    private fun startMacroExecution(macroId: String, macroName: String) {
        if (isMacroRunning) {
            Timber.tag(TAG).w("Macro is already running")
            return
        }

        try {
            currentMacroId = macroId
            isMacroRunning = true

            val notification = createNotification("매크로 실행 중...", macroName)
            startForeground(NOTIFICATION_ID, notification)

            Timber.tag(TAG).d("Macro execution started: $macroId ($macroName)")

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error starting macro execution")
            stopSelf()
        }
    }

    /**
     * 매크로 실행을 중지합니다.
     */
    private fun stopMacroExecution() {
        if (!isMacroRunning) {
            return
        }

        try {
            isMacroRunning = false
            currentMacroId = null

            Timber.tag(TAG).d("Macro execution stopped")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error stopping macro execution")
        }
    }

    /**
     * 알림 채널을 생성합니다 (Android 8.0 이상).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "매크로 실행",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "자동 클릭 매크로 실행 알림"
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 알림을 생성합니다.
     */
    private fun createNotification(title: String, macroName: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MacroExecutionService::class.java).apply {
            action = ACTION_STOP_MACRO
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("매크로: $macroName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(0, "중지", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * 매크로 실행 상태를 반환합니다.
     */
    fun isMacroRunning(): Boolean = isMacroRunning

    /**
     * 현재 실행 중인 매크로 ID를 반환합니다.
     */
    fun getCurrentMacroId(): String? = currentMacroId
}
