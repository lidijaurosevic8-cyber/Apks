package com.example.model

/**
 * Metric availability status to distinguish measured, system-reported, and unavailable values.
 */
enum class MetricSource {
    MEASURED,
    SYSTEM_REPORTED,
    ESTIMATED,
    UNAVAILABLE
}

data class CpuCoreUsage(
    val coreIndex: Int,
    val usagePercent: Float,
    val currentFreqKHz: Long,
    val minFreqKHz: Long = 0,
    val maxFreqKHz: Long = 0,
    val isOnline: Boolean = true
)

data class CpuMetrics(
    val overallUsagePercent: Float = 0f,
    val coreUsages: List<CpuCoreUsage> = emptyList(),
    val coreCount: Int = Runtime.getRuntime().availableProcessors(),
    val architecture: String = "Unknown",
    val primaryFreqMhz: Int = 0,
    val minFreqMhz: Int = 0,
    val maxFreqMhz: Int = 0,
    val governor: String = "Unknown",
    val loadAverage1Min: Float = 0f,
    val isFrequencyAvailable: Boolean = true,
    val availabilityNote: String? = null
)

data class GpuMetrics(
    val vendor: String = "Unknown",
    val renderer: String = "Unknown",
    val version: String = "Unknown",
    val utilizationPercent: Float? = null, // Null indicates unavailable
    val frequencyMhz: Int? = null,         // Null indicates unavailable
    val maxFrequencyMhz: Int? = null,
    val isUtilizationSupported: Boolean = false,
    val unavailableReason: String = "GPU utilization is not exposed by this device's Android firmware (SELinux restricted)."
)

data class RamMetrics(
    val totalBytes: Long = 0L,
    val availableBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usagePercent: Float = 0f,
    val thresholdBytes: Long = 0L,
    val isLowMemory: Boolean = false,
    val appUsedBytes: Long = 0L,
    val appMaxBytes: Long = 0L
)

data class BatteryMetrics(
    val percentage: Int = 0,
    val status: String = "Unknown", // Charging, Discharging, Full, etc.
    val isCharging: Boolean = false,
    val currentAmperes: Float? = null, // in mA (null if not readable)
    val voltageMilliVolts: Int = 0,     // in mV
    val temperatureCelsius: Float = 0f, // in °C
    val powerWatts: Float? = null,      // in Watts
    val health: String = "Good",
    val pluggedSource: String = "Unplugged",
    val technology: String = "Li-ion",
    val cycleCount: Int? = null,        // API 34+ or sysfs
    val capacityEstimateMah: Int = 0,
    val remainingTimeMinutes: Long? = null
)

enum class DeviceThermalState {
    OPTIMAL,
    LIGHT,
    MODERATE,
    SEVERE,
    CRITICAL,
    EMERGENCY,
    SHUTDOWN;

    val isThrottling: Boolean
        get() = this >= MODERATE
}

data class ThermalMetrics(
    val batteryTempCelsius: Float = 0f,
    val cpuTempCelsius: Float? = null,
    val gpuTempCelsius: Float? = null,
    val thermalState: DeviceThermalState = DeviceThermalState.OPTIMAL,
    val thermalHeadroom: Float? = null, // Float where 1.0 is throttling threshold
    val isThrottling: Boolean = false,
    val throttlingWarning: String? = null,
    val thermalZonesCount: Int = 0
)

data class FpsMetrics(
    val currentFps: Float = 60f,
    val averageFps: Float = 60f,
    val minFps: Float = 60f,
    val maxFps: Float = 60f,
    val frameTimeMs: Float = 16.6f,
    val spikedFramesCount: Int = 0,
    val totalFramesTracked: Int = 0,
    val isMeasuring: Boolean = false
)
