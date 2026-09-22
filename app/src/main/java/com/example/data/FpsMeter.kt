package com.example.data

import android.view.Choreographer
import com.example.model.FpsMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

class FpsMeter {

    private val _fpsMetrics = MutableStateFlow(FpsMetrics())
    val fpsMetrics: StateFlow<FpsMetrics> = _fpsMetrics.asStateFlow()

    private var isRunning = false
    private var lastFrameTimeNanos: Long = 0
    private var frameCount = 0
    private var fpsSum = 0f
    private var minFps = 240f
    private var maxFps = 0f
    private var spikedFrames = 0
    private var totalFrames = 0

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            if (lastFrameTimeNanos > 0) {
                val deltaNanos = frameTimeNanos - lastFrameTimeNanos
                if (deltaNanos > 0) {
                    val frameTimeMs = deltaNanos / 1_000_000f
                    val instantFps = (1_000_000_000.0 / deltaNanos).toFloat().coerceIn(1f, 240f)

                    frameCount++
                    totalFrames++
                    fpsSum += instantFps
                    minFps = min(minFps, instantFps)
                    maxFps = max(maxFps, instantFps)

                    // Spiked frame if frameTime > 20ms (> 50fps drop)
                    if (frameTimeMs > 20.0f) {
                        spikedFrames++
                    }

                    val avgFps = fpsSum / frameCount

                    _fpsMetrics.value = FpsMetrics(
                        currentFps = instantFps,
                        averageFps = avgFps,
                        minFps = if (minFps > 200f) instantFps else minFps,
                        maxFps = maxFps,
                        frameTimeMs = frameTimeMs,
                        spikedFramesCount = spikedFrames,
                        totalFramesTracked = totalFrames,
                        isMeasuring = true
                    )
                }
            }
            lastFrameTimeNanos = frameTimeNanos

            if (isRunning) {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        lastFrameTimeNanos = 0
        frameCount = 0
        fpsSum = 0f
        minFps = 240f
        maxFps = 0f
        spikedFrames = 0
        totalFrames = 0
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        _fpsMetrics.value = _fpsMetrics.value.copy(isMeasuring = false)
    }

    fun reset() {
        stop()
        _fpsMetrics.value = FpsMetrics()
    }
}
