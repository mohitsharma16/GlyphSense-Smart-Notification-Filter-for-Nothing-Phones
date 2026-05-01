package com.glyphsense.app.glyph

import com.glyphsense.app.domain.PendingNotification

sealed class ToyView {
    data object Empty : ToyView()

    data class AppIcon(
        val packageName: String,
        val totalImportant: Int
    ) : ToyView()

    data class Pile(val notifications: List<PendingNotification>) : ToyView()
}

enum class ToyMode {
    AppIcon,
    Pile;

    fun next(): ToyMode = when (this) {
        AppIcon -> Pile
        Pile -> AppIcon
    }
}
