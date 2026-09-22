package com.example.model

data class MetricHistoryPoint(
    val timestampMs: Long,
    val cpuUsagePercent: Float,
    val cpuFreqMhz: Int,
    val ramUsagePercent: Float,
    val batteryTempCelsius: Float,
    val batteryPercentage: Int,
    val fps: Float,
    val frameTimeMs: Float
)
