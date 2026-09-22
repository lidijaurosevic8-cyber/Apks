package com.example.ui.dashboard

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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BatteryHealthReport
import com.example.model.BatteryMetrics
import com.example.model.CpuMetrics
import com.example.model.GpuMetrics
import com.example.model.MetricSource
import com.example.model.RamMetrics
import com.example.model.ThermalMetrics
import com.example.ui.MainViewModel
import com.example.ui.components.CircularGauge
import com.example.ui.components.LivePulseIndicator
import com.example.ui.components.MetricReadout
import com.example.ui.components.SourceBadge
import com.example.ui.components.TechCard
import com.example.ui.theme.StatusModerate
import com.example.ui.theme.StatusThrottling
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
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
    val cpu by viewModel.cpuMetrics.collectAsStateWithLifecycle()
    val gpu by viewModel.gpuMetrics.collectAsStateWithLifecycle()
    val ram by viewModel.ramMetrics.collectAsStateWithLifecycle()
    val battery by viewModel.batteryMetrics.collectAsStateWithLifecycle()
    val thermal by viewModel.thermalMetrics.collectAsStateWithLifecycle()
    val batteryHealth by viewModel.batteryHealthReport.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DashboardHeader(
                deviceModel = "${viewModel.deviceInfo.manufacturer} ${viewModel.deviceInfo.model}",
                soc = viewModel.deviceInfo.socModel,
                onOpenSettings = onOpenSettings
            )
        }

        // Quick Gauges Overview Row
        item {
            QuickGaugesRow(cpu = cpu, ram = ram, battery = battery)
        }

        // CPU Card
        item {
            CpuDashboardCard(cpu = cpu)
        }

        // GPU Card
        item {
            GpuDashboardCard(gpu = gpu)
        }

        // RAM Card
        item {
            RamDashboardCard(ram = ram)
        }

        // Battery Card
        item {
            BatteryDashboardCard(
                battery = battery,
                useFahrenheit = settings.useFahrenheit
            )
        }

        // Thermal Monitoring Card
        item {
            ThermalDashboardCard(
                thermal = thermal,
                useFahrenheit = settings.useFahrenheit
            )
        }

        // Battery Health Estimate Card
        item {
            BatteryHealthDashboardCard(
                healthReport = batteryHealth,
                onConfigureCapacity = onOpenSettings
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    deviceModel: String,
    soc: String,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = deviceModel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                LivePulseIndicator()
            }
            Text(
                text = "SoC: $soc • Real-Time Hardware Telemetry",
                style = MaterialTheme.typography.bodySmall,
                color = TechTextSecondary
            )
        }
    }
}

@Composable
private fun QuickGaugesRow(
    cpu: CpuMetrics,
    ram: RamMetrics,
    battery: BatteryMetrics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
        border = BorderStroke(1.dp, TechDarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularGauge(
                value = cpu.overallUsagePercent,
                size = 84.dp,
                title = "CPU",
                color = if (cpu.overallUsagePercent > 80f) Color(0xFFFF5252) else TechCyan
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(TechDarkBorder)
            )
            CircularGauge(
                value = ram.usagePercent,
                size = 84.dp,
                title = "RAM",
                color = if (ram.usagePercent > 85f) TechAmber else TechPurple
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(TechDarkBorder)
            )
            CircularGauge(
                value = battery.percentage.toFloat(),
                size = 84.dp,
                title = "BATTERY",
                color = if (battery.percentage < 20) Color(0xFFFF5252) else TechGreen
            )
        }
    }
}

