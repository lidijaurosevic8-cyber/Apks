package com.example.ui.monitor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.FpsStressSurface
import com.example.ui.components.LivePulseIndicator
import com.example.ui.components.MetricReadout
import com.example.ui.components.RealtimeGraph
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
fun MonitorScreen(viewModel: MainViewModel) {
    val historyPoints by viewModel.historyPoints.collectAsStateWithLifecycle()
    val fpsMetrics by viewModel.fpsMetrics.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Ensure FPS meter starts while on monitor screen
    DisposableEffect(Unit) {
        viewModel.startFpsMeter()
        onDispose {
            viewModel.stopFpsMeter()
        }
    }

    val timeWindowLabel = when (settings.graphHistorySeconds) {
        10 -> "10s"
        30 -> "30s"
        60 -> "1m"
        300 -> "5m"
        else -> "${settings.graphHistorySeconds}s"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Live Telemetry & FPS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LivePulseIndicator()
                    }
                    Text(
                        text = "Real-time rolling hardware graphs & frame metrics",
                        style = MaterialTheme.typography.bodySmall,
                        color = TechTextSecondary
                    )
                }
            }
        }

        // Time Window Selection Chips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                border = BorderStroke(1.dp, TechDarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIMELINE:",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted
                    )

                    listOf(
                        10 to "10s",
                        30 to "30s",
                        60 to "1m",
                        300 to "5m"
                    ).forEach { (seconds, label) ->
                        val selected = settings.graphHistorySeconds == seconds
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setGraphHistory(seconds) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
                                selected = selected,
                                selectedBorderColor = TechCyan,
                                borderColor = TechDarkBorder
                            )
                        )
                    }
                }
            }
        }

        // FPS Monitor Mode Card
        item {
            FpsMonitorSection(
                viewModel = viewModel,
                timeWindowLabel = timeWindowLabel
            )
        }

        // CPU Usage % Graph
        item {
            val cpuPoints = historyPoints.map { it.cpuUsagePercent }
            RealtimeGraph(
                title = "CPU Utilization",
                currentValue = String.format(Locale.US, "%.1f", cpuPoints.lastOrNull() ?: 0f),
                points = cpuPoints,
                minVal = 0f,
                maxVal = 100f,
                unit = "%",
                lineColor = TechCyan,
                timeWindowLabel = timeWindowLabel
            )
        }

        // CPU Frequency Graph
        item {
            val freqPoints = historyPoints.map { it.cpuFreqMhz.toFloat() }
            RealtimeGraph(
                title = "CPU Core 0 Clock Frequency",
                currentValue = "${freqPoints.lastOrNull()?.toInt() ?: 0}",
                points = freqPoints,
                minVal = 0f,
                maxVal = (freqPoints.maxOrNull() ?: 2400f).coerceAtLeast(1000f),
                unit = "MHz",
                lineColor = TechPurple,
                timeWindowLabel = timeWindowLabel
            )
        }

        // RAM Usage % Graph
        item {
            val ramPoints = historyPoints.map { it.ramUsagePercent }
            RealtimeGraph(
                title = "RAM Memory Allocation",
                currentValue = String.format(Locale.US, "%.1f", ramPoints.lastOrNull() ?: 0f),
                points = ramPoints,
                minVal = 0f,
                maxVal = 100f,
                unit = "%",
                lineColor = TechGreen,
                timeWindowLabel = timeWindowLabel
            )
        }

        // Battery Temperature Graph
        item {
            val tempPoints = historyPoints.map { it.batteryTempCelsius }
            RealtimeGraph(
                title = "Battery Thermal Sensor",
                currentValue = String.format(Locale.US, "%.1f", tempPoints.lastOrNull() ?: 0f),
                points = tempPoints,
                minVal = 20f,
                maxVal = 50f,
                unit = "°C",
                lineColor = TechAmber,
                timeWindowLabel = timeWindowLabel
            )
        }

        // Battery Level % Graph
        item {
            val batteryPoints = historyPoints.map { it.batteryPercentage.toFloat() }
            RealtimeGraph(
                title = "Battery Charge Level",
                currentValue = "${batteryPoints.lastOrNull()?.toInt() ?: 0}",
                points = batteryPoints,
                minVal = 0f,
                maxVal = 100f,
                unit = "%",
                lineColor = Color(0xFF00E676),
                timeWindowLabel = timeWindowLabel
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FpsMonitorSection(
    viewModel: MainViewModel,
    timeWindowLabel: String
) {
    val fpsMetrics by viewModel.fpsMetrics.collectAsStateWithLifecycle()
    val historyPoints by viewModel.historyPoints.collectAsStateWithLifecycle()

    TechCard(
        title = "Display & Surface FPS Monitor",
        icon = Icons.Default.Speed,
        accentColor = TechCyan,
        actionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (fpsMetrics.isMeasuring) {
                    OutlinedButton(
                        onClick = { viewModel.stopFpsMeter() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        border = BorderStroke(0.8.dp, Color(0xFFFF5252)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pause", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Button(
                        onClick = { viewModel.startFpsMeter() },
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = TechDarkBackground),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Measure", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = { viewModel.resetFpsMeter() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechTextSecondary),
                    border = BorderStroke(0.8.dp, TechDarkBorder),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(12.dp))
                }
            }
        }
    ) {
        // Readout Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Current FPS",
                value = String.format(Locale.US, "%.1f", fpsMetrics.currentFps),
                unit = "FPS",
                accentColor = TechCyan
            )
            MetricReadout(
                label = "Average",
                value = String.format(Locale.US, "%.1f", fpsMetrics.averageFps),
                unit = "FPS",
                accentColor = TechGreen
            )
            MetricReadout(
                label = "1% Low Min",
                value = String.format(Locale.US, "%.1f", fpsMetrics.minFps),
                unit = "FPS",
                accentColor = if (fpsMetrics.minFps < 30f) Color(0xFFFF5252) else TechAmber
            )
            MetricReadout(
                label = "Peak Max",
                value = String.format(Locale.US, "%.1f", fpsMetrics.maxFps),
                unit = "FPS"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Frame Time",
                value = String.format(Locale.US, "%.2f", fpsMetrics.frameTimeMs),
                unit = "ms",
                sublabel = "Target: 16.6ms (60Hz)"
            )
            MetricReadout(
                label = "Spiked Frames",
                value = "${fpsMetrics.spikedFramesCount}",
                unit = "DROPS",
                accentColor = if (fpsMetrics.spikedFramesCount > 0) Color(0xFFFF5252) else TechGreen,
                sublabel = "> 20ms duration"
            )
            MetricReadout(
                label = "Total Sampled",
                value = "${fpsMetrics.totalFramesTracked}",
                unit = "FRAMES"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive graphics workload test surface
        Text(
            text = "ACTIVE GRAPHICS WORKLOAD VIEWPORT",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = TechTextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        FpsStressSurface(particleCount = 350, isStressing = fpsMetrics.isMeasuring)

        Spacer(modifier = Modifier.height(10.dp))

        // Frame time graph
        val frameTimePoints = historyPoints.map { it.frameTimeMs }
        RealtimeGraph(
            title = "Frame Delivery Time (ms)",
            currentValue = String.format(Locale.US, "%.2f", frameTimePoints.lastOrNull() ?: 16.6f),
            points = frameTimePoints,
            minVal = 0f,
            maxVal = 35f,
            unit = "ms",
            lineColor = TechCyan,
            height = 90.dp,
            timeWindowLabel = timeWindowLabel
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Disclaimer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TechDarkSurfaceVariant)
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TechCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Android does not provide a universal system-wide FPS API for external apps. This monitor measures real-time hardware frame presentation for active viewport surfaces.",
                    fontSize = 10.sp,
                    color = TechTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
