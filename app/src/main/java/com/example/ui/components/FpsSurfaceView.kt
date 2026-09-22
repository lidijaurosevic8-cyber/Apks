package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechDarkBackground
import com.example.ui.theme.TechDarkBorder
import com.example.ui.theme.TechGreen
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TechTextMuted
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val radius: Float
)

@Composable
fun FpsStressSurface(
    modifier: Modifier = Modifier,
    particleCount: Int = 300,
    isStressing: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "surface_anim")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot"
    )

    // Pre-allocated particles
    val particles = remember(particleCount) {
        val colors = listOf(TechCyan, TechGreen, TechPurple, Color(0xFFFF5252), Color(0xFFFFD600))
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.015f,
                vy = (Random.nextFloat() - 0.5f) * 0.015f,
                color = colors[it % colors.size],
                radius = Random.nextFloat() * 3f + 1.5f
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TechDarkBackground)
            .border(1.dp, TechDarkBorder, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Add burst velocity to nearby particles on user tap
                    val tapX = offset.x / size.width
                    val tapY = offset.y / size.height
                    particles.forEach { p ->
                        val dx = p.x - tapX
                        val dy = p.y - tapY
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        if (dist < 0.25f && dist > 0.001f) {
                            p.vx += (dx / dist) * 0.03f
                            p.vy += (dy / dist) * 0.03f
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val rad = Math.toRadians(rotation.toDouble())

            // 1. Draw 3D wireframe rotating polyhedron
            val cubeVertices = listOf(
                Triple(-60f, -60f, -60f),
                Triple(60f, -60f, -60f),
                Triple(60f, 60f, -60f),
                Triple(-60f, 60f, -60f),
                Triple(-60f, -60f, 60f),
                Triple(60f, -60f, 60f),
                Triple(60f, 60f, 60f),
                Triple(-60f, 60f, 60f)
            )

            // 3D rotation transform
            val cosR = cos(rad).toFloat()
            val sinR = sin(rad).toFloat()
            val projected = cubeVertices.map { (x, y, z) ->
                // Rotate around Y and X
                val x1 = x * cosR - z * sinR
                val z1 = x * sinR + z * cosR
                val y1 = y * cosR - z1 * sinR
                val z2 = y * sinR + z1 * cosR

                val fov = 300f
                val distance = 350f
                val scale = fov / (distance + z2)
                Offset(cx + x1 * scale, cy + y1 * scale)
            }

            val edges = listOf(
                0 to 1, 1 to 2, 2 to 3, 3 to 0,
                4 to 5, 5 to 6, 6 to 7, 7 to 4,
                0 to 4, 1 to 5, 2 to 6, 3 to 7
            )

            edges.forEach { (a, b) ->
                drawLine(
                    color = TechCyan.copy(alpha = 0.7f),
                    start = projected[a],
                    end = projected[b],
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 2. Draw & simulate particle field
            val points = mutableListOf<Offset>()
            particles.forEach { p ->
                if (isStressing) {
                    p.x += p.vx
                    p.y += p.vy
                    if (p.x < 0f || p.x > 1f) p.vx = -p.vx
                    if (p.y < 0f || p.y > 1f) p.vy = -p.vy
                }
                val px = p.x * w
                val py = p.y * h
                drawCircle(
                    color = p.color,
                    radius = p.radius,
                    center = Offset(px, py)
                )
            }
        }

        Text(
            text = "TAP TO INTERACT • ${particleCount} PARTICLES",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = TechTextMuted,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
