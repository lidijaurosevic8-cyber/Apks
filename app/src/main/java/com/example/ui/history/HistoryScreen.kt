package com.example.ui.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.MainViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val runs by viewModel.savedBenchmarkRuns.collectAsStateWithLifecycle()
    val comparisonIds by viewModel.comparisonRunIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf<BenchmarkType?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var selectedDetailRun by remember { mutableStateOf<BenchmarkRunResult?>(null) }

    val filteredRuns = runs.filter {
        selectedFilter == null || it.benchmarkType == selectedFilter
    }

    val comparisonRuns = runs.filter { comparisonIds.contains(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechDarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Benchmark History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                    Text(
                        text = "${runs.size} recorded test sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TechTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (runs.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showExportDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                            border = BorderStroke(1.dp, TechDarkBorder),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            border = BorderStroke(1.dp, TechDarkBorder),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Comparison Bar Section (if 2 or more selected)
        if (comparisonRuns.size >= 2) {
            item {
                ComparisonOverviewCard(comparisonRuns = comparisonRuns)
            }
        }

        // Filters row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf<Pair<String, BenchmarkType?>>(
                    "All (${runs.size})" to null,
                    "Combined" to BenchmarkType.COMBINED,
                    "CPU" to BenchmarkType.CPU,
                    "GPU" to BenchmarkType.GPU
                ).forEach { (label, type) ->
                    val isSelected = selectedFilter == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = type },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
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

        if (filteredRuns.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
                    border = BorderStroke(1.dp, TechDarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO SAVED BENCHMARK RUNS",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TechTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Execute a CPU, GPU, or Combined benchmark from the Benchmark tab to record performance metrics.",
                            fontSize = 11.sp,
                            color = TechTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        } else {
            items(filteredRuns, key = { it.id }) { run ->
                HistoryRunItem(
                    run = run,
                    isCompared = comparisonIds.contains(run.id),
                    onToggleCompare = { viewModel.toggleComparison(run.id) },
                    onDelete = { viewModel.deleteBenchmarkRun(run.id) },
                    onClick = { selectedDetailRun = run }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = TechDarkSurface,
            title = {
                Text("Export Benchmark Data", color = TechTextPrimary, fontFamily = FontFamily.Monospace)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select format to export or share your benchmark history:", color = TechTextSecondary, fontSize = 12.sp)
                    Button(
                        onClick = {
                            val csv = ExportHelper.formatToCsv(runs)
                            ExportHelper.shareText(context, csv, "Export Benchmarks CSV")
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = TechDarkBackground)
                    ) {
                        Text("Share as CSV", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val json = ExportHelper.formatToJson(runs, viewModel.deviceInfo)
                            ExportHelper.shareText(context, json, "Export Benchmarks JSON")
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TechPurple, contentColor = Color.White)
                    ) {
                        Text("Share as JSON", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = TechTextMuted)
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = TechDarkSurface,
            title = { Text("Clear All History?", color = TechTextPrimary) },
            text = { Text("This will permanently delete all saved benchmark records.", color = TechTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TechTextMuted)
                }
            }
        )
    }

    // Details Modal Dialog
    selectedDetailRun?.let { run ->
        AlertDialog(
            onDismissRequest = { selectedDetailRun = null },
            containerColor = TechDarkSurface,
            title = {
                Text("${run.benchmarkType.name} Benchmark Run", color = TechCyan, fontFamily = FontFamily.Monospace)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = ExportHelper.formatTextReport(run, viewModel.deviceInfo),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TechTextSecondary,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val report = ExportHelper.formatTextReport(run, viewModel.deviceInfo)
                        ExportHelper.shareText(context, report, "Share Report")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = TechDarkBackground)
                ) {
                    Text("Share Report", fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDetailRun = null }) {
                    Text("Close", color = TechTextMuted)
                }
            }
        )
    }
}

@Composable
private fun HistoryRunItem(
    run: BenchmarkRunResult,
    isCompared: Boolean,
    onToggleCompare: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.US) }
    val dateStr = remember(run.timestamp) { sdf.format(Date(run.timestamp)) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TechDarkSurface),
        border = BorderStroke(1.dp, if (isCompared) TechCyan else TechDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (run.benchmarkType) {
                                    BenchmarkType.CPU -> TechCyan.copy(alpha = 0.2f)
                                    BenchmarkType.GPU -> TechPurple.copy(alpha = 0.2f)
                                    BenchmarkType.COMBINED -> TechGreen.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = run.benchmarkType.name,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when (run.benchmarkType) {
                                BenchmarkType.CPU -> TechCyan
                                BenchmarkType.GPU -> TechPurple
                                BenchmarkType.COMBINED -> TechGreen
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isCompared,
                        onCheckedChange = { onToggleCompare() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TechCyan,
                            uncheckedColor = TechDarkBorder,
                            checkmarkColor = TechDarkBackground
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TechTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCORE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted
                    )
                    Text(
                        text = "${run.combinedScore}",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TechCyan
                    )
                }

                MetricReadout(
                    label = "Avg FPS",
                    value = if (run.averageFps > 0) String.format(Locale.US, "%.1f", run.averageFps) else "--",
                    unit = if (run.averageFps > 0) "FPS" else ""
                )

                MetricReadout(
                    label = "Max Temp",
                    value = String.format(Locale.US, "%.1f°C", run.maxTempCelsius),
                    sublabel = "+${String.format(Locale.US, "%.1f", run.tempDeltaCelsius)}°C"
                )

                MetricReadout(
                    label = "Battery",
                    value = "-${run.batteryDeltaPercent}%",
                    accentColor = if (run.throttlingDetected) Color(0xFFFF5252) else TechGreen,
                    sublabel = if (run.throttlingDetected) "Throttled" else "Normal"
                )
            }
        }
    }
}

@Composable
private fun ComparisonOverviewCard(comparisonRuns: List<BenchmarkRunResult>) {
    TechCard(
        title = "Side-by-Side Comparison (${comparisonRuns.size} selected)",
        icon = Icons.Default.Compare,
        accentColor = TechCyan
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val maxScore = (comparisonRuns.maxOfOrNull { it.combinedScore } ?: 1000).coerceAtLeast(1)

            comparisonRuns.forEachIndexed { index, run ->
                val ratio = (run.combinedScore.toFloat() / maxScore.toFloat()).coerceIn(0.05f, 1f)
                val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.US)
                val dateLabel = sdf.format(Date(run.timestamp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "#${index + 1} [${run.benchmarkType.name}] $dateLabel",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TechTextSecondary
                        )
                        Text(
                            text = "${run.combinedScore} PTS • ${String.format(Locale.US, "%.1f", run.maxTempCelsius)}°C",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(TechDarkSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (index == 0) TechCyan else TechPurple)
                        )
                    }
                }
            }
        }
    }
}
