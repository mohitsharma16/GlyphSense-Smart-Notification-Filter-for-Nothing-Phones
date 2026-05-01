package com.glyphsense.app.domain

import com.nothing.ketchum.Glyph

sealed class DeviceProfile(
    val deviceId: String,
    val matrixSide: Int,
    val supportsTouch: Boolean
) {
    val pixelCount: Int get() = matrixSide * matrixSide

    object Phone3 : DeviceProfile(
        deviceId = Glyph.DEVICE_23112,
        matrixSide = Glyph.DEVICE_23112_MATRIX_LENGTH,
        supportsTouch = true
    )

    object Phone4aPro : DeviceProfile(
        deviceId = Glyph.DEVICE_25111p,
        matrixSide = Glyph.DEVICE_25111p_MATRIX_LENGTH,
        supportsTouch = false
    )

    companion object {
        val SupportedDevices: List<DeviceProfile> = listOf(Phone3, Phone4aPro)
    }
}