package com.example.data

import android.content.Context
import android.content.Intent
import com.example.model.BenchmarkRunResult
import com.example.model.DeviceInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun formatToCsv(runs: List<BenchmarkRunResult>): String {
        val sb = StringBuilder()
        sb.append("ID,Timestamp,Date,Device,Type,DurationSec,CPUScore,GPUScore,CombinedScore,AvgFPS,MinFPS,MaxFPS,StartTempC,MaxTempC,StartBatteryPct,EndBatteryPct,ThrottlingDetected\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (run in runs) {
            val dateStr = sdf.format(Date(run.timestamp))
            sb.append("${run.id},")
            sb.append("${run.timestamp},")
            sb.append("\"$dateStr\",")
            sb.append("\"${run.deviceModel}\",")
            sb.append("${run.benchmarkType.name},")
            sb.append("${run.durationSeconds},")
            sb.append("${run.cpuScore},")
            sb.append("${run.gpuScore},")
            sb.append("${run.combinedScore},")
            sb.append(String.format(Locale.US, "%.1f,", run.averageFps))
            sb.append(String.format(Locale.US, "%.1f,", run.minFps))
            sb.append(String.format(Locale.US, "%.1f,", run.maxFps))
            sb.append(String.format(Locale.US, "%.1f,", run.startTempCelsius))
            sb.append(String.format(Locale.US, "%.1f,", run.maxTempCelsius))
            sb.append("${run.startBatteryPercent},")
            sb.append("${run.endBatteryPercent},")
            sb.append("${run.throttlingDetected}\n")
        }
        return sb.toString()
    }

    fun formatToJson(runs: List<BenchmarkRunResult>, device: DeviceInfo): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"device\": {\n")
        sb.append("    \"manufacturer\": \"${device.manufacturer}\",\n")
        sb.append("    \"model\": \"${device.model}\",\n")
        sb.append("    \"soc\": \"${device.socModel}\",\n")
        sb.append("    \"androidVersion\": \"${device.androidVersion}\",\n")
        sb.append("    \"cpuCores\": ${device.cpuCores},\n")
        sb.append("    \"gpuRenderer\": \"${device.gpuRenderer}\"\n")
        sb.append("  },\n")
        sb.append("  \"benchmark_runs\": [\n")

        runs.forEachIndexed { index, run ->
            sb.append("    {\n")
            sb.append("      \"id\": ${run.id},\n")
            sb.append("      \"isoDate\": \"${sdf.format(Date(run.timestamp))}\",\n")
            sb.append("      \"type\": \"${run.benchmarkType.name}\",\n")
            sb.append("      \"durationSeconds\": ${run.durationSeconds},\n")
            sb.append("      \"cpuScore\": ${run.cpuScore},\n")
            sb.append("      \"gpuScore\": ${run.gpuScore},\n")
            sb.append("      \"combinedScore\": ${run.combinedScore},\n")
            sb.append("      \"averageFps\": ${String.format(Locale.US, "%.1f", run.averageFps)},\n")
            sb.append("      \"minFps\": ${String.format(Locale.US, "%.1f", run.minFps)},\n")
            sb.append("      \"maxFps\": ${String.format(Locale.US, "%.1f", run.maxFps)},\n")
            sb.append("      \"startTempCelsius\": ${String.format(Locale.US, "%.1f", run.startTempCelsius)},\n")
            sb.append("      \"maxTempCelsius\": ${String.format(Locale.US, "%.1f", run.maxTempCelsius)},\n")
            sb.append("      \"startBattery\": ${run.startBatteryPercent},\n")
            sb.append("      \"endBattery\": ${run.endBatteryPercent},\n")
            sb.append("      \"throttlingDetected\": ${run.throttlingDetected}\n")
            sb.append(if (index == runs.size - 1) "    }\n" else "    },\n")
        }

        sb.append("  ]\n")
        sb.append("}\n")
        return sb.toString()
    }

    fun formatTextReport(run: BenchmarkRunResult, device: DeviceInfo): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return """
========================================
 PHONE PERFORMANCE LAB - BENCHMARK REPORT
========================================
Date: ${sdf.format(Date(run.timestamp))}
Device: ${device.manufacturer} ${device.model}
SoC / Chipset: ${device.socModel}
CPU: ${device.cpuCores} Cores (${device.cpuArchitecture})
GPU: ${device.gpuRenderer} (${device.gpuVendor})
OS: Android ${device.androidVersion} (SDK ${device.sdkVersion})

----------------------------------------
BENCHMARK RESULTS [${run.benchmarkType.name}]
----------------------------------------
• Performance Score:  ${run.combinedScore} PTS
• CPU Score:           ${run.cpuScore} PTS
• GPU Score:           ${run.gpuScore} PTS
• Duration:            ${run.durationSeconds} seconds

GRAPHICS & FRAME TIMING
• Average FPS:         ${String.format(Locale.US, "%.1f", run.averageFps)} FPS
• 1% Low Min FPS:      ${String.format(Locale.US, "%.1f", run.minFps)} FPS
• Peak Max FPS:        ${String.format(Locale.US, "%.1f", run.maxFps)} FPS

THERMAL & POWER
• Initial Temp:        ${String.format(Locale.US, "%.1f", run.startTempCelsius)}°C
• Maximum Peak Temp:   ${String.format(Locale.US, "%.1f", run.maxTempCelsius)}°C
• Temperature Rise:    +${String.format(Locale.US, "%.1f", run.tempDeltaCelsius)}°C
• Battery Delta:       -${run.batteryDeltaPercent}% (${run.startBatteryPercent}% -> ${run.endBatteryPercent}%)
• Thermal Throttling:  ${if (run.throttlingDetected) "DETECTED (Clock reduction engaged)" else "NONE (Optimal Headroom)"}

Workload Details:
${run.executionDetails}

========================================
Generated by Phone Performance Lab
========================================
        """.trimIndent()
    }

    fun shareText(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
