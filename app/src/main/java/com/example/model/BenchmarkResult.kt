package com.example.model

enum class BenchmarkType {
    CPU,
    GPU,
    COMBINED
}

data class BenchmarkRunResult(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String,
    val benchmarkType: BenchmarkType,
    val durationSeconds: Int,
    val cpuScore: Int,
    val gpuScore: Int,
    val combinedScore: Int,
    val averageFps: Float,
    val minFps: Float,
    val maxFps: Float,
    val startTempCelsius: Float,
    val maxTempCelsius: Float,
    val startBatteryPercent: Int,
    val endBatteryPercent: Int,
    val throttlingDetected: Boolean,
    val executionDetails: String = ""
) {
    val batteryDeltaPercent: Int
        get() = startBatteryPercent - endBatteryPercent

    val tempDeltaCelsius: Float
        get() = (maxTempCelsius - startTempCelsius).coerceAtLeast(0f)
}
