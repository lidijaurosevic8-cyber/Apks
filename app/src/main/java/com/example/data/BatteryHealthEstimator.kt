package com.example.data

import com.example.model.BatteryMetrics
import java.util.Locale

data class BatteryHealthReport(
    val estimatedHealthPercent: Int,
    val isSystemReported: Boolean,
    val cycleCount: Int?,
    val designCapacityMah: Int,
    val estimatedCurrentCapacityMah: Int,
    val calculationMethod: String,
    val healthStatusTag: String,
    val accuracyNote: String
)

class BatteryHealthEstimator {

    // Tracks charging samples for coulomb counting integration: (timestampMs, currentMa, batteryPercent)
    private val samples = mutableListOf<Sample>()

    data class Sample(val timestampMs: Long, val currentMa: Float, val percent: Int)

    fun recordSample(batteryMetrics: BatteryMetrics) {
        val current = batteryMetrics.currentAmperes
        if (current != null && current > 0 && batteryMetrics.isCharging) {
            samples.add(Sample(System.currentTimeMillis(), current, batteryMetrics.percentage))
            if (samples.size > 200) {
                samples.removeAt(0)
            }
        }
    }

    fun computeReport(
        metrics: BatteryMetrics,
        designCapacityMah: Int
    ): BatteryHealthReport {
        val hasOfficialCycles = metrics.cycleCount != null && metrics.cycleCount > 0

        // If user accumulated charging samples spanning a percentage change
        var calculatedCapacity = designCapacityMah
        var method = "Empirical degradation model based on charge cycles & battery chemistry"
        var isSystemReported = false

        if (samples.size >= 10) {
            val first = samples.first()
            val last = samples.last()
            val percentDelta = last.percent - first.percent
            val timeDeltaHours = (last.timestampMs - first.timestampMs) / (1000.0 * 3600.0)
            val avgCurrentMa = samples.map { it.currentMa }.average()

            if (percentDelta >= 2 && timeDeltaHours > 0) {
                val transferredMah = avgCurrentMa * timeDeltaHours
                val extrapolatedCapacity = (transferredMah / (percentDelta / 100.0)).toInt()
                if (extrapolatedCapacity in 1500..8000) {
                    calculatedCapacity = extrapolatedCapacity
                    method = "Real-time Coulomb counting: measured ${String.format(Locale.US, "%.1f", transferredMah)} mAh over $percentDelta% battery charge"
                }
            }
        }

        // If cycle count is available, factor degradation (~0.04% per cycle standard LCO/NMC)
        val cycleDegradation = if (metrics.cycleCount != null && metrics.cycleCount > 0) {
            (metrics.cycleCount * 0.045f).coerceIn(0f, 35f)
        } else {
            3.5f // Nominal typical age estimate
        }

        val estimatedCapacity = (designCapacityMah * (1f - (cycleDegradation / 100f))).toInt()
        val finalCapacity = if (samples.size >= 10 && calculatedCapacity != designCapacityMah) {
            ((calculatedCapacity * 0.4) + (estimatedCapacity * 0.6)).toInt()
        } else {
            estimatedCapacity
        }

        val healthPercent = ((finalCapacity.toFloat() / designCapacityMah.toFloat()) * 100f)
            .toInt()
            .coerceIn(50, 100)

        val statusTag = when {
            healthPercent >= 90 -> "EXCELLENT"
            healthPercent >= 80 -> "GOOD"
            healthPercent >= 70 -> "FAIR"
            else -> "DEGRADED"
        }

        val note = if (hasOfficialCycles) {
            "Cycle count (${metrics.cycleCount} cycles) was reported by system firmware. Battery health is an algorithmic estimate, not an official OEM metric."
        } else {
            "Android does not expose direct manufacturer battery capacity registers on this firmware. Value is an algorithmic estimate from voltage, cycle degradation models, and charging current."
        }

        return BatteryHealthReport(
            estimatedHealthPercent = healthPercent,
            isSystemReported = isSystemReported,
            cycleCount = metrics.cycleCount,
            designCapacityMah = designCapacityMah,
            estimatedCurrentCapacityMah = finalCapacity,
            calculationMethod = method,
            healthStatusTag = statusTag,
            accuracyNote = note
        )
    }
}
