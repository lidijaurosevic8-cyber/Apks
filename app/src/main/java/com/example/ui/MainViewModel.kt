package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppSettings
import com.example.data.BatteryHealthEstimator
import com.example.data.BatteryHealthReport
import com.example.data.BenchmarkEngine
import com.example.data.BenchmarkRepository
import com.example.data.FpsMeter
import com.example.data.HardwareMonitor
import com.example.data.SettingsRepository
import com.example.data.db.AppDatabase
import com.example.model.BatteryMetrics
import com.example.model.BenchmarkRunResult
import com.example.model.BenchmarkType
import com.example.model.CpuMetrics
import com.example.model.DeviceInfo
import com.example.model.FpsMetrics
import com.example.model.GpuMetrics
import com.example.model.MetricHistoryPoint
import com.example.model.RamMetrics
import com.example.model.ThermalMetrics
import com.example.service.PerformanceMonitoringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class BenchmarkUiState {
    object Idle : BenchmarkUiState()
    data class Running(
        val progress: Float,
        val statusMessage: String,
        val type: BenchmarkType
    ) : BenchmarkUiState()
    data class Completed(val result: BenchmarkRunResult) : BenchmarkUiState()
    data class Error(val message: String) : BenchmarkUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val hardwareMonitor = HardwareMonitor(application)
    private val fpsMeter = FpsMeter()
    private val benchmarkEngine = BenchmarkEngine(hardwareMonitor)
    private val database = AppDatabase.getInstance(application)
    private val benchmarkRepository = BenchmarkRepository(database.benchmarkDao())
    val settingsRepository = SettingsRepository(application)
    private val batteryHealthEstimator = BatteryHealthEstimator()

    // Real-time telemetry StateFlows
    private val _cpuMetrics = MutableStateFlow(CpuMetrics())
    val cpuMetrics: StateFlow<CpuMetrics> = _cpuMetrics.asStateFlow()

    private val _gpuMetrics = MutableStateFlow(GpuMetrics())
    val gpuMetrics: StateFlow<GpuMetrics> = _gpuMetrics.asStateFlow()

    private val _ramMetrics = MutableStateFlow(RamMetrics())
    val ramMetrics: StateFlow<RamMetrics> = _ramMetrics.asStateFlow()

    private val _batteryMetrics = MutableStateFlow(BatteryMetrics())
    val batteryMetrics: StateFlow<BatteryMetrics> = _batteryMetrics.asStateFlow()

    private val _thermalMetrics = MutableStateFlow(ThermalMetrics())
    val thermalMetrics: StateFlow<ThermalMetrics> = _thermalMetrics.asStateFlow()

    val fpsMetrics: StateFlow<FpsMetrics> = fpsMeter.fpsMetrics

    private val _batteryHealthReport = MutableStateFlow<BatteryHealthReport?>(null)
    val batteryHealthReport: StateFlow<BatteryHealthReport?> = _batteryHealthReport.asStateFlow()

    // Static Device Info
    val deviceInfo: DeviceInfo by lazy { hardwareMonitor.getDeviceInfo() }

    // Rolling History Points for Real-time Graphs
    private val _historyPoints = MutableStateFlow<List<MetricHistoryPoint>>(emptyList())
    val historyPoints: StateFlow<List<MetricHistoryPoint>> = _historyPoints.asStateFlow()

    // Benchmark state
    private val _benchmarkState = MutableStateFlow<BenchmarkUiState>(BenchmarkUiState.Idle)
    val benchmarkState: StateFlow<BenchmarkUiState> = _benchmarkState.asStateFlow()

    // Saved runs from database
    val savedBenchmarkRuns: StateFlow<List<BenchmarkRunResult>> = benchmarkRepository.allRuns.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Run Comparison Selection
    private val _comparisonRunIds = MutableStateFlow<Set<Long>>(emptySet())
    val comparisonRunIds: StateFlow<Set<Long>> = _comparisonRunIds.asStateFlow()

    // App Settings
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    private var telemetryJob: Job? = null
    private var isAppForeground = false

    init {
        // Initial static read
        viewModelScope.launch(Dispatchers.Default) {
            _gpuMetrics.value = hardwareMonitor.readGpuMetrics()
            updateTelemetry()
        }
    }

    fun onResume() {
        isAppForeground = true
        startTelemetryLoop()
    }

    fun onPause() {
        isAppForeground = false
        if (!settings.value.backgroundMonitoringEnabled) {
            stopTelemetryLoop()
        }
    }

    private fun startTelemetryLoop() {
        if (telemetryJob?.isActive == true) return
        telemetryJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                updateTelemetry()
                val interval = settings.value.refreshRateMs.coerceIn(300L, 5000L)
                delay(interval)
            }
        }
    }

    private fun stopTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = null
    }

    private fun updateTelemetry() {
        try {
            val cpu = hardwareMonitor.readCpuMetrics()
            val gpu = hardwareMonitor.readGpuMetrics()
            val ram = hardwareMonitor.readRamMetrics()
            val battery = hardwareMonitor.readBatteryMetrics()
            val thermal = hardwareMonitor.readThermalMetrics(battery.temperatureCelsius)
            val fps = fpsMeter.fpsMetrics.value

            _cpuMetrics.value = cpu
            _gpuMetrics.value = gpu
            _ramMetrics.value = ram
            _batteryMetrics.value = battery
            _thermalMetrics.value = thermal

            batteryHealthEstimator.recordSample(battery)
            _batteryHealthReport.value = batteryHealthEstimator.computeReport(
                battery,
                settings.value.designBatteryCapacityMah
            )

            // Update rolling graph history
            val now = System.currentTimeMillis()
            val maxHistorySec = settings.value.graphHistorySeconds
            val maxPoints = (maxHistorySec * 1000L / settings.value.refreshRateMs).toInt().coerceIn(10, 600)

            val newPoint = MetricHistoryPoint(
                timestampMs = now,
                cpuUsagePercent = cpu.overallUsagePercent,
                cpuFreqMhz = cpu.primaryFreqMhz,
                ramUsagePercent = ram.usagePercent,
                batteryTempCelsius = battery.temperatureCelsius,
                batteryPercentage = battery.percentage,
                fps = fps.currentFps,
                frameTimeMs = fps.frameTimeMs
            )

            val currentList = _historyPoints.value.toMutableList()
            currentList.add(newPoint)
            if (currentList.size > maxPoints) {
                currentList.removeAt(0)
            }
            _historyPoints.value = currentList
        } catch (_: Exception) {}
    }

    // FPS Meter Controls
    fun startFpsMeter() {
        fpsMeter.start()
    }

    fun stopFpsMeter() {
        fpsMeter.stop()
    }

    fun resetFpsMeter() {
        fpsMeter.reset()
    }

    // Benchmark Execution
    fun runBenchmark(type: BenchmarkType) {
        if (_benchmarkState.value is BenchmarkUiState.Running) return

        val duration = settings.value.benchmarkDurationSeconds
        _benchmarkState.value = BenchmarkUiState.Running(
            progress = 0f,
            statusMessage = "Preparing benchmark workload...",
            type = type
        )

        viewModelScope.launch(Dispatchers.Default) {
            try {
                fpsMeter.start()
                val result = when (type) {
                    BenchmarkType.CPU -> {
                        benchmarkEngine.runCpuBenchmark(duration) { progress, status ->
                            _benchmarkState.value = BenchmarkUiState.Running(progress, status, type)
                        }
                    }
                    BenchmarkType.GPU -> {
                        val fpsList = _historyPoints.value.map { it.fps }
                        benchmarkEngine.runGpuBenchmark(duration, fpsList) { progress, status ->
                            _benchmarkState.value = BenchmarkUiState.Running(progress, status, type)
                        }
                    }
                    BenchmarkType.COMBINED -> {
                        val fpsList = _historyPoints.value.map { it.fps }
                        benchmarkEngine.runCombinedBenchmark(duration, fpsList) { progress, status ->
                            _benchmarkState.value = BenchmarkUiState.Running(progress, status, type)
                        }
                    }
                }

                // Save to Room DB
                val savedId = benchmarkRepository.saveRun(result)
                val finalResult = result.copy(id = savedId)

                _benchmarkState.value = BenchmarkUiState.Completed(finalResult)
            } catch (e: Exception) {
                _benchmarkState.value = BenchmarkUiState.Error(e.message ?: "Benchmark failed")
            } finally {
                fpsMeter.stop()
            }
        }
    }

    fun resetBenchmarkState() {
        _benchmarkState.value = BenchmarkUiState.Idle
    }

    fun deleteBenchmarkRun(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            benchmarkRepository.deleteRun(id)
            val current = _comparisonRunIds.value.toMutableSet()
            current.remove(id)
            _comparisonRunIds.value = current
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            benchmarkRepository.clearHistory()
            _comparisonRunIds.value = emptySet()
        }
    }

    fun toggleComparison(id: Long) {
        val current = _comparisonRunIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            if (current.size < 4) {
                current.add(id)
            }
        }
        _comparisonRunIds.value = current
    }

    // Settings actions
    fun setRefreshRate(ms: Long) {
        settingsRepository.updateRefreshRate(ms)
    }

    fun setGraphHistory(seconds: Int) {
        settingsRepository.updateGraphHistorySeconds(seconds)
    }

    fun setTemperatureUnit(useFahrenheit: Boolean) {
        settingsRepository.updateTemperatureUnit(useFahrenheit)
    }

    fun setDarkTheme(isDark: Boolean) {
        settingsRepository.updateDarkTheme(isDark)
    }

    fun setBenchmarkDuration(seconds: Int) {
        settingsRepository.updateBenchmarkDuration(seconds)
    }

    fun setDesignBatteryCapacity(mah: Int) {
        settingsRepository.updateDesignBatteryCapacity(mah)
    }

    fun toggleBackgroundMonitoring(enabled: Boolean) {
        settingsRepository.updateBackgroundMonitoring(enabled)
        val app = getApplication<Application>()
        if (enabled) {
            PerformanceMonitoringService.start(app)
        } else {
            PerformanceMonitoringService.stop(app)
        }
    }
}
