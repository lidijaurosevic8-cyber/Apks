package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BenchmarkRunResult
import com.example.model.BenchmarkType

@Entity(tableName = "benchmark_runs")
data class BenchmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val deviceModel: String,
    val benchmarkType: String,
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
    val executionDetails: String
) {
    fun toDomainModel(): BenchmarkRunResult {
        return BenchmarkRunResult(
            id = id,
            timestamp = timestamp,
            deviceModel = deviceModel,
            benchmarkType = runCatching { BenchmarkType.valueOf(benchmarkType) }.getOrDefault(BenchmarkType.COMBINED),
            durationSeconds = durationSeconds,
            cpuScore = cpuScore,
            gpuScore = gpuScore,
            combinedScore = combinedScore,
            averageFps = averageFps,
            minFps = minFps,
            maxFps = maxFps,
            startTempCelsius = startTempCelsius,
            maxTempCelsius = maxTempCelsius,
            startBatteryPercent = startBatteryPercent,
            endBatteryPercent = endBatteryPercent,
            throttlingDetected = throttlingDetected,
            executionDetails = executionDetails
        )
    }

    companion object {
        fun fromDomainModel(domain: BenchmarkRunResult): BenchmarkEntity {
            return BenchmarkEntity(
                id = domain.id,
                timestamp = domain.timestamp,
                deviceModel = domain.deviceModel,
                benchmarkType = domain.benchmarkType.name,
                durationSeconds = domain.durationSeconds,
                cpuScore = domain.cpuScore,
                gpuScore = domain.gpuScore,
                combinedScore = domain.combinedScore,
                averageFps = domain.averageFps,
                minFps = domain.minFps,
                maxFps = domain.maxFps,
                startTempCelsius = domain.startTempCelsius,
                maxTempCelsius = domain.maxTempCelsius,
                startBatteryPercent = domain.startBatteryPercent,
                endBatteryPercent = domain.endBatteryPercent,
                throttlingDetected = domain.throttlingDetected,
                executionDetails = domain.executionDetails
            )
        }
    }
}

fun BenchmarkRunResult.toEntity(): BenchmarkEntity = BenchmarkEntity.fromDomainModel(this)
fun BenchmarkEntity.toModel(): BenchmarkRunResult = this.toDomainModel()
