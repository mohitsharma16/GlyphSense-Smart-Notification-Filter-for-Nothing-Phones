package com.glyphsense.app.glyph

import android.content.Context
import android.graphics.Bitmap
import com.glyphsense.app.domain.DeviceProfile
import com.glyphsense.app.domain.PendingNotification
import com.glyphsense.app.domain.PriorityLevel
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixObject
import com.nothing.ketchum.GlyphMatrixUtils

class FrameRenderer(private val context: Context) {

    fun render(view: ToyView, profile: DeviceProfile): IntArray = when (view) {
        is ToyView.Empty -> renderEmpty(profile)
        is ToyView.AppIcon -> renderAppIcon(view, profile)
        is ToyView.Pile -> renderPile(view.notifications, profile)
    }

    private fun renderEmpty(profile: DeviceProfile): IntArray {
        val side = profile.matrixSide
        val frame = IntArray(side * side)
        val center = side / 2
        val radius = if (side >= 25) 2 else 1
        val brightness = 60
        for (dy in -radius..radius) {
            for (dx in -radius..radius) {
                if (dx * dx + dy * dy <= radius * radius) {
                    val x = center + dx
                    val y = center + dy
                    if (x in 0 until side && y in 0 until side) {
                        frame[y * side + x] = brightness
                    }
                }
            }
        }
        return frame
    }

    private fun renderAppIcon(view: ToyView.AppIcon, profile: DeviceProfile): IntArray {
        AppGlyphs.glyphFor(view.packageName, profile.matrixSide)?.let { hand ->
            return runCatching {
                GlyphMatrixFrame.Builder()
                    .addTop(hand)
                    .build(context)
                    .render()
            }.getOrDefault(hand)
        }

        val drawable = runCatching {
            context.packageManager.getApplicationIcon(view.packageName)
        }.getOrNull() ?: return renderFallbackBadge(profile)

        val raw = GlyphMatrixUtils.drawableToBitmap(drawable) ?: return renderFallbackBadge(profile)
        val sized = Bitmap.createScaledBitmap(raw, profile.matrixSide, profile.matrixSide, true)

        val obj = GlyphMatrixObject.Builder()
            .setImageSource(sized)
            .setScale(100)
            .setPosition(0, 0)
            .setBrightness(255)
            .build()

        return runCatching {
            GlyphMatrixFrame.Builder()
                .addTop(obj)
                .build(context)
                .render()
        }.getOrElse { renderFallbackBadge(profile) }
    }

    private fun renderFallbackBadge(profile: DeviceProfile): IntArray {
        val side = profile.matrixSide
        val frame = IntArray(side * side)
        val border = if (side >= 25) 4 else 2
        for (y in border until side - border) {
            for (x in border until side - border) {
                val edgeDist = minOf(x - border, side - 1 - border - x, y - border, side - 1 - border - y)
                if (edgeDist == 0) frame[y * side + x] = 220
            }
        }
        return frame
    }

    private fun renderPile(
        notifications: List<PendingNotification>,
        profile: DeviceProfile
    ): IntArray {
        val side = profile.matrixSide
        val frame = IntArray(side * side)
        if (notifications.isEmpty()) return renderEmpty(profile)

        val brickWidth = if (side >= 25) 3 else 1
        val brickHeight = if (side >= 25) 2 else 1
        val gap = 1
        val stride = brickWidth + gap
        val bricksPerRow = ((side + gap) / stride).coerceAtLeast(1)

        val sorted = notifications.sortedWith(
            compareBy(
                { priorityRank(it.priority) },
                { -it.postedAt }
            )
        )

        sorted.forEachIndexed { index, notif ->
            val brickCol = index % bricksPerRow
            val brickRow = index / bricksPerRow

            val xStart = brickCol * stride
            val yEnd = side - 1 - brickRow * (brickHeight + gap)
            val yStart = yEnd - (brickHeight - 1)
            if (yStart < 0) return@forEachIndexed

            val brightness = brightnessFor(notif.priority)
            for (dy in 0 until brickHeight) {
                for (dx in 0 until brickWidth) {
                    val x = xStart + dx
                    val y = yStart + dy
                    if (x in 0 until side && y in 0 until side) {
                        frame[y * side + x] = brightness
                    }
                }
            }
        }
        return frame
    }

    private fun priorityRank(level: PriorityLevel): Int = when (level) {
        PriorityLevel.IMPORTANT -> 0
        PriorityLevel.NORMAL -> 1
        PriorityLevel.SILENT -> 2
    }

    private fun brightnessFor(level: PriorityLevel): Int = when (level) {
        PriorityLevel.IMPORTANT -> 255
        PriorityLevel.NORMAL -> 90
        PriorityLevel.SILENT -> 0
    }
}
