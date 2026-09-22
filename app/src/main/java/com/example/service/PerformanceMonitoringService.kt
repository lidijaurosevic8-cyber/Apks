package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.HardwareMonitor
import com.example.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class PerformanceMonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var hardwareMonitor: HardwareMonitor
    private lateinit var settingsRepository: SettingsRepository
    private var monitoringJob: Job? = null

    companion object {
        const val CHANNEL_ID = "perf_monitoring_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.service.STOP_MONITORING"

        fun start(context: Context) {
            val intent = Intent(context, PerformanceMonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PerformanceMonitoringService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        hardwareMonitor = HardwareMonitor(this)
        settingsRepository = SettingsRepository(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            settingsRepository.updateBackgroundMonitoring(false)
            stopSelf()
            return START_NOT_STICKY
        }

        val initialNotification = buildNotification("Monitoring active...", "Reading hardware telemetry")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        startMonitoringLoop()
        return START_STICKY
    }

    private fun startMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    val cpu = hardwareMonitor.readCpuMetrics()
                    val ram = hardwareMonitor.readRamMetrics()
                    val battery = hardwareMonitor.readBatteryMetrics()
                    val useFahrenheit = settingsRepository.settings.value.useFahrenheit

                    val tempStr = if (useFahrenheit) {
                        val f = (battery.temperatureCelsius * 9f / 5f) + 32f
                        String.format(Locale.US, "%.1f°F", f)
                    } else {
                        String.format(Locale.US, "%.1f°C", battery.temperatureCelsius)
                    }

                    val title = "CPU ${cpu.overallUsagePercent.toInt()}% • RAM ${ram.usagePercent.toInt()}% • Battery ${battery.percentage}% • $tempStr"
                    val content = "${cpu.coreCount} Cores @ ${cpu.primaryFreqMhz}MHz • Avail RAM: ${ram.availableBytes / (1024 * 1024)}MB • ${battery.status}"

                    updateNotification(title, content)
                } catch (_: Exception) {}

                val refreshMs = settingsRepository.settings.value.refreshRateMs.coerceAtLeast(1000L)
                delay(refreshMs)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hardware Performance Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time CPU, RAM, Battery and Thermal metrics in notification drawer"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingAppIntent = PendingIntent.getActivity(
            this, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PerformanceMonitoringService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pendingStopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = buildNotification(title, content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        monitoringJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
