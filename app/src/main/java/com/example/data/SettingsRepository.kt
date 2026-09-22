package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val refreshRateMs: Long = 1000L,
    val graphHistorySeconds: Int = 30,
    val useFahrenheit: Boolean = false,
    val isDarkTheme: Boolean = true,
    val benchmarkDurationSeconds: Int = 10,
    val backgroundMonitoringEnabled: Boolean = false,
    val designBatteryCapacityMah: Int = 5000
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("perf_lab_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            refreshRateMs = prefs.getLong("refresh_rate_ms", 1000L),
            graphHistorySeconds = prefs.getInt("graph_history_sec", 30),
            useFahrenheit = prefs.getBoolean("use_fahrenheit", false),
            isDarkTheme = prefs.getBoolean("is_dark_theme", true),
            benchmarkDurationSeconds = prefs.getInt("benchmark_duration_sec", 10),
            backgroundMonitoringEnabled = prefs.getBoolean("bg_monitoring", false),
            designBatteryCapacityMah = prefs.getInt("battery_design_mah", 5000)
        )
    }

    fun updateRefreshRate(ms: Long) {
        prefs.edit().putLong("refresh_rate_ms", ms).apply()
        _settings.value = _settings.value.copy(refreshRateMs = ms)
    }

    fun updateGraphHistorySeconds(seconds: Int) {
        prefs.edit().putInt("graph_history_sec", seconds).apply()
        _settings.value = _settings.value.copy(graphHistorySeconds = seconds)
    }

    fun updateTemperatureUnit(useFahrenheit: Boolean) {
        prefs.edit().putBoolean("use_fahrenheit", useFahrenheit).apply()
        _settings.value = _settings.value.copy(useFahrenheit = useFahrenheit)
    }

    fun updateDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
        _settings.value = _settings.value.copy(isDarkTheme = isDark)
    }

    fun updateBenchmarkDuration(seconds: Int) {
        prefs.edit().putInt("benchmark_duration_sec", seconds).apply()
        _settings.value = _settings.value.copy(benchmarkDurationSeconds = seconds)
    }

    fun updateBackgroundMonitoring(enabled: Boolean) {
        prefs.edit().putBoolean("bg_monitoring", enabled).apply()
        _settings.value = _settings.value.copy(backgroundMonitoringEnabled = enabled)
    }

    fun updateDesignBatteryCapacity(mah: Int) {
        prefs.edit().putInt("battery_design_mah", mah).apply()
        _settings.value = _settings.value.copy(designBatteryCapacityMah = mah)
    }
}
