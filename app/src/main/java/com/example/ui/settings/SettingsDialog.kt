package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechDarkSurface
import com.example.ui.theme.TechDarkSurfaceVariant
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextPrimary
import com.example.ui.theme.TechTextSecondary

@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var capacityInput by remember(settings.designBatteryCapacityMah) {
        mutableStateOf(settings.designBatteryCapacityMah.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TechDarkSurface,
        title = {
            Text(
                text = "Preferences & Diagnostics",
                color = TechTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Refresh Rate
                Column {
                    Text(
                        text = "METRIC REFRESH RATE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            500L to "0.5s",
                            1000L to "1.0s",
                            2000L to "2.0s"
                        ).forEach { (ms, label) ->
                            val isSelected = settings.refreshRateMs == ms
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setRefreshRate(ms) },
                                label = { Text(label, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
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

                // Temperature Unit
                Column {
                    Text(
                        text = "TEMPERATURE UNIT",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            false to "Celsius (°C)",
                            true to "Fahrenheit (°F)"
                        ).forEach { (useF, label) ->
                            val isSelected = settings.useFahrenheit == useF
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setTemperatureUnit(useF) },
                                label = { Text(label, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
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

                // Background Monitoring Notification Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = TechDarkSurfaceVariant),
                    border = BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Monitoring",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TechTextPrimary
                            )
                            Text(
                                text = "Persistent notification with real-time CPU, RAM, Battery & Temp",
                                fontSize = 10.sp,
                                color = TechTextSecondary
                            )
                        }
                        Switch(
                            checked = settings.backgroundMonitoringEnabled,
                            onCheckedChange = { viewModel.toggleBackgroundMonitoring(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TechCyan,
                                checkedTrackColor = TechCyan.copy(alpha = 0.3f),
                                uncheckedTrackColor = TechDarkBackground
                            )
                        )
                    }
                }

                // Battery Design Capacity Manual Entry
                Column {
                    Text(
                        text = "DESIGN BATTERY CAPACITY",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted
                    )
                    Text(
                        text = "Used for health calculations when not provided by kernel",
                        fontSize = 10.sp,
                        color = TechTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = capacityInput,
                            onValueChange = { capacityInput = it.filter { char -> char.isDigit() } },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = TechDarkSurfaceVariant,
                                unfocusedContainerColor = TechDarkSurfaceVariant,
                                focusedTextColor = TechTextPrimary,
                                unfocusedTextColor = TechTextPrimary,
                                focusedIndicatorColor = TechCyan,
                                unfocusedIndicatorColor = TechDarkBorder
                            ),
                            singleLine = true,
                            suffix = { Text("mAh", color = TechCyan, fontFamily = FontFamily.Monospace) }
                        )

                        Button(
                            onClick = {
                                val mah = capacityInput.toIntOrNull()
                                if (mah != null && mah in 1000..15000) {
                                    viewModel.setDesignBatteryCapacity(mah)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = TechDarkBackground)
                        ) {
                            Text("Save", fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = TechDarkBackground)
            ) {
                Text("Done", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    )
}
