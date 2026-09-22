package com.example.data

import android.os.SystemClock
import com.example.model.BenchmarkRunResult
import com.example.model.BenchmarkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class BenchmarkEngine(
    private val hardwareMonitor: HardwareMonitor
) {

    suspend fun runCpuBenchmark(
        durationSeconds: Int,
        onProgress: (Float, String) -> Unit
    ): BenchmarkRunResult = withContext(Dispatchers.Default) {
        val startBattery = hardwareMonitor.readBatteryMetrics()
        val startThermal = hardwareMonitor.readThermalMetrics(startBattery.temperatureCelsius)
        val startCpu = hardwareMonitor.readCpuMetrics()

        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val startTime = SystemClock.elapsedRealtime()
        val endTime = startTime + (durationSeconds * 1000L)

        val totalOps = AtomicLong(0L)
        var maxObservedTemp = startThermal.batteryTempCelsius
        var throttlingObserved = startThermal.isThrottling

        coroutineScope {
            // Monitor loop during run
            val monitorJob = async {
                while (SystemClock.elapsedRealtime() < endTime && isActive) {
                    val currentElapsed = SystemClock.elapsedRealtime() - startTime
                    val progress = (currentElapsed.toFloat() / (durationSeconds * 1000f)).coerceIn(0f, 1f)
                    val currentBattery = hardwareMonitor.readBatteryMetrics()
                    val currentThermal = hardwareMonitor.readThermalMetrics(currentBattery.temperatureCelsius)

                    if (currentThermal.batteryTempCelsius > maxObservedTemp) {
                        maxObservedTemp = currentThermal.batteryTempCelsius
                    }
                    if (currentThermal.isThrottling) {
                        throttlingObserved = true
                    }

                    onProgress(
                        progress,
                        "Stress-testing $cores CPU cores • Ops: ${totalOps.get()} • Temp: ${String.format(Locale.US, "%.1f", maxObservedTemp)}°C"
                    )
                    delay(250)
                }
            }

            // Parallel worker threads
            val workerJobs = (0 until cores).map { coreId ->
                async(Dispatchers.Default) {
                    var localOps = 0L
                    val md5 = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(64) { (it + coreId).toByte() }

                    while (SystemClock.elapsedRealtime() < endTime && isActive) {
                        // 1. Prime sieve batch
                        runPrimeSieve(10_000)
                        localOps += 100

                        // 2. Cryptographic hashing
                        for (k in 0..20) {
                            md5.update(buffer)
                            val digest = md5.digest()
                            System.arraycopy(digest, 0, buffer, 0, min(buffer.size, digest.size))
                        }
                        localOps += 200

                        // 3. Matrix float math
                        runMatrixMultiplication(32)
                        localOps += 300

                        totalOps.addAndGet(localOps)
                        localOps = 0L
                    }
                }
            }

            workerJobs.awaitAll()
            monitorJob.cancel()
        }

        val endBattery = hardwareMonitor.readBatteryMetrics()
        val endThermal = hardwareMonitor.readThermalMetrics(endBattery.temperatureCelsius)
        if (endThermal.batteryTempCelsius > maxObservedTemp) {
            maxObservedTemp = endThermal.batteryTempCelsius
        }
        if (endThermal.isThrottling) {
            throttlingObserved = true
        }

        val ops = totalOps.get()
        // Calibrate score: ~10,000 ops/sec per mid-range core = 1,000 pts
        val score = ((ops / durationSeconds.toDouble()) * 0.12).roundToInt().coerceAtLeast(100)

        BenchmarkRunResult(
            deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            benchmarkType = BenchmarkType.CPU,
            durationSeconds = durationSeconds,
            cpuScore = score,
            gpuScore = 0,
            combinedScore = score,
            averageFps = 0f,
            minFps = 0f,
            maxFps = 0f,
            startTempCelsius = startThermal.batteryTempCelsius,
            maxTempCelsius = maxObservedTemp,
            startBatteryPercent = startBattery.percentage,
            endBatteryPercent = endBattery.percentage,
            throttlingDetected = throttlingObserved,
            executionDetails = "Multi-core workload on $cores threads: Prime Sieve, SHA-256 rounds & 32x32 Matrix transforms. Total ops: $ops"
        )
    }

    suspend fun runGpuBenchmark(
        durationSeconds: Int,
        fpsHistory: List<Float>,
        onProgress: (Float, String) -> Unit
    ): BenchmarkRunResult = withContext(Dispatchers.Default) {
        val startBattery = hardwareMonitor.readBatteryMetrics()
        val startThermal = hardwareMonitor.readThermalMetrics(startBattery.temperatureCelsius)
        val startTime = SystemClock.elapsedRealtime()
        val endTime = startTime + (durationSeconds * 1000L)

        var maxObservedTemp = startThermal.batteryTempCelsius
        var throttlingObserved = startThermal.isThrottling

        while (SystemClock.elapsedRealtime() < endTime && isActive) {
            val currentElapsed = SystemClock.elapsedRealtime() - startTime
            val progress = (currentElapsed.toFloat() / (durationSeconds * 1000f)).coerceIn(0f, 1f)
            val currentBattery = hardwareMonitor.readBatteryMetrics()
            val currentThermal = hardwareMonitor.readThermalMetrics(currentBattery.temperatureCelsius)

            if (currentThermal.batteryTempCelsius > maxObservedTemp) {
                maxObservedTemp = currentThermal.batteryTempCelsius
            }
            if (currentThermal.isThrottling) {
                throttlingObserved = true
            }

            val lastFps = fpsHistory.lastOrNull() ?: 60f
            onProgress(
                progress,
                "Rendering particle physics & 3D geometry • FPS: ${String.format(Locale.US, "%.1f", lastFps)} • Temp: ${String.format(Locale.US, "%.1f", maxObservedTemp)}°C"
            )
            delay(200)
        }

        val endBattery = hardwareMonitor.readBatteryMetrics()
        val endThermal = hardwareMonitor.readThermalMetrics(endBattery.temperatureCelsius)
        if (endThermal.batteryTempCelsius > maxObservedTemp) {
            maxObservedTemp = endThermal.batteryTempCelsius
        }
        if (endThermal.isThrottling) {
            throttlingObserved = true
        }

        val validFps = fpsHistory.filter { it > 5f }
        val avgFps = if (validFps.isNotEmpty()) validFps.average().toFloat() else 60f
        val minFps = if (validFps.isNotEmpty()) validFps.minOrNull() ?: 30f else 30f
        val maxFps = if (validFps.isNotEmpty()) validFps.maxOrNull() ?: 60f else 60f

        // GPU Score formula: weighted average of avg FPS and 1% low min FPS
        val score = ((avgFps * 85f) + (minFps * 35f)).roundToInt().coerceAtLeast(150)

        BenchmarkRunResult(
            deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            benchmarkType = BenchmarkType.GPU,
            durationSeconds = durationSeconds,
            cpuScore = 0,
            gpuScore = score,
            combinedScore = score,
            averageFps = avgFps,
            minFps = minFps,
            maxFps = maxFps,
            startTempCelsius = startThermal.batteryTempCelsius,
            maxTempCelsius = maxObservedTemp,
            startBatteryPercent = startBattery.percentage,
            endBatteryPercent = endBattery.percentage,
            throttlingDetected = throttlingObserved,
            executionDetails = "Hardware graphics pipeline test: 1,200 particle dynamics, 3D wireframe mesh transformations. Avg FPS: ${String.format(Locale.US, "%.1f", avgFps)}"
        )
    }

    suspend fun runCombinedBenchmark(
        durationSeconds: Int,
        fpsHistory: List<Float>,
        onProgress: (Float, String) -> Unit
    ): BenchmarkRunResult = withContext(Dispatchers.Default) {
        val cpuDuration = durationSeconds / 2
        val gpuDuration = durationSeconds - cpuDuration

        onProgress(0.05f, "Stage 1/2: Initializing CPU multi-thread stress test...")
        val cpuResult = runCpuBenchmark(cpuDuration) { prog, msg ->
            onProgress(prog * 0.5f, "Stage 1/2: $msg")
        }

        onProgress(0.55f, "Stage 2/2: Initializing GPU graphical workload...")
        val gpuResult = runGpuBenchmark(gpuDuration, fpsHistory) { prog, msg ->
            onProgress(0.5f + (prog * 0.5f), "Stage 2/2: $msg")
        }

        val combinedScore = ((cpuResult.cpuScore * 0.5) + (gpuResult.gpuScore * 0.5)).roundToInt()
        val maxTemp = max(cpuResult.maxTempCelsius, gpuResult.maxTempCelsius)
        val throttled = cpuResult.throttlingDetected || gpuResult.throttlingDetected

        BenchmarkRunResult(
            deviceModel = cpuResult.deviceModel,
            benchmarkType = BenchmarkType.COMBINED,
            durationSeconds = durationSeconds,
            cpuScore = cpuResult.cpuScore,
            gpuScore = gpuResult.gpuScore,
            combinedScore = combinedScore,
            averageFps = gpuResult.averageFps,
            minFps = gpuResult.minFps,
            maxFps = gpuResult.maxFps,
            startTempCelsius = cpuResult.startTempCelsius,
            maxTempCelsius = maxTemp,
            startBatteryPercent = cpuResult.startBatteryPercent,
            endBatteryPercent = gpuResult.endBatteryPercent,
            throttlingDetected = throttled,
            executionDetails = "Full-spectrum system benchmark: CPU (${cpuResult.cpuScore} pts) + GPU (${gpuResult.gpuScore} pts). Composite Score: $combinedScore"
        )
    }

    private fun runPrimeSieve(limit: Int): Int {
        val isPrime = BooleanArray(limit + 1) { true }
        isPrime[0] = false
        isPrime[1] = false
        var p = 2
        while (p * p <= limit) {
            if (isPrime[p]) {
                var i = p * p
                while (i <= limit) {
                    isPrime[i] = false
                    i += p
                }
            }
            p++
        }
        var count = 0
        for (b in isPrime) if (b) count++
        return count
    }

    private fun runMatrixMultiplication(n: Int): Float {
        val a = Array(n) { FloatArray(n) { 1.05f } }
        val b = Array(n) { FloatArray(n) { 0.95f } }
        val c = Array(n) { FloatArray(n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                var sum = 0f
                for (k in 0 until n) {
                    sum += a[i][k] * b[k][j]
                }
                c[i][j] = sum
            }
        }
        return c[0][0]
    }
}
