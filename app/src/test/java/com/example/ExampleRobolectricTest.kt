package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.BatteryHealthEstimator
import com.example.data.ExportHelper
import com.example.data.db.AppDatabase
import com.example.data.db.toEntity
import com.example.data.db.toModel
import com.example.model.BatteryMetrics
import com.example.model.BenchmarkRunResult
import com.example.model.BenchmarkType
import com.example.model.DeviceInfo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Phone Performance Lab", appName)
    }

    @Test
    fun `test battery health estimator algorithm`() {
        val estimator = BatteryHealthEstimator()
        val metrics = BatteryMetrics(
            percentage = 80,
            status = "Discharging",
            voltageMilliVolts = 4100,
            temperatureCelsius = 28.5f,
            currentAmperes = 450f,
            powerWatts = 1.84f,
            cycleCount = 150
        )

        val report = estimator.computeReport(metrics, 4500)
        assertNotNull(report)
        assertTrue(report.estimatedHealthPercent in 80..100)
        assertEquals(4500, report.designCapacityMah)
        assertEquals(150, report.cycleCount)
    }

    @Test
    fun `test export helper csv formatting`() {
        val run = BenchmarkRunResult(
            id = 1L,
            timestamp = 1700000000000L,
            deviceModel = "Pixel 8",
            benchmarkType = BenchmarkType.COMBINED,
            durationSeconds = 10,
            cpuScore = 1500,
            gpuScore = 1800,
            combinedScore = 1650,
            averageFps = 60.0f,
            minFps = 58.0f,
            maxFps = 60.5f,
            startTempCelsius = 30.0f,
            maxTempCelsius = 35.0f,
            startBatteryPercent = 90,
            endBatteryPercent = 89,
            throttlingDetected = false,
            executionDetails = "Combined run"
        )

        val csv = ExportHelper.formatToCsv(listOf(run))
        assertTrue(csv.contains("Pixel 8"))
        assertTrue(csv.contains("COMBINED"))
        assertTrue(csv.contains("1650"))
    }

    @Test
    fun `test room database save and retrieve benchmark`() = runBlocking {
        val dao = db.benchmarkDao()
        val run = BenchmarkRunResult(
            deviceModel = "Test Phone",
            benchmarkType = BenchmarkType.CPU,
            durationSeconds = 5,
            cpuScore = 2000,
            gpuScore = 0,
            combinedScore = 2000,
            averageFps = 0f,
            minFps = 0f,
            maxFps = 0f,
            startTempCelsius = 25f,
            maxTempCelsius = 29f,
            startBatteryPercent = 100,
            endBatteryPercent = 99,
            throttlingDetected = false,
            executionDetails = "CPU Stress"
        )

        val entity = run.toEntity()
        val id = dao.insertRun(entity)
        assertTrue(id > 0)

        val retrieved = dao.getRunById(id)
        assertNotNull(retrieved)
        assertEquals(2000, retrieved?.toModel()?.cpuScore)
        assertEquals(BenchmarkType.CPU, retrieved?.toModel()?.benchmarkType)
    }
}
