package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard),
    MONITOR("monitor", "Monitor", Icons.Default.Timeline),
    BENCHMARK("benchmark", "Benchmark", Icons.Default.Bolt),
    HISTORY("history", "History", Icons.Default.History),
    DEVICE("device", "Device", Icons.Default.Info)
}
