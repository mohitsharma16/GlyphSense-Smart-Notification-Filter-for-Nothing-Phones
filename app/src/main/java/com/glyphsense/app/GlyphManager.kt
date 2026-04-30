package com.glyphsense.app

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*

class GlyphManager(private val context: Context) {
    private var glyphInstance: Any? = null
    private var isAvailable = false
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            val clazz = Class.forName("com.nothing.ketchum.GlyphMatrixManager")

            glyphInstance = clazz
                .getMethod("getInstance", Context::class.java)
                .invoke(null, context)

            isAvailable = true
            Log.d("GlyphManager", "Glyph SDK available")

        } catch (e: Exception) {
            isAvailable = false
            Log.d("GlyphManager", "Glyph SDK NOT available")
        }
    }

    fun isGlyphAvailable(): Boolean = isAvailable

    private fun render(frame: IntArray) {
        if (!isAvailable || glyphInstance == null) return

        try {
            val clazz = Class.forName("com.nothing.ketchum.GlyphMatrixManager")

            clazz
                .getMethod("renderFrame", IntArray::class.java)
                .invoke(glyphInstance, frame)

        } catch (e: Exception) {
            Log.e("GlyphManager", "Render failed", e)
        }
    }

    fun playAnimation(priority: PriorityLevel) {
        if (!isAvailable) return

        val ledCount = 25

        scope.launch {

            when (priority) {

                PriorityLevel.IMPORTANT -> {
                    repeat(4) {
                        render(IntArray(ledCount) { 255 })
                        delay(150)
                        render(IntArray(ledCount) { 0 })
                        delay(150)
                    }
                }

                PriorityLevel.NORMAL -> {
                    render(IntArray(ledCount) { 120 })
                    delay(400)
                    render(IntArray(ledCount) { 0 })
                }

                PriorityLevel.SILENT -> {
                    render(IntArray(ledCount) { 0 })
                }
            }
        }
    }

}
