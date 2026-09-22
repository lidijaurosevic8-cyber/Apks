package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechTextMuted
import com.example.ui.theme.TechTextPrimary
import java.util.Locale
import kotlin.math.max

@Composable
fun RealtimeGraph(
    title: String,
    currentValue: String,
    points: List<Float>,
    minVal: Float = 0f,
    maxVal: Float = 100f,
    unit: String = "%",
    lineColor: Color = TechCyan,
    height: Dp = 120.dp,
    timeWindowLabel: String = "30s",
    modifier: Modifier = Modifier
) {
    // Dynamic max if data exceeds maxVal
    val effectiveMax = remember(points, maxVal) {
        val observedMax = points.maxOrNull() ?: maxVal
        max(maxVal, observedMax * 1.05f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TechDarkBackground.copy(alpha = 0.6f))
            .border(1.dp, TechDarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TechTextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$currentValue $unit",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TechDarkBorder.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "WINDOW: $timeWindowLabel",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TechTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canvas Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                val w = size.width
                val h = size.height
                val range = (effectiveMax - minVal).coerceAtLeast(1f)

                // Draw horizontal grid guide lines (25%, 50%, 75%)
                val gridLines = 3
                for (i in 1..gridLines) {
                    val y = h * (i.toFloat() / (gridLines + 1))
                    drawLine(
                        color = TechDarkBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                if (points.size >= 2) {
                    val stepX = w / (points.size - 1).coerceAtLeast(1)
                    val strokePath = Path()
                    val fillPath = Path()

                    points.forEachIndexed { i, value ->
                        val normalized = ((value - minVal) / range).coerceIn(0f, 1f)
                        val x = i * stepX
                        val y = h - (normalized * h)

                        if (i == 0) {
                            strokePath.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            strokePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Gradient fill underneath curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.25f),
                                lineColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Curve stroke
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Dot at latest value
                    val lastVal = points.last()
                    val lastNorm = ((lastVal - minVal) / range).coerceIn(0f, 1f)
                    val lastX = w
                    val lastY = h - (lastNorm * h)
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                }
            }

            // Min and Max Legend
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.0f", effectiveMax),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextMuted
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = String.format(Locale.US, "%.0f", minVal),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TechTextMuted
                )
            }
        }
    }
}
