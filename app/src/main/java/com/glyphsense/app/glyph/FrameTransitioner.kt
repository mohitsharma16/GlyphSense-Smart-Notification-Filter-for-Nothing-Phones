package com.glyphsense.app.glyph

import kotlinx.coroutines.delay

class FrameTransitioner(
    private val durationMs: Long = 320L,
    private val steps: Int = 8
) {
    private var current: IntArray? = null

    suspend fun transitionTo(target: IntArray, push: suspend (IntArray) -> Unit) {
        val from = current
        if (from == null || from.size != target.size) {
            push(target)
            current = target.copyOf()
            return
        }
        if (from.contentEquals(target)) {
            return
        }
        val frameDelay = (durationMs / steps).coerceAtLeast(1L)
        for (step in 1..steps) {
            val t = step.toFloat() / steps
            val blended = IntArray(target.size) { idx ->
                val a = from[idx]
                val b = target[idx]
                (a + (b - a) * t).toInt().coerceIn(0, 255)
            }
            push(blended)
            current = blended
            if (step < steps) delay(frameDelay)
        }
    }

    fun reset() {
        current = null
    }
}
