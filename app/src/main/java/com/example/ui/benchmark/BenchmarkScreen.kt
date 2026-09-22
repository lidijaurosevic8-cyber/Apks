package com.example.ui.benchmark

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExportHelper
import com.example.model.BenchmarkRunResult
import com.example.model.BenchmarkType
import com.example.ui.BenchmarkUiState
import com.example.ui.MainViewModel
import com.example.ui.components.FpsStressSurface
import com.example.ui.components.MetricReadout
import com.example.ui.components.TechCard
import com.example.ui.theme.TechAmber
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechDarkSurfaceVariant
import com.example.ui.theme.TechGreen
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextPrimary
import com.example.ui.theme.TechTextSecondary
import java.util.Locale

@Composable
fun BenchmarkScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit
) {
    val benchmarkState by viewModel.benchmarkState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(BenchmarkType.COMBINED) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Column {
                Text(
                    text = "Hardware Benchmark Suite",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
                Text(
                    text = "Stress testing CPU compute, graphics pipeline & thermal stability",
                    style = MaterialTheme.typography.bodySmall,
                    color = TechTextSecondary
                )
            }
        }

        // Active State Display
        when (val state = benchmarkState) {
            is BenchmarkUiState.Running -> {
                item {
                    BenchmarkRunningCard(state = state)
                }
            }

            is BenchmarkUiState.Completed -> {
                item {
                    BenchmarkCompletedCard(
                        result = state.result,
                        onReset = { viewModel.resetBenchmarkState() },
                        onShare = {
                            val text = ExportHelper.formatTextReport(state.result, viewModel.deviceInfo)
                            ExportHelper.shareText(context, text, "Share Benchmark Report")
                        },
                        onViewHistory = onNavigateToHistory
                    )
                }
            }

            is BenchmarkUiState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x22FF5252)),
                        border = BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Benchmark Error", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                            Text(state.message, color = TechTextPrimary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.resetBenchmarkState() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }

            is BenchmarkUiState.Idle -> {
                // Setup & Mode Selection
                item {
                    BenchmarkConfigCard(
                        selectedType = selectedType,
                        onSelectType = { selectedType = it },
                        durationSeconds = settings.benchmarkDurationSeconds,
                        onSelectDuration = { viewModel.setBenchmarkDuration(it) },
                        onStart = { viewModel.runBenchmark(selectedType) }
                    )
                }

                // Information cards about each test
                item {
                    BenchmarkInfoList()
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BenchmarkConfigCard(
    selectedType: BenchmarkType,
    onSelectType: (BenchmarkType) -> Unit,
    durationSeconds: Int,
    onSelectDuration: (Int) -> Unit,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
        border = BorderStroke(1.dp, TechDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SELECT WORKLOAD PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = TechTextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Workload Types Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    BenchmarkType.COMBINED to ("Combined" to Icons.Default.Bolt),
                    BenchmarkType.CPU to ("CPU Stress" to Icons.Default.Memory),
                    BenchmarkType.GPU to ("GPU Graphics" to Icons.Default.VideogameAsset)
                ).forEach { (type, pair) ->
                    val (label, icon) = pair
                    val isSelected = selectedType == type
                    Card(
                        onClick = { onSelectType(type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TechCyan.copy(alpha = 0.15f) else TechDarkSurfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) TechCyan else TechDarkBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) TechCyan else TechTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TechCyan else TechTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Duration selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DURATION:",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15).forEach { sec ->
                        val isSelected = durationSeconds == sec
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectDuration(sec) },
                            label = {
                                Text(
                                    text = "${sec}s",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TechCyan.copy(alpha = 0.2f),
                                selectedLabelColor = TechCyan,
                                containerColor = TechDarkSurfaceVariant,
                                labelColor = TechTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = TechCyan,
                                borderColor = TechDarkBorder
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start Button
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechCyan,
                    contentColor = TechDarkBackground
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXECUTE BENCHMARK",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun BenchmarkRunningCard(state: BenchmarkUiState.Running) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
        border = BorderStroke(1.dp, TechCyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RUNNING ${state.type.name} BENCHMARK",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TechCyan
                )
                Text(
                    text = "${(state.progress * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = TechCyan,
                trackColor = TechDarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time animation viewport during benchmark
            FpsStressSurface(particleCount = 500, isStressing = true)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.statusMessage,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TechTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun BenchmarkCompletedCard(
    result: BenchmarkRunResult,
    onReset: () -> Unit,
    onShare: () -> Unit,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
        border = BorderStroke(1.dp, TechGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TechGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BENCHMARK COMPLETED",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TechGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TechGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${result.durationSeconds}s TEST",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Highlight Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TechDarkBackground)
                    .border(1.dp, TechDarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PERFORMANCE SCORE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.combinedScore}",
                        fontSize = 44.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TechCyan
                    )
                    Text(
                        text = "${result.deviceModel} • ${result.benchmarkType.name}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed telemetry before/after
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricReadout(
                    label = "CPU Score",
                    value = "${result.cpuScore}",
                    unit = "PTS",
                    accentColor = TechCyan
                )
                MetricReadout(
                    label = "GPU Score",
                    value = "${result.gpuScore}",
                    unit = "PTS",
                    accentColor = TechPurple
                )
                MetricReadout(
                    label = "Average FPS",
                    value = String.format(Locale.US, "%.1f", result.averageFps),
                    unit = "FPS",
                    accentColor = TechGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricReadout(
                    label = "Thermal Range",
                    value = String.format(Locale.US, "%.1f°C", result.maxTempCelsius),
                    sublabel = "+${String.format(Locale.US, "%.1f", result.tempDeltaCelsius)}°C rise"
                )
                MetricReadout(
                    label = "Battery Delta",
                    value = "-${result.batteryDeltaPercent}%",
                    sublabel = "${result.startBatteryPercent}% -> ${result.endBatteryPercent}%"
                )
                MetricReadout(
                    label = "Throttling",
                    value = if (result.throttlingDetected) "DETECTED" else "OPTIMAL",
                    accentColor = if (result.throttlingDetected) Color(0xFFFF5252) else TechGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = TechDarkBackground)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechTextPrimary),
                    border = BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Text("History", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechTextSecondary),
                    border = BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun BenchmarkInfoList() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TechCard(
            title = "CPU Benchmark Architecture",
            icon = Icons.Default.Memory,
            accentColor = TechCyan
        ) {
            Text(
                text = "Runs synchronized multi-threaded workloads across all active CPU cores. Executes Prime Sieve loops, SHA-256 cryptographic hashes, and 32x32 matrix transformations to evaluate integer, floating point, and memory cache throughput.",
                fontSize = 12.sp,
                color = TechTextSecondary,
                lineHeight = 16.sp
            )
        }

        TechCard(
            title = "GPU Benchmark Architecture",
            icon = Icons.Default.VideogameAsset,
            accentColor = TechPurple
        ) {
            Text(
                text = "Renders heavy real-time particle dynamics, 3D rotating wireframe meshes with projection matrices, and dynamic blend shaders on active hardware surfaces. Tracks frame time stability, average FPS, and 1% low drops.",
                fontSize = 12.sp,
                color = TechTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
