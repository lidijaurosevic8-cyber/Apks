package com.example.ui.device

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceInfo
import com.example.model.MetricSource
import com.example.ui.MainViewModel
import com.example.ui.components.TechCard
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechGreen
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextPrimary
import com.example.ui.theme.TechTextSecondary
import java.util.Locale

@Composable
fun DeviceScreen(viewModel: MainViewModel) {
    val device = viewModel.deviceInfo

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
                    text = "Device Specifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
                Text(
                    text = "Complete hardware architecture, system and subsystem inventory",
                    style = MaterialTheme.typography.bodySmall,
                    color = TechTextSecondary
                )
            }
        }

        // System & Identity
        item {
            TechCard(
                title = "System Identity & Firmware",
                icon = Icons.Default.PhoneAndroid,
                accentColor = TechCyan,
                sourceTag = MetricSource.SYSTEM_REPORTED
            ) {
                SpecGrid(
                    listOf(
                        "Manufacturer" to device.manufacturer,
                        "Model" to device.model,
                        "Device Code" to device.deviceName,
                        "Motherboard / Board" to device.board,
                        "Hardware Platform" to device.hardware,
                        "Android OS" to "Android ${device.androidVersion} (API ${device.sdkVersion})",
                        "Security Patch" to device.securityPatch,
                        "OS Build" to device.buildId,
                        "Kernel Version" to device.kernelVersion
                    )
                )
            }
        }

        // Processor & SoC
        item {
            TechCard(
                title = "Processor & System-on-Chip",
                icon = Icons.Default.Memory,
                accentColor = TechCyan,
                sourceTag = MetricSource.SYSTEM_REPORTED
            ) {
                SpecGrid(
                    listOf(
                        "SoC / Platform" to device.socModel,
                        "Instruction Architecture" to device.cpuArchitecture,
                        "Physical / Logical Cores" to "${device.cpuCores} Cores",
                        "Primary ABI" to (device.supportedAbis.firstOrNull() ?: "Unknown"),
                        "Supported ABIs" to device.supportedAbis.joinToString(", ")
                    )
                )
            }
        }

        // Graphics & Display
        item {
            TechCard(
                title = "Graphics Subsystem & Display",
                icon = Icons.Default.VideogameAsset,
                accentColor = TechPurple,
                sourceTag = MetricSource.SYSTEM_REPORTED
            ) {
                SpecGrid(
                    listOf(
                        "GPU Renderer" to device.gpuRenderer,
                        "GPU Vendor" to device.gpuVendor,
                        "OpenGL ES Version" to device.openGlVersion,
                        "Display Resolution" to device.displayResolution,
                        "Screen Refresh Rate" to "${String.format(Locale.US, "%.1f", device.refreshRateHz)} Hz",
                        "Supported Modes" to device.supportedRefreshRates.joinToString(" Hz, ") { String.format(Locale.US, "%.0f", it) } + " Hz",
                        "Pixel Density" to "${device.screenDensityDpi} DPI",
                        "Physical Diagonal" to "~${device.screenDiagonalInches} inches"
                    )
                )
            }
        }

        // Memory & Storage
        item {
            val totalRamGb = device.totalRamBytes / (1024.0 * 1024.0 * 1024.0)
            val totalStorageGb = device.totalInternalStorageBytes / (1024.0 * 1024.0 * 1024.0)
            val freeStorageGb = device.freeInternalStorageBytes / (1024.0 * 1024.0 * 1024.0)
            val usedStorageGb = totalStorageGb - freeStorageGb

            TechCard(
                title = "Storage & Memory Subsystems",
                icon = Icons.Default.Storage,
                accentColor = TechGreen,
                sourceTag = MetricSource.SYSTEM_REPORTED
            ) {
                SpecGrid(
                    listOf(
                        "Installed RAM" to String.format(Locale.US, "%.2f GB", totalRamGb),
                        "Total Internal Storage" to String.format(Locale.US, "%.2f GB", totalStorageGb),
                        "Available Free Storage" to String.format(Locale.US, "%.2f GB", freeStorageGb),
                        "Allocated Storage" to String.format(Locale.US, "%.2f GB", usedStorageGb)
                    )
                )
            }
        }

        // Power & Battery Design
        item {
            TechCard(
                title = "Power & Battery Subsystem",
                icon = Icons.Default.Thermostat,
                accentColor = TechGreen,
                sourceTag = MetricSource.MEASURED
            ) {
                SpecGrid(
                    listOf(
                        "Battery Design Profile" to "${device.batteryDesignCapacityMah} mAh",
                        "Hardware Sensors Count" to "${device.sensors.size} sensors active"
                    )
                )
            }
        }

        // Hardware Sensor Manifest
        item {
            TechCard(
                title = "Hardware Sensor Inventory (${device.sensors.size})",
                icon = Icons.Default.Sensors,
                accentColor = TechCyan,
                sourceTag = MetricSource.SYSTEM_REPORTED
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    device.sensors.take(15).forEach { sensor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sensor.name,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TechTextPrimary
                                )
                                Text(
                                    text = "${sensor.vendor} • ${sensor.typeName}",
                                    fontSize = 10.sp,
                                    color = TechTextMuted
                                )
                            }
                            Text(
                                text = "${sensor.powerMa} mA",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TechCyan
                            )
                        }
                    }
                    if (device.sensors.size > 15) {
                        Text(
                            text = "+ ${device.sensors.size - 15} additional hardware sensors detected",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TechTextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SpecGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextMuted,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = TechTextPrimary,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}