@Composable
private fun CpuDashboardCard(cpu: CpuMetrics) {
    TechCard(
        title = "CPU Processor",
        icon = Icons.Default.Memory,
        accentColor = TechCyan,
        sourceTag = MetricSource.MEASURED
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Total Load",
                value = "${cpu.overallUsagePercent.toInt()}%",
                accentColor = TechCyan
            )
            MetricReadout(
                label = "Clock Speed",
                value = if (cpu.primaryFreqMhz > 0) "${cpu.primaryFreqMhz}" else "N/A",
                unit = if (cpu.primaryFreqMhz > 0) "MHz" else "",
                accentColor = TechCyan
            )
            MetricReadout(
                label = "Topology",
                value = "${cpu.coreCount}",
                unit = "CORES",
                sublabel = cpu.architecture
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Per-Core Utilization Breakdown
        Text(
            text = "PER-CORE ACTIVE TELEMETRY",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = TechTextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val cores = cpu.coreUsages.take(8) // Display up to 8 cores cleanly
            cores.chunked(2).forEach { rowCores ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCores.forEach { core ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TechDarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "C${core.coreIndex}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TechTextSecondary,
                                modifier = Modifier.width(22.dp)
                            )
                            LinearProgressIndicator(
                                progress = { (core.usagePercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = TechCyan,
                                trackColor = TechDarkBorder,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${core.usagePercent.toInt()}%",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TechTextPrimary
                            )
                        }
                    }
                    if (rowCores.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (cpu.availabilityNote != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Note: ${cpu.availabilityNote}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TechTextMuted
            )
        }
    }
}

@Composable
private fun GpuDashboardCard(gpu: GpuMetrics) {
    TechCard(
        title = "GPU Graphics",
        icon = Icons.Default.VideogameAsset,
        accentColor = TechPurple,
        sourceTag = if (gpu.isUtilizationSupported) MetricSource.MEASURED else MetricSource.UNAVAILABLE
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Renderer",
                value = gpu.renderer.take(14),
                sublabel = gpu.vendor,
                accentColor = TechPurple
            )
            MetricReadout(
                label = "Utilization",
                value = if (gpu.utilizationPercent != null) "${gpu.utilizationPercent.toInt()}%" else "Unavailable",
                accentColor = TechPurple
            )
            MetricReadout(
                label = "Frequency",
                value = if (gpu.frequencyMhz != null) "${gpu.frequencyMhz}" else "Unavailable",
                unit = if (gpu.frequencyMhz != null) "MHz" else "",
                accentColor = TechPurple
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Prominent accuracy callout
        if (!gpu.isUtilizationSupported) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TechDarkSurfaceVariant)
                    .border(1.dp, TechDarkBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TechAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GPU live load is not exposed by this device's Android SELinux policy. Stats are never fabricated.",
                        fontSize = 11.sp,
                        color = TechTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RamDashboardCard(ram: RamMetrics) {
    TechCard(
        title = "RAM Memory",
        icon = Icons.Default.Memory,
        accentColor = TechCyan,
        sourceTag = MetricSource.SYSTEM_REPORTED
    ) {
        val totalGb = ram.totalBytes / (1024f * 1024f * 1024f)
        val usedGb = ram.usedBytes / (1024f * 1024f * 1024f)
        val availGb = ram.availableBytes / (1024f * 1024f * 1024f)
        val appMb = ram.appUsedBytes / (1024f * 1024f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Total RAM",
                value = String.format(Locale.US, "%.1f", totalGb),
                unit = "GB",
                accentColor = TechCyan
            )
            MetricReadout(
                label = "In Use",
                value = String.format(Locale.US, "%.1f", usedGb),
                unit = "GB",
                sublabel = "${ram.usagePercent.toInt()}% Allocated"
            )
            MetricReadout(
                label = "Available",
                value = String.format(Locale.US, "%.1f", availGb),
                unit = "GB",
                accentColor = TechGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { (ram.usagePercent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (ram.usagePercent > 85f) TechAmber else TechCyan,
            trackColor = TechDarkSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "APP PROCESS: ${appMb.toInt()} MB",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TechTextMuted
            )
            Text(
                text = if (ram.isLowMemory) "LOW MEMORY WARNING" else "MEMORY HEALTH: NORMAL",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (ram.isLowMemory) Color(0xFFFF5252) else TechGreen
            )
        }
    }
}

@Composable
private fun BatteryDashboardCard(
    battery: BatteryMetrics,
    useFahrenheit: Boolean
) {
    TechCard(
        title = "Battery & Power",
        icon = Icons.Default.BatteryChargingFull,
        accentColor = TechGreen,
        sourceTag = MetricSource.MEASURED
    ) {
        val tempDisplay = if (useFahrenheit) {
            val f = (battery.temperatureCelsius * 9f / 5f) + 32f
            String.format(Locale.US, "%.1f°F", f)
        } else {
            String.format(Locale.US, "%.1f°C", battery.temperatureCelsius)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Level",
                value = "${battery.percentage}%",
                sublabel = battery.status,
                accentColor = TechGreen
            )
            MetricReadout(
                label = "Temperature",
                value = tempDisplay,
                accentColor = if (battery.temperatureCelsius > 42f) Color(0xFFFF5252) else TechGreen
            )
            MetricReadout(
                label = "Voltage",
                value = "${battery.voltageMilliVolts}",
                unit = "mV"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Current Rate",
                value = if (battery.currentAmperes != null) "${battery.currentAmperes.toInt()}" else "N/A",
                unit = if (battery.currentAmperes != null) "mA" else "",
                sublabel = if (battery.isCharging) "Charging flow" else "Discharge rate"
            )
            MetricReadout(
                label = "Power Draw",
                value = if (battery.powerWatts != null) String.format(Locale.US, "%.2f", battery.powerWatts) else "N/A",
                unit = if (battery.powerWatts != null) "W" else ""
            )
            MetricReadout(
                label = "Health",
                value = battery.health.uppercase(),
                accentColor = TechGreen
            )
        }
    }
}

@Composable
private fun ThermalDashboardCard(
    thermal: ThermalMetrics,
    useFahrenheit: Boolean
) {
    val stateColor = when {
        thermal.isThrottling -> StatusThrottling
        thermal.batteryTempCelsius > 39f -> StatusModerate
        else -> TechGreen
    }

    TechCard(
        title = "Thermal Monitor",
        icon = Icons.Default.Thermostat,
        accentColor = stateColor,
        sourceTag = MetricSource.MEASURED
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricReadout(
                label = "Battery Sensor",
                value = if (useFahrenheit) {
                    val f = (thermal.batteryTempCelsius * 9f / 5f) + 32f
                    String.format(Locale.US, "%.1f°F", f)
                } else {
                    String.format(Locale.US, "%.1f°C", thermal.batteryTempCelsius)
                },
                accentColor = stateColor
            )

            MetricReadout(
                label = "SoC / Thermal Zones",
                value = if (thermal.cpuTempCelsius != null) {
                    if (useFahrenheit) {
                        val f = (thermal.cpuTempCelsius * 9f / 5f) + 32f
                        String.format(Locale.US, "%.1f°F", f)
                    } else {
                        String.format(Locale.US, "%.1f°C", thermal.cpuTempCelsius)
                    }
                } else "N/A",
                sublabel = "${thermal.thermalZonesCount} zones found"
            )

            MetricReadout(
                label = "Thermal Status",
                value = thermal.thermalState.name,
                accentColor = stateColor
            )
        }

        if (thermal.throttlingWarning != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x22FF5252))
                    .border(1.dp, Color(0x66FF5252), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = thermal.throttlingWarning,
                        fontSize = 11.sp,
                        color = Color(0xFFFF8A80)
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryHealthDashboardCard(
    healthReport: BatteryHealthReport?,
    onConfigureCapacity: () -> Unit
) {
    if (healthReport == null) return

    TechCard(
        title = "Battery Health Estimate",
        icon = Icons.Default.BatteryChargingFull,
        accentColor = TechGreen,
        sourceTag = MetricSource.ESTIMATED,
        actionButton = {
            OutlinedButton(
                onClick = onConfigureCapacity,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                border = BorderStroke(0.8.dp, TechDarkBorder),
                modifier = Modifier.height(28.dp)
            ) {
                Text(text = "Config", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ESTIMATED HEALTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechTextMuted
                )
                Text(
                    text = "${healthReport.estimatedHealthPercent}%",
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TechGreen
                )
                Text(
                    text = "Status: ${healthReport.healthStatusTag}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "DESIGN CAPACITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechTextMuted
                )
                Text(
                    text = "${healthReport.designCapacityMah} mAh",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
                Text(
                    text = "Estimated: ~${healthReport.estimatedCurrentCapacityMah} mAh",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextSecondary
                )
                if (healthReport.cycleCount != null) {
                    Text(
                        text = "Cycles: ${healthReport.cycleCount}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TechDarkSurfaceVariant)
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "Methodology: ${healthReport.calculationMethod}",
                    fontSize = 11.sp,
                    color = TechTextSecondary,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = healthReport.accuracyNote,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextMuted,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
